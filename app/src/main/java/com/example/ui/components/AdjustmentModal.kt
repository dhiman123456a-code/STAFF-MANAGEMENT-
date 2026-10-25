package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Adjustment
import com.example.data.model.AdjustmentType
import com.example.data.model.StaffPayrollSummary
import com.example.ui.theme.AmberEsports
import com.example.ui.theme.CoralPenalty
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.EmeraldPaid
import com.example.util.CycleManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdjustmentModal(
    summary: StaffPayrollSummary,
    onDismiss: () -> Unit,
    onAddAdjustment: (type: AdjustmentType, amount: Double, reason: String) -> Unit,
    onDeleteAdjustment: (Adjustment) -> Unit
) {
    var selectedType by remember { mutableStateOf(AdjustmentType.PENALTY) }
    var amountText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }

    val staff = summary.staff

    val finePresets = listOf(
        "Late lobby host (-₹20)",
        "IDP delay (-₹10)",
        "Scoreboard error (-₹20)",
        "AFK / Late match start (-₹50)",
        "Dispute mishandling (-₹30)"
    )

    val bonusPresets = listOf(
        "Overtime stream (+₹50)",
        "Emergency backup host (+₹40)",
        "Weekend rush (+₹50)",
        "Flawless score reporting (+₹30)",
        "Extra tier hosted (+₹40)"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    1.dp,
                    if (selectedType == AdjustmentType.PENALTY) CoralPenalty.copy(alpha = 0.5f) else EmeraldPaid.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                ),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .heightIn(max = 660.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fines & Bonuses",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Staff: ${staff.name} (${staff.role})",
                            fontSize = 12.sp,
                            color = CyanAccent
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Adjustment Type Selector: Bonus vs Fine
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == AdjustmentType.PENALTY,
                        onClick = { selectedType = AdjustmentType.PENALTY },
                        label = { Text("⚠️ Fine / Penalty (-₹)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralPenalty,
                            selectedLabelColor = DeepBackground,
                            containerColor = DarkSurfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("select_fine_chip")
                    )

                    FilterChip(
                        selected = selectedType == AdjustmentType.BONUS,
                        onClick = { selectedType = AdjustmentType.BONUS },
                        label = { Text("🎁 Bonus / Incentive (+₹)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPaid,
                            selectedLabelColor = DeepBackground,
                            containerColor = DarkSurfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("select_bonus_chip")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Field with Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Amount (₹)", fontSize = 12.sp) },
                        placeholder = { Text("e.g. 20") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("adjustment_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (selectedType == AdjustmentType.PENALTY) CoralPenalty else EmeraldPaid
                        )
                    )

                    val quickAmounts = listOf("10", "20", "50", "100")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        quickAmounts.forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .clickable { amountText = amt }
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "₹$amt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedType == AdjustmentType.PENALTY) CoralPenalty else EmeraldPaid
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reason input with Quick Presets
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason / Infraction / Reward", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Late lobby host by 10 mins") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("adjustment_reason_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (selectedType == AdjustmentType.PENALTY) CoralPenalty else EmeraldPaid
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Presets chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = if (selectedType == AdjustmentType.PENALTY) finePresets else bonusPresets
                    presets.forEach { preset ->
                        val cleanReason = preset.substringBefore(" (")
                        val cleanAmt = Regex("""\d+""").find(preset)?.value ?: ""
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .clickable {
                                    reasonText = cleanReason
                                    if (cleanAmt.isNotEmpty()) amountText = cleanAmt
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Adjustment Button
                val parsedAmt = amountText.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = {
                        if (parsedAmt > 0 && reasonText.isNotBlank()) {
                            onAddAdjustment(selectedType, parsedAmt, reasonText.trim())
                            amountText = ""
                            reasonText = ""
                        }
                    },
                    enabled = parsedAmt > 0 && reasonText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == AdjustmentType.PENALTY) CoralPenalty else EmeraldPaid,
                        contentColor = DeepBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("apply_adjustment_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apply ${if (selectedType == AdjustmentType.PENALTY) "Fine (-₹${parsedAmt.toInt()})" else "Bonus (+₹${parsedAmt.toInt()})"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DarkSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // Existing Adjustments List for this cycle
                Text(
                    text = "Active Adjustments (${summary.adjustments.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(8.dp)
                ) {
                    if (summary.adjustments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No fines or bonuses applied yet for this staff member.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        items(summary.adjustments) { adj ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (adj.type == AdjustmentType.BONUS) "🎁" else "⚠️",
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = adj.reason,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (adj.type == AdjustmentType.BONUS) "Bonus (+₹${adj.amount.toInt()})" else "Penalty (-₹${adj.amount.toInt()})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (adj.type == AdjustmentType.BONUS) EmeraldPaid else CoralPenalty
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteAdjustment(adj) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete adjustment",
                                        tint = CoralPenalty.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Net Calculation Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Updated Net Payable:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CycleManager.formatCurrency(summary.netCalculatedPay),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Done", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
