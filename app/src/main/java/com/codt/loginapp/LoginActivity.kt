package com.codt.loginapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var faceNet: FaceNet
    private var capturedBitmap: Bitmap? = null
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        faceNet = FaceNet(this)

        previewView = findViewById(R.id.previewView)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        startCamera()

        btnLogin.setOnClickListener {
            captureFaceAndLogin()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                toast("Camera failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureFaceAndLogin() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    capturedBitmap = imageProxyToBitmap(image)
                    image.close()

                    // Detect face first
                    FaceDetectorHelper.detectFace(capturedBitmap!!) { hasFace ->
                        if (!hasFace) {
                            toast("No face detected")
                            return@detectFace
                        }

                        val capturedEmbedding = faceNet.getEmbedding(capturedBitmap!!)

                        lifecycleScope.launch {
                            val employees = AppDatabase.getDatabase(this@LoginActivity)
                                .employeeDao()
                                .getAll()

                            var matchedName: String? = null
                            for (emp in employees) {
                                val empEmbedding = FaceUtils.stringToEmbedding(emp.faceEmbedding)
                                if (FaceUtils.compareEmbeddings(capturedEmbedding, empEmbedding, 1.0f)) {
                                    matchedName = emp.name
                                    break
                                }
                            }

                            if (matchedName != null) {
                                toast("Login Successful! Welcome $matchedName")
                            } else {
                                toast("Face not recognized")
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    toast("Capture failed: ${exception.message}")
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
