package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.model.Adjustment
import com.example.data.model.AdjustmentType
import com.example.data.model.PaymentRecord
import com.example.data.model.PaymentStatus
import com.example.data.model.RateRule
import com.example.data.model.Shift
import com.example.data.model.Staff
import com.example.data.model.StaffPayrollSummary
import com.example.util.CycleManager
import com.example.util.ShiftParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ScrimPayRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "scrimpay_database.db"
    ).fallbackToDestructiveMigration().build()

    private val staffDao = db.staffDao()
    private val rateRuleDao = db.rateRuleDao()
    private val shiftDao = db.shiftDao()
    private val adjustmentDao = db.adjustmentDao()
    private val paymentRecordDao = db.paymentRecordDao()

    val allStaff: Flow<List<Staff>> = staffDao.getAllStaff()
    val allRateRules: Flow<List<RateRule>> = rateRuleDao.getAllRateRules()

    suspend fun initDefaultsIfEmpty() {
        if (rateRuleDao.getRuleCount() == 0) {
            rateRuleDao.insertAll(ShiftParser.DEFAULT_RULES)
        }
    }

    suspend fun seedSampleStaffIfEmpty() {
        // Only seed if empty
        val existing = staffDao.getStaffById(1)
        if (existing == null) {
            val s1 = Staff(
                name = "Alex 'Viper'",
                role = "Head Lobby Host",
                phone = "+91 98765 43210",
                upiId = "viper.esports@okhdfcbank",
                avatarColor = 0xFF00E5FF
            )
            val s2 = Staff(
                name = "Rohan Sharma",
                role = "Scrim Admin",
                phone = "+91 91234 56789",
                upiId = "rohan.sharma@paytm",
                avatarColor = 0xFFF59E0B
            )
            val s3 = Staff(
                name = "Priya Sen",
                role = "Official Caster",
                phone = "+91 98111 22334",
                upiId = "priyasen@upi",
                avatarColor = 0xFF10B981
            )
            val s4 = Staff(
                name = "Vikram 'Ghost'",
                role = "IDP Moderator",
                phone = "+91 99887 76655",
                upiId = "vikramghost@ybl",
                avatarColor = 0xFFA855F7
            )
            val id1 = staffDao.insertStaff(s1)
            val id2 = staffDao.insertStaff(s2)
            val id3 = staffDao.insertStaff(s3)
            val id4 = staffDao.insertStaff(s4)

            // Seed initial shifts and bonus/penalties for current cycle
            val currentCycle = CycleManager.getCycle(com.example.data.model.CycleType.WEEKLY, 0)
            val cId = currentCycle.id

            // Alex: 3 shifts (-25, -30, -45) + bonus
            shiftDao.insertShifts(
                listOf(
                    Shift(staffId = id1, cycleId = cId, tag = "Tier3-25", amount = 30.0),
                    Shift(staffId = id1, cycleId = cId, tag = "GroupB-30", amount = 30.0),
                    Shift(staffId = id1, cycleId = cId, tag = "GrandFinal-45", amount = 40.0)
                )
            )
            adjustmentDao.insertAdjustment(
                Adjustment(staffId = id1, cycleId = cId, type = AdjustmentType.BONUS, amount = 50.0, reason = "Weekend Overtime Host")
            )

            // Rohan: 4 shifts (-35, -45, -55, -25) + fine
            shiftDao.insertShifts(
                listOf(
                    Shift(staffId = id2, cycleId = cId, tag = "Semi-35", amount = 40.0),
                    Shift(staffId = id2, cycleId = cId, tag = "Final-45", amount = 40.0),
                    Shift(staffId = id2, cycleId = cId, tag = "Elite-55", amount = 40.0),
                    Shift(staffId = id2, cycleId = cId, tag = "Warmup-25", amount = 30.0)
                )
            )
            adjustmentDao.insertAdjustment(
                Adjustment(staffId = id2, cycleId = cId, type = AdjustmentType.PENALTY, amount = 20.0, reason = "Late IDP share (10 min)")
            )

            // Priya: 2 caster shifts (-45, -55)
            shiftDao.insertShifts(
                listOf(
                    Shift(staffId = id3, cycleId = cId, tag = "T1Cast-45", amount = 40.0),
                    Shift(staffId = id3, cycleId = cId, tag = "FinalCast-55", amount = 40.0)
                )
            )
        }
    }

    fun getPayrollSummariesForCycle(cycleId: String): Flow<List<StaffPayrollSummary>> {
        return combine(
            staffDao.getAllStaff(),
            shiftDao.getShiftsForCycle(cycleId),
            adjustmentDao.getAdjustmentsForCycle(cycleId),
            paymentRecordDao.getPaymentRecordsForCycle(cycleId)
        ) { staffList, shifts, adjustments, payments ->
            val shiftMap = shifts.groupBy { it.staffId }
            val adjMap = adjustments.groupBy { it.staffId }
            val payMap = payments.associateBy { it.staffId }

            staffList.map { staff ->
                StaffPayrollSummary(
                    staff = staff,
                    shifts = shiftMap[staff.id] ?: emptyList(),
                    adjustments = adjMap[staff.id] ?: emptyList(),
                    paymentRecord = payMap[staff.id]
                )
            }
        }
    }

    // Staff actions
    suspend fun saveStaff(staff: Staff): Long = staffDao.insertStaff(staff)
    suspend fun updateStaff(staff: Staff) = staffDao.updateStaff(staff)
    suspend fun deleteStaff(staff: Staff) = staffDao.deleteStaff(staff)

    // Shift actions
    suspend fun addShifts(shifts: List<Shift>) = shiftDao.insertShifts(shifts)
    suspend fun deleteShift(shift: Shift) = shiftDao.deleteShift(shift)
    suspend fun clearShiftsForStaff(staffId: Long, cycleId: String) = shiftDao.clearShiftsForStaff(staffId, cycleId)

    // Adjustment actions (Bonus / Penalties)
    suspend fun addAdjustment(adjustment: Adjustment): Long = adjustmentDao.insertAdjustment(adjustment)
    suspend fun updateAdjustment(adjustment: Adjustment) = adjustmentDao.updateAdjustment(adjustment)
    suspend fun deleteAdjustment(adjustment: Adjustment) = adjustmentDao.deleteAdjustment(adjustment)

    // Rate rules
    suspend fun addRateRule(rule: RateRule): Long = rateRuleDao.insertRateRule(rule)
    suspend fun updateRateRule(rule: RateRule) = rateRuleDao.updateRateRule(rule)
    suspend fun deleteRateRule(rule: RateRule) = rateRuleDao.deleteRateRule(rule)

    // Strict Payment Reset Logic
    suspend fun markPaymentAsPaid(staffId: Long, cycleId: String, amount: Double, refNumber: String, note: String) {
        val record = PaymentRecord(
            staffId = staffId,
            cycleId = cycleId,
            status = PaymentStatus.PAID,
            amountPaid = amount,
            referenceNumber = refNumber,
            paidAt = System.currentTimeMillis(),
            note = note
        )
        paymentRecordDao.upsertPaymentRecord(record)
    }

    suspend fun resetPaymentToPending(staffId: Long, cycleId: String) {
        paymentRecordDao.deleteRecord(staffId, cycleId)
    }
}
