package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.AdjustmentType
import com.example.data.model.CycleInfo
import com.example.data.model.StaffPayrollSummary

object WhatsAppFormatter {

    fun generateMasterReport(cycle: CycleInfo, summaries: List<StaffPayrollSummary>): String {
        val sb = StringBuilder()
        sb.append("🏆 *ESPORTS SCRIM STAFF PAYROLL* 🏆\n")
        sb.append("📅 *Period:* ${cycle.label}\n")
        sb.append("📊 *Cycle Mode:* ${if (cycle.type.name == "WEEKLY") "Weekly (Sun-Sat)" else "Bi-Weekly (14 Days)"}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        if (summaries.isEmpty()) {
            sb.append("ℹ️ *No staff records logged for this cycle.*\n")
        } else {
            sb.append("👥 *STAFF SETTLEMENT SUMMARY:*\n")
            sb.append("────────────────────────────\n")

            for (s in summaries) {
                val staff = s.staff
                val statusBadge = if (s.isPaid) "PAID ✅" else "PENDING ⏳"

                sb.append("👤 *${staff.name}* (${staff.role})\n")
                sb.append("🎮 *Shifts:* ${s.shiftCount} lobbies (₹${s.baseShiftAmount.toInt()})\n")

                if (s.bonusAmount > 0) {
                    val reasons = s.adjustments.filter { it.type == AdjustmentType.BONUS }.joinToString(", ") { "${it.reason} (+₹${it.amount.toInt()})" }
                    sb.append("🎁 *Bonus:* +₹${s.bonusAmount.toInt()} ($reasons)\n")
                }

                if (s.penaltyAmount > 0) {
                    val reasons = s.adjustments.filter { it.type == AdjustmentType.PENALTY }.joinToString(", ") { "${it.reason} (-₹${it.amount.toInt()})" }
                    sb.append("⚠️ *Fines:* -₹${s.penaltyAmount.toInt()} ($reasons)\n")
                }

                sb.append("💰 *Net Payable:* ₹${s.netCalculatedPay.toInt()}\n")
                if (staff.upiId.isNotBlank()) {
                    sb.append("💳 *UPI:* `${staff.upiId}`\n")
                }
                sb.append("📌 *Status:* $statusBadge")
                if (s.isPaid && !s.paymentRecord?.referenceNumber.isNullOrBlank()) {
                    sb.append(" (Ref: ${s.paymentRecord?.referenceNumber})")
                }
                sb.append("\n────────────────────────────\n")
            }

            val totalNet = summaries.sumOf { it.netCalculatedPay }
            val totalPaid = summaries.filter { it.isPaid }.sumOf { it.netCalculatedPay }
            val totalPending = summaries.filter { !it.isPaid }.sumOf { it.netCalculatedPay }

            sb.append("\n📈 *CYCLE TOTALS:*\n")
            sb.append("• Total Scrim Payroll: *₹${totalNet.toInt()}*\n")
            sb.append("• Settled / Paid: *₹${totalPaid.toInt()}* ✅\n")
            sb.append("• Remaining Pending: *₹${totalPending.toInt()}* ⏳\n")
        }

        sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("⚡ _Generated with ScrimPay Esports Tracker_")
        return sb.toString()
    }

    fun generateIndividualSlip(cycle: CycleInfo, summary: StaffPayrollSummary): String {
        val s = summary
        val staff = s.staff
        val statusBadge = if (s.isPaid) "PAID ✅" else "PENDING ⏳"

        val sb = StringBuilder()
        sb.append("🎮 *ESPORTS SCRIM STAFF SALARY SLIP* 🎮\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("👤 *Staff:* ${staff.name}\n")
        sb.append("💼 *Role:* ${staff.role}\n")
        sb.append("📅 *Cycle:* ${cycle.label}\n")
        sb.append("────────────────────────────\n")
        sb.append("📊 *BREAKDOWN:*\n")
        sb.append("• Shifts Completed: ${s.shiftCount} lobbies\n")
        sb.append("• Base Shift Earnings: ₹${s.baseShiftAmount.toInt()}\n")

        if (s.shifts.isNotEmpty()) {
            val tags = s.shifts.take(8).joinToString(", ") { it.tag }
            val more = if (s.shifts.size > 8) " +${s.shifts.size - 8} more" else ""
            sb.append("  _Tags: $tags${more}_\n")
        }

        if (s.bonusAmount > 0) {
            sb.append("• Total Bonuses: +₹${s.bonusAmount.toInt()}\n")
            for (adj in s.adjustments.filter { it.type == AdjustmentType.BONUS }) {
                sb.append("  ↳ ${adj.reason}: +₹${adj.amount.toInt()}\n")
            }
        }

        if (s.penaltyAmount > 0) {
            sb.append("• Total Fines/Deductions: -₹${s.penaltyAmount.toInt()}\n")
            for (adj in s.adjustments.filter { it.type == AdjustmentType.PENALTY }) {
                sb.append("  ↳ ${adj.reason}: -₹${adj.amount.toInt()}\n")
            }
        }

        sb.append("────────────────────────────\n")
        sb.append("💰 *TOTAL NET PAY: ₹${s.netCalculatedPay.toInt()}*\n")
        if (staff.upiId.isNotBlank()) {
            sb.append("💳 *UPI VPA:* `${staff.upiId}`\n")
        }
        sb.append("📌 *Payment Status:* $statusBadge\n")
        if (s.isPaid) {
            if (!s.paymentRecord?.referenceNumber.isNullOrBlank()) {
                sb.append("🧾 *UTR / Ref:* ${s.paymentRecord?.referenceNumber}\n")
            }
            sb.append("🎉 *Active Balance Reset: ₹0*\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("⚡ _ScrimPay Official Settlement Record_")
        return sb.toString()
    }

    fun shareToWhatsApp(context: Context, message: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            // Fallback to general share chooser if WhatsApp is not directly installed
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }, "Share Scrim Payroll Summary")
            context.startActivity(chooser)
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "ScrimPay Report") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
