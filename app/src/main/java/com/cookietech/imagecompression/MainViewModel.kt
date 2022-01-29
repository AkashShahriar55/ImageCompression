package com.cookietech.imagecompression

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.zelory.compressor.saveBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {



    fun compressImage(imageUri: Uri, onCompletion: (bitmap: Bitmap?,time:Long,fileSize:Long?) -> Unit) {
        viewModelScope.launch{
            withContext(Dispatchers.Default){
                val start = System.currentTimeMillis()
                val imagePath = Utils.getRealPathFromUri(getApplication(),imageUri)
                var output = ImageCompressor.compressImage(imagePath,2160,2160)
                val end = System.currentTimeMillis()

                val filename = imagePath.substring(imagePath.lastIndexOf("/") + 1)
                var fileSize:Long? = 0

                if(output != null){
                    val file = Utils.savePhoto(output,getApplication(),90,filename)
                    fileSize = file?.length()?.div(1024)
                    onCompletion(output,end-start,fileSize)
                }else{
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = false
                    output = BitmapFactory.decodeFile(imagePath, options)
                    fileSize = File(imagePath).length().div(1024)
                    onCompletion(output,end-start,fileSize)
                }



            }
        }

    }
}