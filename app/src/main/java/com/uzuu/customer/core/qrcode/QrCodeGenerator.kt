package com.uzuu.customer.core.qrcode

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeGenerator {
    
    /**
     * Sinh QR code từ VietQR string hoặc text bất kỳ
     * @param text: VietQR string hoặc text cần mã hóa
     * @param size: kích thước ảnh QR code (pixels)
     * @return Bitmap của QR code hoặc null nếu lỗi
     */
    fun generateQrCode(text: String, size: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 2
            )
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val pixels = IntArray(width * height)
            
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix[x, y]) {
                        0xFF000000.toInt()  // Black
                    } else {
                        0xFFFFFFFF.toInt()  // White
                    }
                }
            }
            
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
