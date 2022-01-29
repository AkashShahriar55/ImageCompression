package com.cookietech.imagecompression

import android.graphics.*
import android.media.ExifInterface
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.IOException

object ImageCompressor {


    fun compressImage(imagePath: String,reqHeight:Int, reqWidth: Int): Bitmap? {
        try {

            var reqH = reqHeight
            var reqW = reqWidth

            // decode image size
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)
            val file = File(imagePath)

            Log.d("compress_test", "compressImage: " + file.length())
            if(options.outHeight <= reqH && options.outWidth <= reqW && file.length() < (1024*1024) ){
                return null
            }
            // Find the correct scale value. It should be the power of 2.
            //  int width_tmp = options.outWidth, height_tmp = options.outHeight;
            var scale = 1
            if (reqH == 0 && reqW == 0 && (options.outHeight >= 4096 || options.outWidth >= 4096)) {
                reqW = 2048
                reqH = 2048
            }
            if (reqH > 0 && reqW > 0) scale = calculateInSampleSize(options, reqW, reqH)
            options.inSampleSize = scale
            // Log.d("IMageSampleSize","scale : "+scale+"   "+width_tmp);
            // decode with inSampleSize
            options.inJustDecodeBounds = false
            //      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = ByteArray(16*1024)
            var bitmap = BitmapFactory.decodeFile(imagePath, options)
            if (bitmap == null) {
                Log.d("compress_test", "null bitmap")
                return null
            }
            val exif = ExifInterface(imagePath)
            val rotation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val mat = Matrix()
            val rotationInDegrees: Int = exifToDegrees(rotation)
            mat.postRotate(rotationInDegrees.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mat, true)
            bitmap = resizeBitmapKeepingAspectRatio(bitmap,reqWidth,reqHeight)
            return bitmap
            Log.d("compress_test", bitmap.width.toString() + " " + bitmap.height)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("compress_test", "compressImage: "+e.message)
        }

        return null

    }


    fun resizeBitmapKeepingAspectRatio(
        originalBitmap: Bitmap,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val orginalWidth: Float
        val orginalHeight: Float
        val width: Float
        val heigth: Float
        orginalWidth = originalBitmap.width.toFloat()
        orginalHeight = originalBitmap.height.toFloat()
        Log.d(
            "image_size__",
            "originalH: " + orginalHeight + "originalW" + orginalWidth + "regH: " + reqHeight + "reqW: " + reqWidth
        )
        if (orginalWidth > orginalHeight) {
            width = reqWidth.toFloat()
            heigth = reqWidth * orginalHeight / orginalWidth
        } else {
            heigth = reqHeight.toFloat()
            width = reqHeight * orginalWidth / orginalHeight
        }
        if (width > orginalWidth || heigth > orginalHeight) { // no need to resize if original image is smaller than targeted size
            val originalConfig = originalBitmap.config
            val bitmapConfig = originalConfig ?: Bitmap.Config.ARGB_8888
            //            Log.d(TAG, "===>bitmap config: " + bitmapConfig);
            return originalBitmap.copy(bitmapConfig, false)
        }
        val background = Bitmap.createBitmap(width.toInt(), heigth.toInt(), originalBitmap.config)
        val canvas = Canvas(background)
        val scale = width / orginalWidth
        val xTranslation = 0.0f
        val yTranslation = (heigth - orginalHeight * scale) / 2.0f
        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale, scale)
        val paint = Paint()
        paint.isFilterBitmap = true
        canvas.drawBitmap(originalBitmap, transformation, paint)
        return background
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    private fun exifToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                90
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                180
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                270
            }
            else -> 0
        }
    }


}