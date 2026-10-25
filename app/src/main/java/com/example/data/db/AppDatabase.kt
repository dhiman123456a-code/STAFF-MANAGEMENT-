package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.Adjustment
import com.example.data.model.AdjustmentType
import com.example.data.model.PaymentRecord
import com.example.data.model.PaymentStatus
import com.example.data.model.RateRule
import com.example.data.model.Shift
import com.example.data.model.Staff
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromAdjustmentType(value: AdjustmentType): String = value.name

    @TypeConverter
    fun toAdjustmentType(value: String): AdjustmentType = enumValueOf(value)

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = enumValueOf(value)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE isArchived = 0 ORDER BY id ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :id LIMIT 1")
    suspend fun getStaffById(id: Long): Staff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff): Long

    @Update
    suspend fun updateStaff(staff: Staff)

    @Delete
    suspend fun deleteStaff(staff: Staff)
}

@Dao
interface RateRuleDao {
    @Query("SELECT * FROM rate_rules ORDER BY id ASC")
    fun getAllRateRules(): Flow<List<RateRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRateRule(rateRule: RateRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<RateRule>)

    @Update
    suspend fun updateRateRule(rateRule: RateRule)

    @Delete
    suspend fun deleteRateRule(rateRule: RateRule)

    @Query("SELECT COUNT(*) FROM rate_rules")
    suspend fun getRuleCount(): Int
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE cycleId = :cycleId")
    fun getShiftsForCycle(cycleId: String): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE staffId = :staffId AND cycleId = :cycleId")
    fun getShiftsForStaffAndCycle(staffId: Long, cycleId: String): Flow<List<Shift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShifts(shifts: List<Shift>)

    @Delete
    suspend fun deleteShift(shift: Shift)

    @Query("DELETE FROM shifts WHERE staffId = :staffId AND cycleId = :cycleId")
    suspend fun clearShiftsForStaff(staffId: Long, cycleId: String)
}

@Dao
interface AdjustmentDao {
    @Query("SELECT * FROM adjustments WHERE cycleId = :cycleId ORDER BY timestamp DESC")
    fun getAdjustmentsForCycle(cycleId: String): Flow<List<Adjustment>>

    @Query("SELECT * FROM adjustments WHERE staffId = :staffId AND cycleId = :cycleId ORDER BY timestamp DESC")
    fun getAdjustmentsForStaffAndCycle(staffId: Long, cycleId: String): Flow<List<Adjustment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: Adjustment): Long

    @Update
    suspend fun updateAdjustment(adjustment: Adjustment)

    @Delete
    suspend fun deleteAdjustment(adjustment: Adjustment)
}

@Dao
interface PaymentRecordDao {
    @Query("SELECT * FROM payment_records WHERE cycleId = :cycleId")
    fun getPaymentRecordsForCycle(cycleId: String): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE staffId = :staffId AND cycleId = :cycleId LIMIT 1")
    suspend fun getPaymentRecord(staffId: Long, cycleId: String): PaymentRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPaymentRecord(record: PaymentRecord): Long

    @Query("DELETE FROM payment_records WHERE staffId = :staffId AND cycleId = :cycleId")
    suspend fun deleteRecord(staffId: Long, cycleId: String)
}

@Database(
    entities = [
        Staff::class,
        RateRule::class,
        Shift::class,
        Adjustment::class,
        PaymentRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun staffDao(): StaffDao
    abstract fun rateRuleDao(): RateRuleDao
    abstract fun shiftDao(): ShiftDao
    abstract fun adjustmentDao(): AdjustmentDao
    abstract fun paymentRecordDao(): PaymentRecordDao
}
