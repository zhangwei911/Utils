package viz.commonlib.util

import android.app.Dialog
import android.content.Context
import android.icu.text.CaseMap
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import kotlinx.android.synthetic.main.dialog_download.*
import viz.commonlib.utils.R

class ProgressBarDialog(
    context: Context,
    private val title: String
) :
    Dialog(context, R.style.progress_dialog) {

    var cancelClick: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_download)
        val lp = window!!.attributes
        val d = context.resources.displayMetrics
        lp.width = (d.widthPixels * 0.8).toInt()
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        window?.attributes = lp
        this.setCancelable(false)
        setTitle(title)
        materialButton_cancel_download.setOnClickListener {
            cancelClick?.invoke()
        }
    }

    fun setTitle(title: String) {
        textView_title_download.text = title
    }

    fun show(
        showBG: Boolean = false
    ) {
        if (!showBG) {
            window?.setDimAmount(0f)
        }
        show()
        setProgress(0)
    }

    fun setProgress(progress: Int) {
        progressBar_download.progress = progress
        textView_progress_download.text =
            "${progress}%"
    }
}