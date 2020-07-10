package viz.commonlib.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.progress_dialog.*
import viz.commonlib.utils.R

class ProgressDialog(context: Context, private val onBack: (() -> Unit)? = null) :
    Dialog(context, R.style.progress_dialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_dialog)
        val lp = window!!.attributes
        val d = context.resources.displayMetrics
        lp.width = (d.widthPixels * 0.5).toInt()
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        window?.attributes = lp
        this.setCancelable(false)
        setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                dismiss()
                onBack?.invoke()
            }
            false
        }
    }

    fun show(
        text: String,
        showError: Boolean = false,
        showCancel: Boolean = false,
        showBG: Boolean = false
    ) {
        if (!showBG) {
            window?.setDimAmount(0f)
        }
        show()
        textView_info.text = text
        textView_info_error.visibility = if (showError) {
            View.VISIBLE
        } else {
            View.GONE
        }
        materialButton_cancel_upload.visibility = if (showCancel) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun setMessage(msg: String) {
        textView_info?.text = msg
    }
}