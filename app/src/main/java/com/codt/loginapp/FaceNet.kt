package com.codt.loginapp

import android.content.Context
import android.graphics.Bitmap
import android.os.FileUtils
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

class FaceNet(context: Context) {

    private val interpreter: Interpreter
    private val inputSize = 160

    init {
        val model = FileUtil.loadMappedFile(context, "facenet.tflite")
        interpreter = Interpreter(model)
    }

    fun getEmbedding(bitmap: Bitmap): FloatArray {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val input = Array(1) { Array(inputSize) { Array(inputSize) { FloatArray(3) } } }

        for (x in 0 until inputSize) {
            for (y in 0 until inputSize) {
                val px = resized.getPixel(x, y)
                input[0][y][x][0] = ((px shr 16 and 0xFF) - 128) / 128f
                input[0][y][x][1] = ((px shr 8 and 0xFF) -128) / 128f
                input[0][y][x][2] = ((px and 0xFF) - 128) / 128f
            }
        }

        val output = Array(1) { FloatArray(512) }
        interpreter.run(input, output)
        return output[0]
    }
}

