package com.codt.loginapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var faceNet: FaceNet
    private var capturedBitmap: Bitmap? = null
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        faceNet = FaceNet(this)

        previewView = findViewById(R.id.previewView)
        val etName = findViewById<EditText>(R.id.etName)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        if (checkSelfPermission(android.Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        } else {
            startCamera() 
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                toast("Enter employee name")
                return@setOnClickListener
            }
            captureFaceAndRegister(name)
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

    private fun captureFaceAndRegister(name: String) {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    capturedBitmap = imageProxyToBitmap(image)
                    image.close()
                    FaceDetectorHelper.detectFace(capturedBitmap!!) { hasFace ->
                        if (!hasFace) {
                            toast("No face detected")
                            return@detectFace
                        }
                        val embedding = faceNet.getEmbedding(capturedBitmap!!)
                        val embString = FaceUtils.embeddingToString(embedding)
                        lifecycleScope.launch {
                            AppDatabase.getDatabase(this@MainActivity)
                                .employeeDao()
                                .insert(EmployeeEntity(name = name, faceEmbedding = embString))
                            toast("Employee registered successfully")
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
