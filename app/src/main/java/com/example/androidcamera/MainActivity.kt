package com.example.androidcamera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.androidcamera.databinding.ActivityMainBinding
import java.io.File
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

const val REQUEST_CODE = 101
const val REQUEST_CODE_TAKE_PICTURE = 42

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var takePictureBtn: Button
    private lateinit var img: ImageView
    private lateinit var takePicLauncher: ActivityResultLauncher<Uri>
    private lateinit var photoUri: Uri
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        takePictureBtn = activityMainBinding.cameraBtn
        img = activityMainBinding.img1
        takePicLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    val bitmap = loadBitmapFromUri(photoUri)
                    img.setImageBitmap(bitmap)
                } else {
                    Toast.makeText(this, "Image Load failed", Toast.LENGTH_LONG).show()
                }
            }

        takePictureBtn.setOnClickListener {
            requestPermissions()
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPhotoUri(): Uri {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return FileProvider.getUriForFile(
            this,
             "com.example.androidcamera.fileprovider",
            createImageFile(storageDir)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createImageFile(storageDir : File?) : File {
        val timeStamp = LocalDate.now()
        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val time = df.format(timeStamp)
        val imgFileName = "JPG+" + time + "_"
        val imagefile = File.createTempFile(imgFileName,".jpg",storageDir)
        return imagefile
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (!hasCameraPermissions()) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE
            )
        } else {
            giveAccess()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun giveAccess() {
        photoUri = createPhotoUri()
        takePicLauncher.launch(photoUri)
    }

    private fun hasCameraPermissions() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && !grantResults.isEmpty()) {
            giveAccess()
        }
    }
}