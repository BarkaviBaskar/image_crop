package com.example.crop

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var selectButton: Button
    private lateinit var uri: Uri
    private lateinit var camIntent: Intent
    private lateinit var galIntent: Intent
    private lateinit var cropIntent: Intent

    private val REQUEST_CAMERA = 101
    private val REQUEST_GALLERY = 102
    private val REQUEST_CROP = 103


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        selectButton = findViewById(R.id.selectButton)
        selectButton.setOnClickListener {
            openBottomSheet()
        }
    }

    private fun openBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)

        val btnCamera = bottomSheetView.findViewById<ImageButton>(R.id.cameraButton)
        val btnGallery = bottomSheetView.findViewById<ImageButton>(R.id.galleryButton)

        btnCamera.setOnClickListener {
            openCamera()
            bottomSheetDialog.dismiss()
        }

        btnGallery.setOnClickListener {
            openGallery()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun openGallery() {
        galIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galIntent, REQUEST_GALLERY)
    }

    private fun openCamera() {
        camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(camIntent, REQUEST_CAMERA)
    }

    private fun cropImage(uri: Uri) {
        try {
            cropIntent = Intent("com.android.camera.action.CROP")
            cropIntent.setDataAndType(uri, "image/*")
            cropIntent.putExtra("crop", true)
            cropIntent.putExtra("outputX", 180)
            cropIntent.putExtra("outputY", 180)
            cropIntent.putExtra("aspectX", 3)
            cropIntent.putExtra("aspectY", 4)
            cropIntent.putExtra("scaleUpIfNeeded", true)
            cropIntent.putExtra("return-data", true)
            startActivityForResult(cropIntent, REQUEST_CROP)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    val imageUri = bitmapToUriConverter(imageBitmap) 
                    imageUri?.let { cropImage(it as Uri) }
                }
                REQUEST_GALLERY -> {
                    val imageUri = data?.data
                    imageUri?.let { cropImage(it) }
                }
                REQUEST_CROP -> {
                    val croppedImageUri = data?.data
                    imageView.setImageURI(croppedImageUri)
                }
            }
        }
    }

    private fun bitmapToUriConverter(bitmap: Bitmap): Any {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(applicationContext.contentResolver, bitmap, "temp_image", null)
        return Uri.parse(path)

    }

}



