package viz.commonlib.util

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.TextView
import bolts.Task
import com.viz.tools.l
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

fun TextView.htmlPrefix(text: String, prefix: String = "<font color=\"red\">*</font>") {
    html(prefix + text)
}

fun TextView.htmlPrefix(resId: Int, prefix: String = "<font color=\"red\">*</font>") {
    html(prefix + resources.getString(resId))
}

fun TextView.html(htmlRes: Int) {
    html(resources.getString(htmlRes))
}

fun TextView.html(htmlString: String) {
    text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(htmlString)
    }
}

fun String.isUrl(): Boolean {
    return startsWith("http://") || startsWith("https://")
}

fun String.isNumber(): Boolean {
    val pattern: Pattern = Pattern.compile("^[0-9]*$")
    val isNum: Matcher = pattern.matcher(this)
    return isNum.matches()
}

fun String.isDecimal(): Boolean {
    val pattern: Pattern = Pattern.compile("^[0-9.]*$")
    val isNum: Matcher = pattern.matcher(this)
    return isNum.matches() && !this.startsWith(".") && !this.endsWith(".") && this.indexOf(".") != -1 && (this.indexOf(
        "."
    ) == this.lastIndexOf(
        "."
    ))
}

fun String.fileName(): String {
    return File(this).name
}

fun String.toFile(): File {
    return File(this)
}

fun String.fileSize(): Long {
    return File(this).length()
}

fun String.fileScheme(): String {
    return "file://$this"
}

fun String.deleteFile() {
    if (isFileExist()) {
        File(this).delete()
    }
}

fun String.isFileExist(): Boolean {
    return File(this).exists()
}

fun String.createNewFile(): File {
    val file = File(this)
    if (!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }
    return file
}

//使用Channel 实现View 防止重复点击
fun View.setOnceClick(block: suspend () -> Unit) {
    val action = GlobalScope.actor<Unit> {
        for (event in channel) block()
    }
    setOnClickListener {
        action.offer(Unit)
    }
}

fun kotlin.String.subString(
    startIndexStr: String,
    lastIndexStr: String,
    isContainsStart: Boolean = false,
    isContainsLast: Boolean = false
): String {
    val indexStart = indexOf(startIndexStr)
    val indexLast = indexOf(lastIndexStr)
    if (indexStart > -1 && indexLast > -1) {
        return substring(
            indexStart + if (isContainsStart) {
                0
            } else {
                startIndexStr.length
            }, indexLast + if (isContainsLast) {
                lastIndexStr.length
            } else {
                0
            }
        )
    } else if (indexLast > -1 && indexStart == -1) {
        return substring(
            0, indexLast + if (isContainsLast) {
                lastIndexStr.length
            } else {
                0
            }
        )
    } else if (indexStart > -1 && indexLast == -1) {
        return substring(
            indexStart + if (isContainsStart) {
                0
            } else {
                startIndexStr.length
            }
        )
    } else {
        return substring(0)
    }
}

fun kotlin.String.subString(
    startIndex: Int,
    lastIndexStr: String,
    isContainsLast: Boolean = false
): String {
    val index = indexOf(lastIndexStr)
    if (index > -1) {
        return substring(
            startIndex, index + if (isContainsLast) {
                lastIndexStr.length
            } else {
                0
            }
        )
    } else {
        return substring(0)
    }
}

fun kotlin.String.subString(
    startIndexStr: String,
    lastIndex: Int,
    isContainsStart: Boolean = false
): String {
    val index = indexOf(startIndexStr)
    if (index > -1) {
        return substring(
            indexOf(startIndexStr) + if (isContainsStart) {
                0
            } else {
                startIndexStr.length
            }, lastIndex
        )
    } else {
        return substring(0, lastIndex)
    }
}

fun <TResult> Task<TResult>.continueWithEnd(
    taskName: String,
    isLog: Boolean = true
): Task<TResult> {
    return continueWith { t ->
        when {
            t.isCancelled -> {
                l.d("${taskName}任务取消")
            }
            t.isFaulted -> {
                val error = t.error
                l.d("${taskName}任务失败 $error")
                error.printStackTrace()
            }
            else -> {
                if (isLog) {
                    l.d("${taskName}任务成功")
                }
            }
        }
        return@continueWith null
    }
}

/** Use external media if it is available, our app's file directory otherwise */
fun getOutputDirectory(context: Context): File {
    val appContext = context.applicationContext
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, appContext.packageName).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else appContext.filesDir
}


