package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Staff
import com.example.ui.theme.AmberEsports
import com.example.ui.theme.CoralPenalty
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.EmeraldPaid
import com.example.util.QrCodeHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StaffSettingsDialog(
    staff: Staff?, // null if creating new
    onDismiss: () -> Unit,
    onSaveStaff: (Staff) -> Unit,
    onDeleteStaff: ((Staff) -> Unit)? = null
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(staff?.name ?: "") }
    var role by remember { mutableStateOf(staff?.role ?: "Lobby Host") }
    var phone by remember { mutableStateOf(staff?.phone ?: "") }
    var upiId by remember { mutableStateOf(staff?.upiId ?: "") }
    var selectedColor by remember { mutableStateOf(staff?.avatarColor ?: 0xFF00E5FF) }

    var extractionStatusMessage by remember { mutableStateOf<String?>(null) }
    var extractionSuccess by remember { mutableStateOf<Boolean?>(null) }

    // Android zero-permission Photo Picker for static QR image
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val extractedVpa = QrCodeHelper.extractUpiFromQrBitmap(bitmap)
                    if (extractedVpa != null) {
                        upiId = extractedVpa
                        extractionSuccess = true
                        extractionStatusMessage = "Extracted UPI ID: $extractedVpa"
                        Toast.makeText(context, "UPI ID successfully auto-extracted!", Toast.LENGTH_SHORT).show()
                    } else {
                        extractionSuccess = false
                        extractionStatusMessage = "No UPI QR code detected in image. Please enter manually."
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                extractionSuccess = false
                extractionStatusMessage = "Failed to process image: ${e.localizedMessage}"
            }
        }
    }

    val rolePresets = listOf("Lobby Host", "Head Admin", "Scrim Admin", "Official Caster", "IDP Moderator")
    val avatarColors = listOf(0xFF00E5FF, 0xFFF59E0B, 0xFF10B981, 0xFFA855F7, 0xFFEC4899, 0xFF3B82F6)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
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
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (staff == null) "Add Scrim Staff" else "Edit Staff & UPI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Auto-extract QR or set manual VPA",
                                fontSize = 11.sp,
                                color = AmberEsports
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

                // Staff Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Staff Full Name / In-Game Name", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Alex 'Viper'") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Staff Role
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Designation", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Lobby Host") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_role_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Role Presets
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rolePresets.forEach { r ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .clickable { role = r }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = r, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phone (WhatsApp)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("WhatsApp Phone (Optional)", fontSize = 12.sp) },
                    placeholder = { Text("e.g. +91 98765 43210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent)
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DarkSurfaceVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // UPI Settings & QR Auto-Extraction Box
                Text(
                    text = "UPI Payment & QR Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Upload a static QR code image to auto-detect UPI ID, or type manually below.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Auto-Extract Button
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth().testTag("upload_qr_button")
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upload QR Code Image (Auto-Extract)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Extraction feedback message
                if (extractionStatusMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (extractionSuccess == true) EmeraldPaid.copy(alpha = 0.15f)
                                else CoralPenalty.copy(alpha = 0.15f)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = extractionStatusMessage!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (extractionSuccess == true) EmeraldPaid else CoralPenalty
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Manual UPI input field
                val isUpiValid = upiId.isBlank() || QrCodeHelper.isValidUpiId(upiId)
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it.trim() },
                    label = { Text("UPI ID (VPA)", fontSize = 12.sp) },
                    placeholder = { Text("e.g. rohan@okhdfcbank or staff@upi") },
                    isError = !isUpiValid,
                    supportingText = {
                        if (!isUpiValid) {
                            Text("Invalid UPI format (e.g. username@bank)", color = CoralPenalty)
                        } else if (upiId.isNotBlank()) {
                            Text("Valid UPI VPA format", color = EmeraldPaid)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_upi_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isUpiValid) CyanAccent else CoralPenalty
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Avatar color selection
                Text(
                    text = "Badge Color:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    avatarColors.forEach { colLong ->
                        val col = Color(colLong)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { selectedColor = colLong },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == colLong) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = DeepBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save & Delete Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (staff != null && onDeleteStaff != null) {
                        IconButton(
                            onClick = {
                                onDeleteStaff(staff)
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Staff", tint = CoralPenalty)
                        }
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val newOrUpdated = Staff(
                                    id = staff?.id ?: 0L,
                                    name = name.trim(),
                                    role = role.trim(),
                                    phone = phone.trim(),
                                    upiId = upiId.trim(),
                                    avatarColor = selectedColor,
                                    createdAt = staff?.createdAt ?: System.currentTimeMillis()
                                )
                                onSaveStaff(newOrUpdated)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank() && isUpiValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = DeepBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_staff_button")
                    ) {
                        Text(
                            text = if (staff == null) "Add Staff" else "Save Changes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
