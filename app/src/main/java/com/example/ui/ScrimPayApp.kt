package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Staff
import com.example.ui.components.AdjustmentModal
import com.example.ui.components.CycleSelectorBar
import com.example.ui.components.PaymentQrDialog
import com.example.ui.components.RateConfigDialog
import com.example.ui.components.ShiftParserDialog
import com.example.ui.components.StaffSettingsDialog
import com.example.ui.components.StaffSummaryCard
import com.example.ui.components.WhatsAppExportDialog
import com.example.ui.theme.AmberEsports
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.EmeraldPaid

enum class FilterStatus {
    ALL,
    PENDING_ONLY,
    PAID_ONLY
}

@Composable
fun ScrimPayApp(viewModel: ScrimPayViewModel) {
    val currentCycle by viewModel.currentCycle.collectAsState()
    val cycleType by viewModel.cycleType.collectAsState()
    val summaries by viewModel.summaries.collectAsState()
    val allStaff by viewModel.allStaff.collectAsState()
    val rateRules by viewModel.rateRules.collectAsState()

    val activeStaffForPayment by viewModel.activeStaffForPayment.collectAsState()
    val activeStaffForAdjustment by viewModel.activeStaffForAdjustment.collectAsState()
    val showShiftParser by viewModel.showShiftParserDialog.collectAsState()
    val preselectedStaffId by viewModel.preselectedStaffIdForParser.collectAsState()

    val showStaffSettings by viewModel.showStaffSettingsDialog.collectAsState()
    val staffToEdit by viewModel.staffToEdit.collectAsState()

    val showRateConfig by viewModel.showRateConfigDialog.collectAsState()
    val showWhatsAppExport by viewModel.showWhatsAppExportDialog.collectAsState()
    val individualSlipToExport by viewModel.individualSlipToExport.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf(FilterStatus.ALL) }

    val filteredSummaries = summaries.filter { summary ->
        val matchesQuery = summary.staff.name.contains(searchQuery, ignoreCase = true) ||
                summary.staff.role.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterStatus) {
            FilterStatus.ALL -> true
            FilterStatus.PENDING_ONLY -> !summary.isPaid
            FilterStatus.PAID_ONLY -> summary.isPaid
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = DeepBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.preselectedStaffIdForParser.value = null
                    viewModel.showShiftParserDialog.value = true
                },
                containerColor = CyanAccent,
                contentColor = DeepBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_smart_parser")
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Parse Shifts / Tags",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // App Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanAccent.copy(alpha = 0.2f))
                                .border(1.dp, CyanAccent, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ScrimPay",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AmberEsports)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "ESPORTS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DeepBackground
                                    )
                                }
                            }
                            Text(
                                text = "Staff Payroll & Record Tracker",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action Icons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rate Config Button
                        IconButton(
                            onClick = { viewModel.showRateConfigDialog.value = true },
                            modifier = Modifier.testTag("rate_rules_topbar_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Rate Rules",
                                tint = CyanAccent
                            )
                        }

                        // WhatsApp Summary Export Button
                        IconButton(
                            onClick = {
                                viewModel.individualSlipToExport.value = null
                                viewModel.showWhatsAppExportDialog.value = true
                            },
                            modifier = Modifier.testTag("whatsapp_export_topbar_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "WhatsApp Export",
                                tint = EmeraldPaid
                            )
                        }

                        // Add Staff Button
                        IconButton(
                            onClick = {
                                viewModel.staffToEdit.value = null
                                viewModel.showStaffSettingsDialog.value = true
                            },
                            modifier = Modifier.testTag("add_staff_topbar_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Staff",
                                tint = CyanAccent
                            )
                        }
                    }
                }
            }

            // Main Content Area with LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Cycle Selector & KPI Card
                item {
                    CycleSelectorBar(
                        cycle = currentCycle,
                        cycleType = cycleType,
                        summaries = summaries,
                        onCycleTypeChange = { viewModel.setCycleType(it) },
                        onPreviousCycle = { viewModel.previousCycle() },
                        onNextCycle = { viewModel.nextCycle() },
                        onResetToCurrent = { viewModel.resetToCurrentCycle() }
                    )
                }

                // 2. Search & Filter Bar
                item {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search staff by name or role...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_staff_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Filter Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = filterStatus == FilterStatus.ALL,
                                onClick = { filterStatus = FilterStatus.ALL },
                                label = { Text("All (${summaries.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkSurfaceElevated,
                                    selectedLabelColor = CyanAccent,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            FilterChip(
                                selected = filterStatus == FilterStatus.PENDING_ONLY,
                                onClick = { filterStatus = FilterStatus.PENDING_ONLY },
                                label = { Text("Pending (${summaries.count { !it.isPaid }})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberEsports,
                                    selectedLabelColor = DeepBackground,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            FilterChip(
                                selected = filterStatus == FilterStatus.PAID_ONLY,
                                onClick = { filterStatus = FilterStatus.PAID_ONLY },
                                label = { Text("Settled (${summaries.count { it.isPaid }})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPaid,
                                    selectedLabelColor = DeepBackground,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // 3. Staff Summary Cards List
                if (filteredSummaries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceVariant)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = CyanAccent.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No staff matching '$searchQuery'" else "No staff records found",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap the + button above to register staff or use Smart Parser below.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredSummaries, key = { it.staff.id }) { summary ->
                        StaffSummaryCard(
                            summary = summary,
                            onPayClick = {
                                viewModel.activeStaffForPayment.value = summary
                            },
                            onAdjustmentsClick = {
                                viewModel.activeStaffForAdjustment.value = summary
                            },
                            onAddShiftsClick = {
                                viewModel.preselectedStaffIdForParser.value = summary.staff.id
                                viewModel.showShiftParserDialog.value = true
                            },
                            onEditStaffClick = {
                                viewModel.staffToEdit.value = summary.staff
                                viewModel.showStaffSettingsDialog.value = true
                            },
                            onShareSlipClick = {
                                viewModel.individualSlipToExport.value = summary
                                viewModel.showWhatsAppExportDialog.value = true
                            },
                            onDeleteShift = { shift ->
                                viewModel.deleteShift(shift)
                            },
                            onResetPayment = {
                                viewModel.resetPaymentToPending(summary.staff.id)
                            }
                        )
                    }
                }

                // Bottom spacer for FAB
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    // Overlays & Dialogs

    // 1. Smart Shift Parser Dialog
    if (showShiftParser) {
        ShiftParserDialog(
            staffList = allStaff,
            activeRules = rateRules,
            initialStaffId = preselectedStaffId,
            onDismiss = { viewModel.showShiftParserDialog.value = false },
            onSaveShifts = { staffId, items ->
                viewModel.addParsedShifts(staffId, items)
            }
        )
    }

    // 2. Penalty & Bonus Interactive Modal
    activeStaffForAdjustment?.let { staffSummary ->
        AdjustmentModal(
            summary = staffSummary,
            onDismiss = { viewModel.activeStaffForAdjustment.value = null },
            onAddAdjustment = { type, amount, reason ->
                viewModel.addAdjustment(staffSummary.staff.id, type, amount, reason)
            },
            onDeleteAdjustment = { adj ->
                viewModel.deleteAdjustment(adj)
            }
        )
    }

    // 3. Dynamic UPI Payment QR & Strict Reset Dialog
    activeStaffForPayment?.let { staffSummary ->
        PaymentQrDialog(
            summary = staffSummary,
            cycle = currentCycle,
            onDismiss = { viewModel.activeStaffForPayment.value = null },
            onConfirmPayment = { amount, refNumber, note ->
                viewModel.confirmPayment(staffSummary.staff.id, amount, refNumber, note)
            },
            onResetPayment = {
                viewModel.resetPaymentToPending(staffSummary.staff.id)
                viewModel.activeStaffForPayment.value = null
            }
        )
    }

    // 4. Staff Settings & QR Auto-Extraction Dialog
    if (showStaffSettings) {
        StaffSettingsDialog(
            staff = staffToEdit,
            onDismiss = { viewModel.showStaffSettingsDialog.value = false },
            onSaveStaff = { staff ->
                viewModel.saveStaff(staff)
            },
            onDeleteStaff = { staff ->
                viewModel.deleteStaff(staff)
            }
        )
    }

    // 5. Rate Configuration Dialog
    if (showRateConfig) {
        RateConfigDialog(
            rules = rateRules,
            onDismiss = { viewModel.showRateConfigDialog.value = false },
            onAddRule = { pattern, amount, matchType, desc ->
                viewModel.addRateRule(pattern, amount, matchType, desc)
            },
            onDeleteRule = { rule ->
                viewModel.deleteRateRule(rule)
            }
        )
    }

    // 6. WhatsApp Export Dialog
    if (showWhatsAppExport) {
        WhatsAppExportDialog(
            cycle = currentCycle,
            summaries = summaries,
            initialStaffSummary = individualSlipToExport,
            onDismiss = {
                viewModel.showWhatsAppExportDialog.value = false
                viewModel.individualSlipToExport.value = null
            }
        )
    }
}
