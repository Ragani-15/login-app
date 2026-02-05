package com.codt.loginapp

import kotlin.math.sqrt

object FaceUtils {
    fun embeddingToString(embedding: FloatArray): String {
        return embedding.joinToString(",")
    }

    fun stringToEmbedding(data: String): FloatArray {
        return data.split(",").map { it.toFloat() }.toFloatArray()
    }

    fun compareEmbeddings(e1: FloatArray, e2: FloatArray, threshold: Float = 1.0f): Boolean {
        var sum = 0f
        for (i in e1.indices) {
            val diff = e1[i] - e2[i]
            sum += diff * diff
        }
        return sum < threshold
    }
}

