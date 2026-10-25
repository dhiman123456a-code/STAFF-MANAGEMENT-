package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CycleInfo
import com.example.data.model.CycleType
import com.example.data.model.StaffPayrollSummary
import com.example.ui.theme.AmberEsports
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.EmeraldPaid
import com.example.util.CycleManager

@Composable
fun CycleSelectorBar(
    cycle: CycleInfo,
    cycleType: CycleType,
    summaries: List<StaffPayrollSummary>,
    onCycleTypeChange: (CycleType) -> Unit,
    onPreviousCycle: () -> Unit,
    onNextCycle: () -> Unit,
    onResetToCurrent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPayroll = summaries.sumOf { it.netCalculatedPay }
    val totalPaid = summaries.filter { it.isPaid }.sumOf { it.netCalculatedPay }
    val totalPending = summaries.filter { !it.isPaid }.sumOf { it.netCalculatedPay }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cycle Type Selector Chips (Weekly vs Bi-Weekly)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = cycleType == CycleType.WEEKLY,
                        onClick = { onCycleTypeChange(CycleType.WEEKLY) },
                        label = { Text("Weekly (Sun-Sat)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = DeepBackground,
                            containerColor = DarkSurfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("weekly_cycle_chip")
                    )
                    FilterChip(
                        selected = cycleType == CycleType.BI_WEEKLY,
                        onClick = { onCycleTypeChange(CycleType.BI_WEEKLY) },
                        label = { Text("Bi-Weekly (14d)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = DeepBackground,
                            containerColor = DarkSurfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("biweekly_cycle_chip")
                    )
                }

                if (cycle.offset != 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { onResetToCurrent() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("current_cycle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Jump to Current",
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cycle Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPreviousCycle,
                    modifier = Modifier.size(36.dp).testTag("prev_cycle_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Cycle",
                        tint = CyanAccent
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = cycle.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (cycle.offset == 0) {
                            Text(
                                text = "ACTIVE SCRIM CYCLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldPaid,
                                letterSpacing = 1.sp
                            )
                        } else {
                            val cycleText = if (cycle.offset < 0) "${-cycle.offset} cycles ago" else "Future cycle"
                            Text(
                                text = cycleText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = AmberEsports
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNextCycle,
                    modifier = Modifier.size(36.dp).testTag("next_cycle_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Cycle",
                        tint = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cycle Financial Summary KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total
                KpiCard(
                    title = "Total Scrims",
                    value = CycleManager.formatCurrency(totalPayroll),
                    subtitle = "${summaries.size} Staff",
                    icon = Icons.Default.Payments,
                    accentColor = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                // Settled
                KpiCard(
                    title = "Settled",
                    value = CycleManager.formatCurrency(totalPaid),
                    subtitle = "${summaries.count { it.isPaid }} Paid",
                    icon = Icons.Default.CheckCircle,
                    accentColor = EmeraldPaid,
                    modifier = Modifier.weight(1f)
                )
                // Pending
                KpiCard(
                    title = "Active Pending",
                    value = CycleManager.formatCurrency(totalPending),
                    subtitle = "${summaries.count { !it.isPaid }} Due",
                    icon = Icons.Default.HourglassTop,
                    accentColor = AmberEsports,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
