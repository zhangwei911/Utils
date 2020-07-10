package viz.commonlib.util

import com.viz.tools.Toast
import viz.commonlib.utils.R

/**
 * @author wei
 * @title: TipsUtil
 * @projectName CloudRoom
 * @description: no implements function tips
 * @date 2020-03-12 13:23
 */
object TipsUtil {
    fun show() {
        Toast.show(R.string.function_deving)
    }
}