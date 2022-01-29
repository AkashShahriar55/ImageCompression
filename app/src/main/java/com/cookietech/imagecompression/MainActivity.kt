package com.cookietech.imagecompression

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.cookietech.imagecompression.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kitegamesstudio.kgspicker.builder.ImageFormatClass
import com.kitegamesstudio.kgspicker.builder.PickerBuilderClass
import com.kitegamesstudio.kgspicker.builder.PickerCallback
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList
import android.R.attr.path
import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.drjacky.imagepicker.ImagePicker
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val mainViewModel: MainViewModel by viewModels()
    private lateinit var launcher:ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                Log.d("compress_test", "onCreate: " + uri.path + " " + uri.authority + " " + uri.isAbsolute)
                // Use the uri to load the image
                mainViewModel.compressImage(uri) { bitmap,time,fileSize ->
                    GlobalScope.launch(Dispatchers.Main) {
                        binding.loadingTime.text = "Compression time: $time ms"
                        binding.resolution.text = "Resolution: ${bitmap?.width} * ${bitmap?.height}"
                        binding.size.text = "Size : $fileSize kb"
                        binding.compressedImage.setImageBitmap(bitmap)
                    }
                }

            }
        }

        binding.loadImage.setOnClickListener {
            checkPermission()
        }

    }


    fun checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            Dexter.withActivity(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        loadImage()
                    } else {
                        showPermissionRational()

                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).onSameThread().check()
            return
        }
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                // check if all permissions are granted
                if (report.areAllPermissionsGranted()) {
                    loadImage()
                } else {
                    showPermissionRational()
                    // permission is denied permenantly, navigate user to app settings
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest>,
                token: PermissionToken
            ) {
                token.continuePermissionRequest()
            }
        })
            .onSameThread()
            .check()
    }

    private fun showPermissionRational() {
        AlertDialog.Builder(this@MainActivity)
            .setTitle("Permission Denied")
            .setMessage("You have to give permission") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(
                "SETTINGS",
                DialogInterface.OnClickListener { dialog, which -> // Continue with delete operation
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", baseContext.packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    baseContext.startActivity(intent)
                    dialog.dismiss()
                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun loadImage() {


        launcher.launch(
            ImagePicker.with(this)
                //...
                .galleryOnly()
                .createIntent()
        )
    }









}