package viz.commonlib.util

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import androidx.arch.core.util.Function
import androidx.core.content.FileProvider
import com.viz.tools.l
import java.io.*
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


/**
 * @author wei
 * @title: FileUtils
 * @projectName CloudRoom
 * @description:
 * @date 2020-03-13 22:37
 */
object FileUtils {
    @JvmStatic
    fun getPath(context: Context): String {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            context.getExternalFilesDir(null)!!.absolutePath
        }
    }

    @JvmStatic
    fun selectFile(
        activity: Activity,
        mimeTypes: Array<String?>?,
        requestCode: Int
    ) {
        //ACTION_GET_CONTENT ACTION_OPEN_DOCUMENT
        val intent = Intent()
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        activity.startActivityForResult(Intent.createChooser(intent, "choose file"), requestCode)
//ACTION_PICK
//        Intent intent = Intent()
//        intent.action = Intent.ACTION_PICK
//        intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        startActivityForResult(Intent.createChooser(intent, "pick file"), 101)
    }

    @JvmStatic
    fun selectFileResult(
        requestCodeStart: Int,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        function: Function<List<Uri>?, Void?>
    ) {
        if (resultCode != Activity.RESULT_OK || requestCode != requestCodeStart || data == null) {
            return
        }
        val uri = data.data
        val uris: MutableList<Uri> =
            ArrayList()
        if (uri != null) {
            uris.add(uri)
        } else {
            val clipData = data.clipData
            if (clipData != null) {
                val clipDataSize = clipData.itemCount
                for (i in 0 until clipDataSize) {
                    val item = clipData.getItemAt(i)
                    uris.add(item.uri)
                }
            }
        }
        function.apply(uris)
    }

    /**
     * 获取文件选择器选中的文件路径
     *
     * @param context
     * @param uri
     * @return
     */
    @JvmStatic
    fun getPath(context: Context, uri: Uri): String? {
        val path: String?
        //使用第三方应用打开
        if ("file".equals(uri.scheme, ignoreCase = true)) {
            path = uri.path
            return path
        }
        //4.4以后
        path = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            getPathFromNewSdk(context, uri)
            //4.4以下下系统调用方法
        } else {
            getPathFromOldSdk(context, uri)
        }
        return path
    }

    private fun getPathFromOldSdk(
        context: Context,
        contentUri: Uri
    ): String? {
        var res = ""
        val proj = arrayOf(
            MediaStore.Images.Media.DATA
        )
        val cursor =
            context.contentResolver.query(contentUri, proj, null, null, null)
        if (null != cursor && cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
            cursor.close()
        }
        return res
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    private fun getPathFromNewSdk(
        context: Context,
        uri: Uri
    ): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        l.df("文件协议", uri.scheme)
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                l.d("isExternalStorageDocument")
                val docId = DocumentsContract.getDocumentId(uri)
                l.d(docId)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/" + docId.replace("$type:", "")
                }
            } else if (isDownloadsDocument(uri)) {
                l.d("isDownloadsDocument")
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                l.d("isMediaDocument")
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                l.df("类型", type)
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs =
                    arrayOf(docId.replace("$type:", ""))
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return ""
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return ""
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    const val REQUEST_CODE_OPEN_DIRECTORY = 1100

    @JvmStatic
    fun request(activity: Activity, requestCodeStart: Int) {
        val sharedPreferences =
            activity.getSharedPreferences("data", Context.MODE_PRIVATE)
        val uriStr = sharedPreferences.getString("uri", "")
        if (!TextUtils.isEmpty(uriStr)) {
            try {
                val uri = Uri.parse(uriStr)
                val takeFlags = activity.intent.flags and
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                // Check for the freshest data.
                activity.contentResolver.takePersistableUriPermission(uri, takeFlags)
                updateDatas(activity, uri)
            } catch (e: SecurityException) {
                e.printStackTrace()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                activity.startActivityForResult(intent, requestCodeStart)
            }
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            activity.startActivityForResult(intent, requestCodeStart)
        }
    }

    @JvmStatic
    fun updateDatas(activity: Activity, uriTree: Uri?) {
        l.d(uriTree.toString())
        val sharedPreferences =
            activity.getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("uri", uriTree.toString())
        editor.apply()
    }

    @JvmStatic
    fun result(
        activity: Activity,
        requestCodeStart: Int,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        function: Function<Uri?, Void?>
    ) {
        if (requestCode == requestCodeStart && resultCode == Activity.RESULT_OK) {
            l.d(
                String.format(
                    "Open Directory result Uri : %s",
                    data!!.data
                )
            )
            val uriTree = data.data
            val sharedPreferences =
                activity.getSharedPreferences("data", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("uri", uriTree.toString())
            editor.apply()
            val takeFlags =
                data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            activity.contentResolver.takePersistableUriPermission(uriTree!!, takeFlags)
            updateDatas(activity, uriTree)
            function.apply(uriTree)
        }
    }

    @JvmStatic
    fun getRealPathInAndroid10(activity: Activity, uri: Uri): String {
        var rPath = ""
        try {
            l.start("nio copy")
            activity.contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor?.let {
                val fis = FileInputStream(it)
                val inChannel = fis.channel
                getPath(activity, uri)?.let { path ->
                    rPath = getPath(activity) + "/tmp/" + UUID.randomUUID() + "/" + File(
                        path
                    ).name
                    val t = File(rPath)
                    if (!t.parentFile.exists()) {
                        t.parentFile.mkdirs()
                    }
                    t.createNewFile()
                    val out = FileOutputStream(t)
                    val outChannel = out.channel
                    inChannel.transferTo(0, inChannel.size(), outChannel)
                    fis.close()
                    inChannel.close()
                    out.close()
                    outChannel.close()
                }
            }
            l.end("nio copy")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rPath
    }

    /**
     * 删除SD卡中的文件或目录
     *
     * @param path
     * @return
     */
    fun deleteSDFile(path: String?): Boolean {
        return deleteSDFile(path, false)
    }

    /**
     * 删除SD卡中的文件或目录
     *
     * @param path
     * @param deleteParent true为删除父目录
     * @return
     */
    fun deleteSDFile(path: String?, deleteParent: Boolean): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        val file = File(path)
        return if (!file.exists()) {
            //不存在
            true
        } else deleteFile(file, deleteParent)
    }

    /**
     * @param file
     * @param deleteParent true为删除父目录
     * @return
     */
    fun deleteFile(file: File?, deleteParent: Boolean): Boolean {
        var file = file
        var flag = false
        if (file == null) {
            return flag
        }
        if (file.isDirectory) {
            //是文件夹
            val files = file.listFiles()
            if (files.size > 0) {
                for (i in files.indices) {
                    flag = deleteFile(files[i], true)
                    if (!flag) {
                        return flag
                    }
                }
            }
            if (deleteParent) {
                flag = file.delete()
            }
        } else {
            flag = file.delete()
        }
        file = null
        return flag
    }

    /**
     * 添加到媒体数据库
     *
     * @param context 上下文
     */
    fun fileScanVideo(
        context: Context,
        videoPath: String,
        videoWidth: Int,
        videoHeight: Int,
        videoTime: Int
    ): Uri? {
        val file = File(videoPath)
        if (file.exists()) {
            var uri: Uri? = null
            val size = file.length()
            val fileName = file.name
            val dateTaken = System.currentTimeMillis()
            val values = ContentValues(11)
            values.put(MediaStore.Video.Media.DATA, videoPath) // 路径;
            values.put(MediaStore.Video.Media.TITLE, fileName) // 标题;
            values.put(MediaStore.Video.Media.DURATION, videoTime * 1000) // 时长
            values.put(MediaStore.Video.Media.WIDTH, videoWidth) // 视频宽
            values.put(MediaStore.Video.Media.HEIGHT, videoHeight) // 视频高
            values.put(MediaStore.Video.Media.SIZE, size) // 视频大小;
            values.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken) // 插入时间;
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName) // 文件名;
            values.put(MediaStore.Video.Media.DATE_MODIFIED, dateTaken / 1000) // 修改时间;
            values.put(MediaStore.Video.Media.DATE_ADDED, dateTaken / 1000) // 添加时间;
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            val resolver = context.contentResolver
            if (resolver != null) {
                uri = try {
                    resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    null
                }
            }
            if (uri == null) {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(videoPath),
                    arrayOf("video/*")
                ) { path, uri -> }
            }
            return uri
        }
        return null
    }

    /**
     * SD卡存在并可以使用
     */
    fun isSDExists(): Boolean {
        return Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
    }

    /**
     * 获取SD卡的剩余容量，单位是Byte
     *
     * @return
     */
    fun getSDFreeMemory(): Long {
        try {
            if (isSDExists()) {
                val pathFile = Environment.getExternalStorageDirectory()
                // Retrieve overall information about the space on a filesystem.
                // This is a Wrapper for Unix statfs().
                val statfs = StatFs(pathFile.path)
                // 获取SDCard上每一个block的SIZE
                val nBlockSize = statfs.blockSize.toLong()
                // 获取可供程序使用的Block的数量
                // long nAvailBlock = statfs.getAvailableBlocksLong();
                val nAvailBlock = statfs.availableBlocks.toLong()
                // 计算SDCard剩余大小Byte
                return nAvailBlock * nBlockSize
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return 0
    }

    fun getUri(
        context: Context,
        authorites: String,
        file: File
    ): Uri? {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //设置7.0以上共享文件，分享路径定义在xml/file_paths.xml
            FileProvider.getUriForFile(context, authorites, file)
        } else {
            // 7.0以下,共享文件
            Uri.fromFile(file)
        }
        return uri
    }

    /**
     * RandomAccessFile 获取文件的MD5值
     *
     * @param file 文件路径
     * @return md5
     */
    fun getFileMd5(file: File?): String? {
        val messageDigest: MessageDigest
        var randomAccessFile: RandomAccessFile? = null
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            if (file == null) {
                return ""
            }
            if (!file.exists()) {
                return ""
            }
            randomAccessFile = RandomAccessFile(file, "r")
            val bytes = ByteArray(1024 * 1024 * 10)
            var len = 0
            while (randomAccessFile.read(bytes).also({ len = it }) != -1) {
                messageDigest.update(bytes, 0, len)
            }
            val bigInt = BigInteger(1, messageDigest.digest())
            var md5: String = bigInt.toString(16)
            while (md5.length < 32) {
                md5 = "0$md5"
            }
            return md5
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close()
                    randomAccessFile = null
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return ""
    }

    fun copyFromAsset(
        context: Context,
        fileName: String?,
        dst: File,
        overwrite: Boolean
    ) {
        if (!dst.exists() || overwrite) {
            try {
                dst.createNewFile()
                val `in` = context.assets.open(fileName!!)
                copyInStreamToFile(`in`, dst)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    fun copyInStreamToFile(`in`: InputStream, dst: File?) {
        val out = FileOutputStream(dst)
        copyFile(`in`, out)
        `in`.close()
        out.flush()
        out.close()
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    /**
     * 保存文件
     */
    fun saveFile(src: String, filename: String) {
        var foChannel: FileChannel?
        try {
            foChannel = FileOutputStream(filename).channel
            foChannel.write(ByteBuffer.wrap(src.toByteArray()))
            foChannel.close()
            foChannel = null
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}