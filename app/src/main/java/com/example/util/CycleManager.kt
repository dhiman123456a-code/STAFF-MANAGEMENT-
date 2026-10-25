package com.example.util

import com.example.data.model.CycleInfo
import com.example.data.model.CycleType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CycleManager {

    private val labelDateFormat = SimpleDateFormat("EEE, MMM d", Locale.ENGLISH)
    private val yearDateFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
    private val idDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    /**
     * Compute Sunday-to-Saturday cycle for the given cycleType and offset from current date.
     * Offset 0 = current cycle, -1 = previous cycle, +1 = next cycle.
     */
    fun getCycle(cycleType: CycleType, offset: Int = 0): CycleInfo {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.SUNDAY

        // Align to current Sunday
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromSunday = dayOfWeek - Calendar.SUNDAY
        cal.add(Calendar.DAY_OF_MONTH, -daysFromSunday)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Apply cycle offset
        val stepDays = if (cycleType == CycleType.WEEKLY) 7 else 14
        cal.add(Calendar.DAY_OF_MONTH, offset * stepDays)

        val startDate = cal.time
        val startMillis = cal.timeInMillis

        // End date is following Saturday (6 days for weekly, 13 days for bi-weekly)
        val durationDays = if (cycleType == CycleType.WEEKLY) 6 else 13
        cal.add(Calendar.DAY_OF_MONTH, durationDays)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)

        val endDate = cal.time
        val endMillis = cal.timeInMillis

        val cyclePrefix = if (cycleType == CycleType.WEEKLY) "W" else "BW"
        val cycleId = "${cyclePrefix}_${idDateFormat.format(startDate)}_${idDateFormat.format(endDate)}"

        val startStr = labelDateFormat.format(startDate)
        val endStr = labelDateFormat.format(endDate)
        val yearStr = yearDateFormat.format(endDate)
        val label = "$startStr - $endStr, $yearStr"

        return CycleInfo(
            id = cycleId,
            type = cycleType,
            startDateMillis = startMillis,
            endDateMillis = endMillis,
            label = label,
            offset = offset
        )
    }

    fun formatCurrency(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            "₹${amount.toInt()}"
        } else {
            "₹${String.format(Locale.ENGLISH, "%.2f", amount)}"
        }
    }
}
