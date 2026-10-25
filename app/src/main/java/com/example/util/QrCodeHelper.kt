package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.EnumMap
import java.util.Locale

object QrCodeHelper {

    private val UPI_VPA_REGEX = Regex("""[a-zA-Z0-9.\-_]{2,256}@[a-zA-Z]{2,64}""")

    fun isValidUpiId(upi: String): Boolean {
        return UPI_VPA_REGEX.matches(upi.trim())
    }

    /**
     * Builds standard NPCI UPI payment deep link
     */
    fun buildUpiUri(upiId: String, name: String, amount: Double, note: String): String {
        val cleanUpi = upiId.trim()
        val cleanName = URLEncoder.encode(name.trim(), StandardCharsets.UTF_8.name())
        val cleanNote = URLEncoder.encode(note.trim(), StandardCharsets.UTF_8.name())
        val formattedAmount = String.format(Locale.ENGLISH, "%.2f", amount)

        return "upi://pay?pa=$cleanUpi&pn=$cleanName&am=$formattedAmount&cu=INR&tn=$cleanNote"
    }

    /**
     * Generates a dynamic QR Code bitmap from payment content
     */
    fun generateDynamicQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 2)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
            }
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Auto-extracts UPI ID (VPA) from an uploaded QR code bitmap image
     */
    fun extractUpiFromQrBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
                put(DecodeHintType.TRY_HARDER, java.lang.Boolean.TRUE)
            }

            val result = QRCodeReader().decode(binaryBitmap, hints)
            val rawText = result.text ?: return null
            extractVpaFromRawString(rawText)
        } catch (e: Exception) {
            // Decoding failed or no QR found
            null
        }
    }

    /**
     * Extracts UPI VPA from a raw string (either upi:// URL or plain string)
     */
    fun extractVpaFromRawString(raw: String): String? {
        val trimmed = raw.trim()

        // 1. Try URI query param parsing for "upi://pay?pa=..."
        if (trimmed.startsWith("upi://", ignoreCase = true) || trimmed.contains("pa=", ignoreCase = true)) {
            val paParamMatch = Regex("""[?&]pa=([^&]+)""", RegexOption.IGNORE_CASE).find(trimmed)
            if (paParamMatch != null) {
                val encodedVpa = paParamMatch.groupValues[1]
                val decodedVpa = try {
                    URLDecoder.decode(encodedVpa, StandardCharsets.UTF_8.name()).trim()
                } catch (e: Exception) {
                    encodedVpa.trim()
                }
                if (isValidUpiId(decodedVpa)) {
                    return decodedVpa
                }
            }
        }

        // 2. Scan entire text for any valid UPI VPA regex pattern
        val match = UPI_VPA_REGEX.find(trimmed)
        if (match != null) {
            return match.value
        }

        return null
    }
}
