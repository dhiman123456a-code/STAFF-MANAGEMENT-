package com.example

import com.example.data.model.Adjustment
import com.example.data.model.AdjustmentType
import com.example.data.model.CycleType
import com.example.data.model.PaymentRecord
import com.example.data.model.PaymentStatus
import com.example.data.model.Shift
import com.example.data.model.Staff
import com.example.data.model.StaffPayrollSummary
import com.example.util.CycleManager
import com.example.util.QrCodeHelper
import com.example.util.ShiftParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testShiftParser_StandardRates() {
        val rules = ShiftParser.DEFAULT_RULES

        // -25 and -30 must resolve to ₹30
        val (rate25, _) = ShiftParser.resolveRate("Match-25", rules)
        assertEquals(30.0, rate25, 0.01)

        val (rate30, _) = ShiftParser.resolveRate("T3-30", rules)
        assertEquals(30.0, rate30, 0.01)

        // -35, -45, -55 must resolve to ₹40
        val (rate35, _) = ShiftParser.resolveRate("Semi-35", rules)
        assertEquals(40.0, rate35, 0.01)

        val (rate45, _) = ShiftParser.resolveRate("Final-45", rules)
        assertEquals(40.0, rate45, 0.01)

        val (rate55, _) = ShiftParser.resolveRate("Elite-55", rules)
        assertEquals(40.0, rate55, 0.01)
    }

    @Test
    fun testShiftParser_BulkInput() {
        val input = "Alex: -25, -30, -35, -45, -55"
        val parsed = ShiftParser.parseInput(input, ShiftParser.DEFAULT_RULES)

        assertEquals(1, parsed.size)
        assertEquals("Alex", parsed[0].detectedStaffName)
        assertEquals(5, parsed[0].count)
        // 2 x 30 + 3 x 40 = 60 + 120 = 180
        assertEquals(180.0, parsed[0].totalAmount, 0.01)
    }

    @Test
    fun testCycleManager_SundayToSaturday() {
        val weeklyCycle = CycleManager.getCycle(CycleType.WEEKLY, 0)
        assertTrue(weeklyCycle.label.startsWith("Sun"))
        assertTrue(weeklyCycle.label.contains("Sat"))

        val biweeklyCycle = CycleManager.getCycle(CycleType.BI_WEEKLY, 0)
        assertTrue(biweeklyCycle.label.startsWith("Sun"))
        assertTrue(biweeklyCycle.label.contains("Sat"))
    }

    @Test
    fun testQrCodeHelper_UpiUriAndVpa() {
        val uri = QrCodeHelper.buildUpiUri(
            upiId = "viper@okhdfcbank",
            name = "Alex Viper",
            amount = 310.0,
            note = "ScrimPay W42"
        )
        assertTrue(uri.startsWith("upi://pay?pa=viper@okhdfcbank"))
        assertTrue(uri.contains("am=310.00"))

        val extracted = QrCodeHelper.extractVpaFromRawString(uri)
        assertEquals("viper@okhdfcbank", extracted)

        assertTrue(QrCodeHelper.isValidUpiId("alex@okhdfcbank"))
        assertTrue(QrCodeHelper.isValidUpiId("rohan.sharma@paytm"))
        assertFalse(QrCodeHelper.isValidUpiId("not_a_upi"))
    }

    @Test
    fun testStrictPaymentResetLogic() {
        val staff = Staff(id = 1, name = "Alex", role = "Host")
        val shifts = listOf(
            Shift(id = 1, staffId = 1, cycleId = "c1", tag = "-25", amount = 30.0),
            Shift(id = 2, staffId = 1, cycleId = "c1", tag = "-45", amount = 40.0)
        )
        val adjustments = listOf(
            Adjustment(id = 1, staffId = 1, cycleId = "c1", type = AdjustmentType.BONUS, amount = 50.0, reason = "Overtime"),
            Adjustment(id = 2, staffId = 1, cycleId = "c1", type = AdjustmentType.PENALTY, amount = 20.0, reason = "Late")
        )

        // Pending summary: 30 + 40 + 50 - 20 = 100
        val pendingSummary = StaffPayrollSummary(staff, shifts, adjustments, paymentRecord = null)
        assertFalse(pendingSummary.isPaid)
        assertEquals(100.0, pendingSummary.netCalculatedPay, 0.01)
        assertEquals(100.0, pendingSummary.activePayableBalance, 0.01)

        // Paid summary: Active balance strictly resets to 0.0
        val paidRecord = PaymentRecord(id = 1, staffId = 1, cycleId = "c1", status = PaymentStatus.PAID, amountPaid = 100.0)
        val paidSummary = StaffPayrollSummary(staff, shifts, adjustments, paymentRecord = paidRecord)
        assertTrue(paidSummary.isPaid)
        assertEquals(0.0, paidSummary.activePayableBalance, 0.01)
    }
}
