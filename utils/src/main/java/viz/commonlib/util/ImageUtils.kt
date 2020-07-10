package viz.commonlib.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.viz.tools.l
import viz.commonlib.util.FileUtils.getPath
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLConnection
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    private const val TAG = "ImageUtils"
    private val DATE_FORMAT =
        SimpleDateFormat("yyyyMMdd_HHmmss")

    /**
     * 自定义裁剪，根据第一个像素点(左上角)X和Y轴坐标和需要的宽高来裁剪
     *
     * @param srcBitmap
     * @param firstPixelX
     * @param firstPixelY
     * @param needWidth
     * @param needHeight
     * @param recycleSrc
     * @return
     */
    fun cropBitmapCustom(
        srcBitmap: Bitmap,
        firstPixelX: Int,
        firstPixelY: Int,
        needWidth: Int,
        needHeight: Int,
        recycleSrc: Boolean
    ): Bitmap {
        var srcBitmap = srcBitmap
        var needWidth = needWidth
        var needHeight = needHeight

        Log.d("danxx", "cropBitmapRight before w : " + srcBitmap.width)
        Log.d("danxx", "cropBitmapRight before h : " + srcBitmap.height)

        if (firstPixelX + needWidth > srcBitmap.width) {
            needWidth = srcBitmap.width - firstPixelX
        }

        if (firstPixelY + needHeight > srcBitmap.height) {
            needHeight = srcBitmap.height - firstPixelY
        }

        /**裁剪关键步骤 */
        val cropBitmap =
            Bitmap.createBitmap(srcBitmap, firstPixelX, firstPixelY, needWidth, needHeight)

        Log.d("danxx", "cropBitmapRight after w : " + cropBitmap.width)
        Log.d("danxx", "cropBitmapRight after h : " + cropBitmap.height)


        /**回收之前的Bitmap */
        if (recycleSrc && srcBitmap != null && srcBitmap != cropBitmap && !srcBitmap.isRecycled) {
            srcBitmap.recycle()
        }

        return cropBitmap
    }

    fun rotateBitmap(
        source: Bitmap,
        degree: Int,
        flipHorizontal: Boolean,
        recycle: Boolean
    ): Bitmap {
        if (degree == 0) {
            return source
        }
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        if (flipHorizontal) {
            matrix.postScale(-1f, 1f) // 前置摄像头存在水平镜像的问题，所以有需要的话调用这个方法进行水平镜像
        }
        val rotateBitmap =
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, false)
        if (recycle) {
            source.recycle()
        }
        return rotateBitmap
    }

    fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        dir: String = context.packageName,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        compressQuality: Int = 100
    ): String {
        val fileName =
            DATE_FORMAT.format(Date(System.currentTimeMillis())) + if (format == Bitmap.CompressFormat.JPEG) {
                ".jpg"
            } else {
                ".png"
            }
        val outFile = File(
            getPath(context) + "/" + dir,
            fileName
        )
        if (!outFile.exists()) {
            try {
                outFile.parentFile.mkdirs()
                outFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        l.d(TAG, "saveImage. filepath: " + outFile.absolutePath)
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(outFile)
            val success = bitmap.compress(format, compressQuality, os)
            l.d(success)
            if (success) {
                insertToDB(context, outFile.absolutePath)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (os != null) {
                try {
                    os.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return outFile.absolutePath
    }

    fun insertToDB(context: Context, picturePath: String) {
        val file = File(picturePath)
        val mimeType = getMimeType(file)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fileName = file.name
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            val contentResolver: ContentResolver = context.contentResolver
            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri == null) {
                Log.e(TAG, "图片保存失败")
                return
            }
            try {
                val fos =
                    contentResolver.openOutputStream(uri) as FileOutputStream?
                val fis = FileInputStream(file)
                val ofc = fos!!.channel
                val ifc = fis.channel
                val buffer = ByteBuffer.allocateDirect(1024)
                while (ifc.read(buffer) != -1) {
                    buffer.flip()
                    ofc.write(buffer)
                    buffer.clear()
                }
                fis.close()
                fos.close()
                Log.i(TAG, "图片保存成功")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.path),
                arrayOf(mimeType)
            ) { path: String, uri: Uri? ->
                Log.e(
                    TAG,
                    "图片已成功保存到$path"
                )
            }
        }
    }


    fun getMimeType(file: File): String {
        val fileNameMap = URLConnection.getFileNameMap()
        return fileNameMap.getContentTypeFor(file.name)
    }
}