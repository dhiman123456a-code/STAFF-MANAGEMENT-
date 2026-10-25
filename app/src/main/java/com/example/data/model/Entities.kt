package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String = "Lobby Host", // e.g. Lobby Host, Scrim Admin, Caster, Moderator
    val phone: String = "",
    val upiId: String = "", // e.g. name@okhdfcbank
    val avatarColor: Long = 0xFF00E5FF,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rate_rules")
data class RateRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tagPattern: String, // e.g. "-25", "-30", "-35", "-45", "-55"
    val rateAmount: Double, // e.g. 30.0 or 40.0
    val matchType: String = "SUFFIX", // SUFFIX, CONTAINS, EXACT
    val description: String = ""
)

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val staffId: Long,
    val cycleId: String, // e.g. "2026-10-18_2026-10-24"
    val tag: String, // e.g. "Alpha-25"
    val amount: Double, // calculated rate e.g. 30.0
    val dateMillis: Long = System.currentTimeMillis(),
    val rawInput: String = ""
)

enum class AdjustmentType {
    PENALTY, // Fine / deduction
    BONUS    // Extra reward / incentive
}

@Entity(tableName = "adjustments")
data class Adjustment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val staffId: Long,
    val cycleId: String,
    val type: AdjustmentType,
    val amount: Double,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class PaymentStatus {
    PENDING,
    PAID
}

@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val staffId: Long,
    val cycleId: String,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val amountPaid: Double = 0.0,
    val referenceNumber: String = "", // UTR or Txn ID
    val paidAt: Long? = null,
    val note: String = ""
)

data class StaffPayrollSummary(
    val staff: Staff,
    val shifts: List<Shift>,
    val adjustments: List<Adjustment>,
    val paymentRecord: PaymentRecord?
) {
    val shiftCount: Int get() = shifts.size
    val baseShiftAmount: Double get() = shifts.sumOf { it.amount }
    val bonusAmount: Double get() = adjustments.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount }
    val penaltyAmount: Double get() = adjustments.filter { it.type == AdjustmentType.PENALTY }.sumOf { it.amount }
    val netCalculatedPay: Double get() = (baseShiftAmount + bonusAmount - penaltyAmount).coerceAtLeast(0.0)

    val isPaid: Boolean get() = paymentRecord?.status == PaymentStatus.PAID
    val activePayableBalance: Double
        get() = if (isPaid) 0.0 else netCalculatedPay
}

enum class CycleType {
    WEEKLY,
    BI_WEEKLY
}

data class CycleInfo(
    val id: String,
    val type: CycleType,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val label: String,
    val offset: Int // 0 = current, -1 = previous, +1 = next
)
