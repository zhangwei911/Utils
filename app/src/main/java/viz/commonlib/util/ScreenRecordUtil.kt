package viz.commonlib.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import com.viz.tools.Toast
import com.viz.tools.l
import viz.commonlib.utils.R
import viz.commonlib.service.ScreenRecordService
import java.io.File
import java.util.*
import kotlin.math.min

/**
 * @title: ScreenRecordUtil
 * @projectName InsuranceDoubleRecord
 * @description: Screen Record
 * @author wei
 * @date 2020-04-03 12:19
 */
class ScreenRecordUtil {
    private var mediaProjectionManager: MediaProjectionManager? = null
    var isRecording = false
    fun request(activity: Activity, requestCode: Int) {
        mediaProjectionManager =
            activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager!!.createScreenCaptureIntent()
        val packageManager: PackageManager = activity.packageManager
        if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            //存在录屏授权的Activity
            activity.startActivityForResult(intent, requestCode)
        } else {
            Toast.show(activity, R.string.can_not_record_tip)
        }
    }

    fun result(
        activity: Activity,
        requestCodeStart: Int,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onResult: ((isAllow: Boolean) -> Unit)
    ) {
        if (requestCode == requestCodeStart) {
            if (resultCode == Activity.RESULT_OK) {
                l.d("start screen record service")
                isRecording = true
                l.d(data)
                val service = Intent(activity, ScreenRecordService::class.java)
                service.putExtra(SCAN, requestCode)
                service.putExtra("resultCode", resultCode)
                service.putExtra("data", data)
                l.d(Build.VERSION.SDK_INT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.startForegroundService(service)
                } else {
                    activity.startService(service)
                }
                onResult.invoke(true)
            } else {
                onResult.invoke(false)
            }
        }
    }

    fun stop(activity: Activity) {
        isRecording = false
        val service = Intent(activity, ScreenRecordService::class.java)
        activity.stopService(service)
    }
}