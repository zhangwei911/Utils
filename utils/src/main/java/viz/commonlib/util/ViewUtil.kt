package viz.commonlib.util

import android.util.Size
import android.view.View

/**
 * @title: ViewUtil
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author wei
 * @date 2020-04-06 13:42
 */
object ViewUtil {
    fun isTouchPointInView(targetView: View, xAxis: Int, yAxis: Int): Boolean {
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        val left = location[0]
        val top = location[1]
        val right = left + targetView.measuredWidth
        val bottom = top + targetView.measuredHeight
        if (yAxis in top..bottom && xAxis >= left
            && xAxis <= right
        ) {
            return true
        }
        return false
    }
}