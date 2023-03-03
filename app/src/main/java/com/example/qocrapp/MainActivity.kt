package com.example.qocrapp

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.qocrapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@ExperimentalGetImage
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QOcrApp"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).toTypedArray()
    }

    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartCamera.setOnClickListener {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        }

        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // no need to save the image to device
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmapImage = binding.viewFinder.bitmap

                    bitmapImage?.let {
                        openExtractedTextResultFragment(it)
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    private fun openExtractedTextResultFragment(bitmapImage: Bitmap) {
        ExtractedTextResultFragment(
            bitmap = bitmapImage,
            actionCallback = { extractedText ->

            }
        ).show(supportFragmentManager, ExtractedTextResultFragment::class.simpleName)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            binding.viewFinder.visibility = View.VISIBLE
            binding.imageView.setImageURI(null)

            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

