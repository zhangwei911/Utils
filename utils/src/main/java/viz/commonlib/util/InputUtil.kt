package viz.commonlib.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_FORCED

/**
 * @title: InputUtil
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author wei
 * @date 2020-04-06 13:35
 */
object InputUtil {
    fun show(activity: Activity, view: View) {
        val input = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        input?.showSoftInput(view, SHOW_FORCED)
    }

    fun hide(activity: Activity, view: View) {
        val input = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        input?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}