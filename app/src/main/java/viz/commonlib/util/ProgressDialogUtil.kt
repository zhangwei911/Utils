package viz.commonlib.util

import android.content.Context
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.viz.tools.l
import viz.commonlib.utils.R


/**
 * @title: ProgressDialogUtil
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author wei
 * @date 2020-04-13 15:14
 */
class ProgressDialogUtil(private val context: Context) {
    private var dialog: ProgressDialog? = null
    var onBack: (() -> Unit)? = null
    fun show(text: String = "") {
        if (dialog == null) {
            dialog = ProgressDialog(context, onBack)
            dialog?.show(text)
        } else {
            if (!isShowing()) {
                dialog?.show(text)
            }
        }
    }

    fun update(text: String) {
        dialog?.setMessage(text)
    }

    fun hide() {
        if (isShowing()) {
            dialog?.dismiss()
        }
    }

    fun isShowing(): Boolean {
        return dialog?.isShowing ?: false
    }
}