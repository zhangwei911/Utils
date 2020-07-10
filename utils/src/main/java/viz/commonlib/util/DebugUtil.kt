package viz.commonlib.util

import android.content.Context
import android.content.pm.ApplicationInfo


/**
 * @title: DebugUtil
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author wei
 * @date 2020-04-21 13:50
 */
object DebugUtil {
    fun isDebug(context: Context): Boolean {
        return try {
            context.applicationInfo != null && context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE !== 0
        } catch (e: Exception) {
            false
        }
    }
}