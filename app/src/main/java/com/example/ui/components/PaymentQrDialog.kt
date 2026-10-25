package com.example.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CycleInfo
import com.example.data.model.StaffPayrollSummary
import com.example.ui.theme.AmberEsports
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.EmeraldPaid
import com.example.util.CycleManager
import com.example.util.QrCodeHelper
import com.example.util.WhatsAppFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentQrDialog(
    summary: StaffPayrollSummary,
    cycle: CycleInfo,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, refNumber: String, note: String) -> Unit,
    onResetPayment: () -> Unit
) {
    val context = LocalContext.current
    val staff = summary.staff
    val isPaid = summary.isPaid
    val netAmount = if (isPaid) summary.paymentRecord?.amountPaid ?: summary.netCalculatedPay else summary.netCalculatedPay

    var refNumber by remember { mutableStateOf(summary.paymentRecord?.referenceNumber ?: "") }
    var paymentNote by remember { mutableStateOf(summary.paymentRecord?.note ?: "") }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var upiUriString by remember { mutableStateOf("") }

    LaunchedEffect(staff.upiId, netAmount, cycle.label) {
        val effectiveUpi = if (staff.upiId.isNotBlank()) staff.upiId else "esports.host@okhdfcbank"
        val note = "ScrimPay ${cycle.label}"
        val uri = QrCodeHelper.buildUpiUri(
            upiId = effectiveUpi,
            name = staff.name,
            amount = netAmount,
            note = note
        )
        upiUriString = uri
        qrBitmap = QrCodeHelper.generateDynamicQrBitmap(uri, sizePx = 480)
    }

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
                    if (isPaid) EmeraldPaid.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                ),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .heightIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPaid) EmeraldPaid.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPaid) Icons.Default.Receipt else Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = if (isPaid) EmeraldPaid else CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPaid) "Payment Receipt" else "Dynamic UPI Payment QR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = staff.name + " (" + staff.role + ")",
                                fontSize = 12.sp,
                                color = CyanAccent
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Settlement Amount Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPaid) EmeraldPaid.copy(alpha = 0.15f) else DarkSurfaceVariant)
                        .border(1.dp, if (isPaid) EmeraldPaid else AmberEsports, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPaid) "SETTLED AMOUNT (RESET TO ₹0)" else "ACTIVE PAYABLE BALANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isPaid) EmeraldPaid else AmberEsports,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CycleManager.formatCurrency(netAmount),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isPaid) EmeraldPaid else CyanAccent
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPaid) EmeraldPaid else AmberEsports)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isPaid) "PAID ✅" else "PENDING ⏳",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = DeepBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic QR Code Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SCAN VIA ANY UPI APP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "GPay • PhonePe • Paytm • BHIM",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Dynamic UPI QR Code",
                                modifier = Modifier
                                    .size(200.dp)
                                    .testTag("dynamic_qr_code_image")
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF0F172A))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (staff.upiId.isNotBlank()) staff.upiId else "No UPI configured",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Launch UPI App directly button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUriString))
                            val chooser = Intent.createChooser(intent, "Pay via UPI App")
                            context.startActivity(chooser)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No UPI app found. Please scan QR instead.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = DeepBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("launch_upi_intent_button")
                ) {
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open Directly in UPI App",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Strict Payment Reset & Verification Section
                if (!isPaid) {
                    Text(
                        text = "Strict Payment Settlement Verification:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Status stays PENDING until you confirm below. Marking as PAID strictly resets active pending balance to ₹0.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = refNumber,
                        onValueChange = { refNumber = it },
                        label = { Text("UTR / Transaction Reference (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("e.g. 423948291039") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("utr_reference_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("Internal Note (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Verified on bank SMS") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onConfirmPayment(netAmount, refNumber.trim(), paymentNote.trim())
                            Toast.makeText(context, "Payment Confirmed! Active balance reset to ₹0.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPaid,
                            contentColor = DeepBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_payment_paid_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verify & Mark as PAID (Reset to ₹0)",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // Paid Receipt Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Settlement Record",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = EmeraldPaid
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!summary.paymentRecord?.referenceNumber.isNullOrBlank()) {
                            Text(
                                text = "Reference (UTR): ${summary.paymentRecord?.referenceNumber}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!summary.paymentRecord?.note.isNullOrBlank()) {
                            Text(
                                text = "Note: ${summary.paymentRecord?.note}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        summary.paymentRecord?.paidAt?.let { ts ->
                            val dateStr = SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.ENGLISH).format(Date(ts))
                            Text(
                                text = "Confirmed: $dateStr",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Active pending balance is strictly ₹0.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPaid
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onResetPayment,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Revert to PENDING Status",
                            color = AmberEsports,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
