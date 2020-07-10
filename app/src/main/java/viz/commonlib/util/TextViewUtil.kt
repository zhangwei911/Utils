package viz.commonlib.util

import android.widget.TextView

/**
 * @title: TextViewUtil
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author wei
 * @date 2020-04-07 15:23
 */
class TextViewUtil {
    enum class POSITION {
        TOP,
        RIGHT,
        BOTTON,
        LEFT
    }

    companion object {

        fun setDrawable(view: TextView, resId: Int = -1, position: POSITION = POSITION.RIGHT) {
            setDrawable(view, resId, 0, 0, 0, 0, position)
        }

        fun setDrawable(
            view: TextView,
            resId: Int,
            left: Int,
            top: Int,
            width: Int,
            height: Int,
            position: POSITION
        ) {
            val drawable = if (resId != -1) {
                view.resources.getDrawable(resId, null).apply {
                    setBounds(
                        left,
                        top,
                        if (width == 0) intrinsicWidth else width,
                        if (height == 0) intrinsicHeight else height
                    )
                }
            } else {
                null
            }
            when (position) {
                POSITION.TOP -> {
                    view.setCompoundDrawables(null, drawable, null, null)
                }
                POSITION.RIGHT -> {
                    view.setCompoundDrawables(null, null, drawable, null)
                }
                POSITION.BOTTON -> {
                    view.setCompoundDrawables(null, null, null, drawable)
                }
                POSITION.LEFT -> {
                    view.setCompoundDrawables(drawable, null, null, null)
                }
            }
        }
    }
}