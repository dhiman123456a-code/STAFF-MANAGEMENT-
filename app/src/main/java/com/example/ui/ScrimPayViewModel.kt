package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Adjustment
import com.example.data.model.AdjustmentType
import com.example.data.model.CycleInfo
import com.example.data.model.CycleType
import com.example.data.model.RateRule
import com.example.data.model.Shift
import com.example.data.model.Staff
import com.example.data.model.StaffPayrollSummary
import com.example.data.repository.ScrimPayRepository
import com.example.util.CycleManager
import com.example.util.ParsedShiftItem
import com.example.util.QrCodeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScrimPayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScrimPayRepository(application)

    private val _cycleType = MutableStateFlow(CycleType.WEEKLY)
    val cycleType: StateFlow<CycleType> = _cycleType.asStateFlow()

    private val _cycleOffset = MutableStateFlow(0)
    val cycleOffset: StateFlow<Int> = _cycleOffset.asStateFlow()

    private val _currentCycle = MutableStateFlow(CycleManager.getCycle(CycleType.WEEKLY, 0))
    val currentCycle: StateFlow<CycleInfo> = _currentCycle.asStateFlow()

    val allStaff: StateFlow<List<Staff>> = repository.allStaff.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val rateRules: StateFlow<List<RateRule>> = repository.allRateRules.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Dynamic summaries for active cycle
    val summaries: StateFlow<List<StaffPayrollSummary>> = _currentCycle.flatMapLatest { cycle ->
        repository.getPayrollSummariesForCycle(cycle.id)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // UI Dialog & Selection States
    val activeStaffForPayment = MutableStateFlow<StaffPayrollSummary?>(null)
    val activeStaffForAdjustment = MutableStateFlow<StaffPayrollSummary?>(null)
    val showShiftParserDialog = MutableStateFlow(false)
    val preselectedStaffIdForParser = MutableStateFlow<Long?>(null)

    val showStaffSettingsDialog = MutableStateFlow(false)
    val staffToEdit = MutableStateFlow<Staff?>(null)

    val showRateConfigDialog = MutableStateFlow(false)
    val showWhatsAppExportDialog = MutableStateFlow(false)
    val individualSlipToExport = MutableStateFlow<StaffPayrollSummary?>(null)

    val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            repository.initDefaultsIfEmpty()
            repository.seedSampleStaffIfEmpty()
        }
    }

    private fun refreshCycle() {
        _currentCycle.value = CycleManager.getCycle(_cycleType.value, _cycleOffset.value)
    }

    fun setCycleType(type: CycleType) {
        _cycleType.value = type
        _cycleOffset.value = 0
        refreshCycle()
    }

    fun previousCycle() {
        _cycleOffset.value -= 1
        refreshCycle()
    }

    fun nextCycle() {
        _cycleOffset.value += 1
        refreshCycle()
    }

    fun resetToCurrentCycle() {
        _cycleOffset.value = 0
        refreshCycle()
    }

    // Staff actions
    fun saveStaff(staff: Staff, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (staff.id == 0L) {
                repository.saveStaff(staff)
            } else {
                repository.updateStaff(staff)
            }
            onComplete()
        }
    }

    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
        }
    }

    // Shift actions
    fun addParsedShifts(staffId: Long, items: List<ParsedShiftItem>) {
        viewModelScope.launch {
            val cId = _currentCycle.value.id
            val shifts = items.map { item ->
                Shift(
                    staffId = staffId,
                    cycleId = cId,
                    tag = item.tag,
                    amount = item.amount,
                    rawInput = item.matchedRule
                )
            }
            repository.addShifts(shifts)
        }
    }

    fun addManualShift(staffId: Long, tag: String, amount: Double) {
        viewModelScope.launch {
            val cId = _currentCycle.value.id
            repository.addShifts(
                listOf(
                    Shift(
                        staffId = staffId,
                        cycleId = cId,
                        tag = tag,
                        amount = amount
                    )
                )
            )
        }
    }

    fun deleteShift(shift: Shift) {
        viewModelScope.launch {
            repository.deleteShift(shift)
        }
    }

    fun clearShiftsForStaff(staffId: Long) {
        viewModelScope.launch {
            repository.clearShiftsForStaff(staffId, _currentCycle.value.id)
        }
    }

    // Adjustments (Bonus / Penalties)
    fun addAdjustment(staffId: Long, type: AdjustmentType, amount: Double, reason: String) {
        viewModelScope.launch {
            val cId = _currentCycle.value.id
            repository.addAdjustment(
                Adjustment(
                    staffId = staffId,
                    cycleId = cId,
                    type = type,
                    amount = amount,
                    reason = reason
                )
            )
        }
    }

    fun deleteAdjustment(adjustment: Adjustment) {
        viewModelScope.launch {
            repository.deleteAdjustment(adjustment)
        }
    }

    // Strict Payment Reset Logic
    fun confirmPayment(staffId: Long, amount: Double, referenceNumber: String, note: String) {
        viewModelScope.launch {
            val cId = _currentCycle.value.id
            repository.markPaymentAsPaid(
                staffId = staffId,
                cycleId = cId,
                amount = amount,
                refNumber = referenceNumber,
                note = note
            )
            activeStaffForPayment.value = null
        }
    }

    fun resetPaymentToPending(staffId: Long) {
        viewModelScope.launch {
            val cId = _currentCycle.value.id
            repository.resetPaymentToPending(staffId, cId)
        }
    }

    // Rate Rules
    fun addRateRule(pattern: String, amount: Double, matchType: String, description: String) {
        viewModelScope.launch {
            repository.addRateRule(
                RateRule(
                    tagPattern = pattern,
                    rateAmount = amount,
                    matchType = matchType,
                    description = description
                )
            )
        }
    }

    fun deleteRateRule(rule: RateRule) {
        viewModelScope.launch {
            repository.deleteRateRule(rule)
        }
    }

    fun updateRateRule(rule: RateRule) {
        viewModelScope.launch {
            repository.updateRateRule(rule)
        }
    }

    // Auto-Extraction QR helper
    fun extractUpiFromBitmap(bitmap: Bitmap): String? {
        return QrCodeHelper.extractUpiFromQrBitmap(bitmap)
    }
}
