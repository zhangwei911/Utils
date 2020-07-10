package viz.commonlib.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.viz.tools.TimeFormat
import com.viz.tools.Toast
import com.viz.tools.apk.PreferencesUtils
import com.viz.tools.l
import viz.commonlib.utils.R
import viz.commonlib.util.FileUtils
import viz.commonlib.util.SCAN
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.min


/**
 * @title: ScreenRecorderService
 * @projectName InsuranceDoubleRecord
 * @description: Screen Recorder Service
 * @author wei
 * @date 2020-04-03 14:03
 */
class ScreenRecordService : Service() {
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var savePath: String? = null
    private var saveName: String? = null
    private var saveFile: File? = null
    private var mediaRecorder: MediaRecorder? = null
    var recordAudio = true
    var VIDEO_FRAME_RATE = 20
    private var virtualDisplay: VirtualDisplay? = null
    var isRecording = false
    var requestCode = -1
    var resultCode = -1
    var mData: Intent? = null
    override fun onBind(intent: Intent?): IBinder {
        return RecordBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            try {
                mediaProjectionManager =
                    baseContext.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
                requestCode = getIntExtra(SCAN, -1)
                resultCode = getIntExtra("resultCode", -1)
                mData = getParcelableExtra<Intent>("data")
                mediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode, mData!!)
                // 实测，部分手机上录制视频的时候会有弹窗的出现，所以我们需要做一个 150ms 的延迟
                Handler().postDelayed({
                    if (initRecorder()) {
                        try {
                            l.d("start")
                            mediaRecorder?.start()
                            isRecording = true
                        } catch (e: Exception) {
                            Toast.show(baseContext, R.string.screen_record_start_failed)
                            e.printStackTrace()
                        }
                    } else {
                        Toast.show(baseContext, R.string.phone_not_support_screen_record)
                    }
                }, 150)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var lss: LocalServerSocket? = null
    private var sender: LocalSocket? = null
    private var receiver: LocalSocket? = null
    private fun initLocalSocket(): Boolean {
        var ret = true
        try {
            releaseLocalSocket()
            val serverName = "armAudioServer"
            val bufSize = 1024
            lss = LocalServerSocket(serverName)
            receiver = LocalSocket()
            receiver?.connect(LocalSocketAddress(serverName))
            receiver?.receiveBufferSize = bufSize
            receiver?.sendBufferSize = bufSize
            sender = lss!!.accept()
            sender?.receiveBufferSize = bufSize
            sender?.sendBufferSize = bufSize
        } catch (e: IOException) {
            ret = false
        }
        return ret
    }

    private fun releaseLocalSocket() {
        try {
            sender?.close()
            receiver?.close()
            lss?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sender = null
        receiver = null
        lss = null
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            //数字是随便写的“40”，
            nm.createNotificationChannel(
                NotificationChannel(
                    "40",
                    "App Service",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            val builder = NotificationCompat.Builder(this, "40")

            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2, builder.build())
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

    inner class RecordBinder : Binder() {
        val recordService: ScreenRecordService
            get() = this@ScreenRecordService
    }

    private fun initRecorder(): Boolean {
        l.d("initRecorder")
        var result = true
        savePath =
            FileUtils.getPath(baseContext) + "/${baseContext.packageName}/screen/" + TimeFormat.getCurrentTime(
                "yyyyMMdd"
            )
        saveName = PreferencesUtils.getString(baseContext, "videoId", "") + ".mp4"
        // 创建文件夹
        val f = File(savePath)
        if (!f.exists()) {
            f.mkdirs()
        }
        // 录屏保存的文件
        saveFile = File(savePath, saveName)
        PreferencesUtils.putString(baseContext, "recordPath", saveFile!!.absolutePath)
        saveFile?.apply {
            if (exists()) {
                delete()
            }
        }
        mediaRecorder = MediaRecorder()
        val dm = baseContext.resources.displayMetrics
        val width = min(dm.widthPixels, 1080)
        val height = min(dm.heightPixels, 1920)
        mediaRecorder?.apply {
            // 可以设置是否录制音频
            if (recordAudio) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            if (recordAudio) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }
            setOutputFile(saveFile!!.absolutePath)
            setVideoSize(width, height)
            setVideoEncodingBitRate(500000)
            setVideoFrameRate(VIDEO_FRAME_RATE)
            setOnErrorListener { mr, what, extra ->
                l.df(what, extra)
            }
            setOnInfoListener { mr, what, extra ->
                l.df(what, extra)
            }
            try {
                l.d("prepare")
                prepare()
                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "MainScreen", width, height, dm.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null
                )
                l.d("initRecorder 成功")
            } catch (e: Exception) {
                l.e("IllegalStateException preparing MediaRecorder: ${e.message}")
                e.printStackTrace()
                result = false
            }
        }
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        clearAll()
    }

    fun stop() {
        if (isRecording) {
            isRecording = false
            try {
                mediaRecorder?.apply {
                    setOnErrorListener(null)
                    setOnInfoListener(null)
                    setPreviewDisplay(null)
                    stop()
                    l.d("stop success")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                l.e("stopRecorder() error！${e.message}")
            } finally {
                mediaRecorder?.reset()
                virtualDisplay?.release()
                mediaProjection?.stop()
            }
        }
    }

    fun clearAll() {
        mediaRecorder?.release()
        mediaRecorder = null
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
    }
}