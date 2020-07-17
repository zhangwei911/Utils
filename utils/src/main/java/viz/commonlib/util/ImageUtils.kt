package viz.commonlib.util

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import com.viz.tools.apk.ScreenUtils
import com.viz.tools.l
import org.xutils.common.util.DensityUtil
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



    /**
     * 屏幕截图
     * @param activity
     * @return
     */
    fun screenShot(activity: Activity?, onResult: ((filePath: String) -> Unit)): Bitmap? {
        if (activity == null) {
            l.e("screenShot--->activity is null")
            return null
        }
        val view: View = activity.window.decorView
        //允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true)
        view.buildDrawingCache()
        val navigationBarHeight: Int = ScreenUtils.getNavigationBarHeight(activity)


        //获取屏幕宽和高
        val width: Int = DensityUtil.getScreenWidth()
        val height: Int = DensityUtil.getScreenHeight()

        // 全屏不用考虑状态栏，有导航栏需要加上导航栏高度
        var bitmap: Bitmap? = null
        try {
            bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, width,
                    height + navigationBarHeight)
        } catch (e: java.lang.Exception) {
            // 这里主要是为了兼容异形屏做的处理，我这里的处理比较仓促，直接靠捕获异常处理
            // 其实vivo oppo等这些异形屏手机官网都有判断方法
            // 正确的做法应该是判断当前手机是否是异形屏，如果是就用下面的代码创建bitmap
            var msg = e.message
            // 部分手机导航栏高度不占窗口高度，不用添加，比如OppoR15这种异形屏
            if (msg!!.contains("<= bitmap.height()")) {
                try {
                    bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, width,
                            height)
                } catch (e1: java.lang.Exception) {
                    msg = e1.message
                    // 适配Vivo X21异形屏，状态栏和导航栏都没有填充
                    if (msg!!.contains("<= bitmap.height()")) {
                        try {
                            bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, width,
                                    height - ScreenUtils.getStatusBarHeight(view.context))
                        } catch (e2: java.lang.Exception) {
                            e2.printStackTrace()
                        }
                    } else {
                        e1.printStackTrace()
                    }
                }
            } else {
                e.printStackTrace()
            }
        }

        //销毁缓存信息
        view.destroyDrawingCache()
        view.setDrawingCacheEnabled(false)
        if (null != bitmap) {
            try {
                val filePath = ImageUtils.saveBitmap(view.context, bitmap)
                l.d("--->截图保存地址：$filePath")
                onResult.invoke(filePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }

    /**
     * view截图
     * @return
     */
    fun viewShot(v: View, onResult: ((filePath: String) -> Unit)) {
        if (null == v) {
            l.e("view is null")
            return
        }
        v.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    v.viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    v.viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
                // 核心代码start
                val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
                val c = Canvas(bitmap)
                v.layout(0, 0, v.layoutParams.width, v.layoutParams.height)
                v.draw(c)
                // end
                try {
                    val savePath = ImageUtils.saveBitmap(v.context, bitmap)
                    l.d("--->截图保存地址：$savePath")
                    onResult.invoke(savePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })
    }
}