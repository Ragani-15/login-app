package com.codt.loginapp

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

object FaceDetectorHelper {

    fun detectFace(
        bitmap: Bitmap,
        onResult: (Boolean) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                onResult(faces.isNotEmpty())
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
