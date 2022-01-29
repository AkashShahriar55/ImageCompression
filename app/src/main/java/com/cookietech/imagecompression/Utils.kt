package com.cookietech.imagecompression

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.*

object Utils {
    fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        return try {
            Log.d("mylastpathprint2", "getRealPathFromUri: ")
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val databaseUri: Uri
            val selection: String?
            val selectionArgs: Array<String>?
            if (contentUri.path!!.contains("/document/image:")) {
                databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                selection = "_id=?"
                selectionArgs = arrayOf(DocumentsContract.getDocumentId(contentUri).split(":")[1])
            } else {
                databaseUri = contentUri
                selection = null
                selectionArgs = null
            }
            cursor = context.contentResolver.query(databaseUri, proj, selection, selectionArgs, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            Log.d("mylastpathprint2", "return: ")
            Log.d("save_debug", "real path done")
            cursor.getString(column_index)
        } catch (ex: Exception) {
            Log.d("save_debug", "real path failed ")
            ex.printStackTrace()
            ""
        } finally {
            cursor?.close()
        }
    }

    fun savePhoto(bmp: Bitmap, mContext: Context, quality:Int, filename:String): File? {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val imageFileFolder =
                File(Environment.getExternalStorageDirectory(), "Compressed")
            imageFileFolder.mkdir()
            var out: FileOutputStream? = null
            val c = Calendar.getInstance()
            val date: String =
                (fromInt(c[Calendar.MONTH].toLong())
                        + fromInt(c[Calendar.DAY_OF_MONTH].toLong())
                        + fromInt(c[Calendar.YEAR].toLong())
                        + fromInt(c[Calendar.HOUR_OF_DAY].toLong())
                        + fromInt(c[Calendar.MINUTE].toLong())
                        + fromInt(c[Calendar.SECOND].toLong()))
            var imageFileName: File? = null
            val picId: String = fromInt(System.currentTimeMillis()) + ""
            try {
                imageFileName = File(imageFileFolder, "$filename")
                out = FileOutputStream(imageFileName)
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
                Log.d("OutputTest", "png.......")

                //saveImagePath = imageFileName.getAbsolutePath();
                //            Log.i("Istiak", saveImagePath);
                out.flush()
                out.close()
                // scanPhoto(imageFileName.toString());
                out = null
            } catch (e: Exception) {
                Log.d("akash_debug", "savePhoto: failed")
                e.printStackTrace()
            }
            imageFileName
        } else {
            val c = Calendar.getInstance()
            val date: String =
                (fromInt(c[Calendar.MONTH].toLong())
                        + fromInt(c[Calendar.DAY_OF_MONTH].toLong())
                        + fromInt(c[Calendar.YEAR].toLong())
                        + fromInt(c[Calendar.HOUR_OF_DAY].toLong())
                        + fromInt(c[Calendar.MINUTE].toLong())
                        + fromInt(c[Calendar.SECOND].toLong()))
            val picId: String = fromInt(System.currentTimeMillis()) + ""
            val path = "Pictures/Compressed"

            //            if(type == BackgroundEraserActivity.JPEG)
            //                filename=filename+".jpeg";
            //            else
            //                filename = filename+".png";
            val collection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val newImage = ContentValues()
            var mimtype: String? = ""

            newImage.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            newImage.put(MediaStore.MediaColumns.MIME_TYPE, mimtype)
            newImage.put(MediaStore.MediaColumns.DATE_ADDED, date)
            newImage.put(MediaStore.MediaColumns.DATE_MODIFIED, date)
            newImage.put(MediaStore.MediaColumns.SIZE, bmp.byteCount)
            newImage.put(MediaStore.MediaColumns.WIDTH, bmp.width)
            newImage.put(MediaStore.MediaColumns.HEIGHT, bmp.height)
            newImage.put(MediaStore.MediaColumns.RELATIVE_PATH, path)
            newImage.put(MediaStore.MediaColumns.IS_PENDING, 1)
            val newImageUri = mContext.contentResolver.insert(collection, newImage)
            var imageFileName: File
            try {
                val out2 = mContext.contentResolver.openOutputStream(newImageUri!!, "w")
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, out2)
                newImage.clear()
                newImage.put(MediaStore.MediaColumns.IS_PENDING, 0)
                mContext.contentResolver.update(newImageUri, newImage, null, null)
            } catch (e: Exception) {
                Toast.makeText(mContext, "Save Failed$e", Toast.LENGTH_SHORT).show()
                Log.d("save_debug", "savePhoto: failed")
                e.printStackTrace()
            }


            //Log.d("sharing_debug", "savePhotoForGreaterAndroid28: " + getRealPathFromUri(this,newImageUri));
            Log.d("save_debug", "savePhoto: finished ")
            File(
                newImageUri?.let {
                    getRealPathFromUri(
                        mContext,
                        it
                    )
                }
            )
        }
    }

    private fun fromInt(`val`: Long): String? {
        return `val`.toString()
    }
}