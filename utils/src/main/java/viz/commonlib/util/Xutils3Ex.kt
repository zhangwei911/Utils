package viz.commonlib.util

import com.viz.tools.TimeFormat
import com.viz.tools.l
import org.xutils.http.RequestParams
import org.xutils.http.app.RequestTracker
import org.xutils.http.request.UriRequest
import viz.commonlib.util.FileUtils
import java.io.File
import java.util.*

/**
 * @title: Xutils3Ex
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author zhangwei
 * @date 2020/6/5 10:54
 */
fun RequestParams.requestTrackerLog(httpResDir: String, isSaveLogToFile: Boolean = true) {
    requestTracker = object : RequestTracker {
        override fun onSuccess(request: UriRequest?, result: Any?) {
            xutils3Log(request, result, "success")
        }

        private fun xutils3Log(request: UriRequest?, result: Any?, type: String) {
            request?.apply {
                l.d(result)
                l.df(
                    responseCode,
                    request.params.toString(),
                    responseMessage,
                    responseHeaders
                )
                if (!isSaveLogToFile) {
                    return
                }
                try {
                    val httpResFilePath =
                        httpResDir + TimeFormat.getCurrentTime("yyyyMMddHHmmss") + "-" + UUID.randomUUID() + "-$type.log"
                    val httpResFile = File(httpResFilePath)
                    httpResFile.parentFile?.apply {
                        if (!exists()) {
                            mkdirs()
                        }
                        httpResFile.createNewFile()
                    }
                    httpResFile.createNewFile()
                    val allRes = String.format(
                        "记录时间:%s\nurl:\n%s\n\n请求方式:\n%s\n\n请求头:\n%s\n\n响应code:\n%d\n\n请求body:\n%s\n\n请求query:\n%s\n\n响应消息:\n%s",
                        TimeFormat.getCurrentTime(),
                        request.params.toString(),
                        request.params.method.name,
                        request.params.headers.toMutableList().toString(),
                        responseCode,
                        params.queryStringParams ?: "[null]",
                        params.bodyContent ?: "[null]",
                        responseMessage ?: "[null]"
                    )
                    FileUtils.saveFile(allRes, httpResFilePath)
                    l.i("请求结果保存在[${httpResFilePath}]")
                } catch (e: Exception) {
                    l.e(e.message)
                }
            }
        }

        override fun onWaiting(params: RequestParams?) {
        }

        override fun onRequestCreated(request: UriRequest?) {
        }

        override fun onCancelled(request: UriRequest?) {
        }

        override fun onFinished(request: UriRequest?) {
        }

        override fun onCache(request: UriRequest?, result: Any?) {
        }

        override fun onError(request: UriRequest?, ex: Throwable?, isCallbackError: Boolean) {
            ex?.apply {
                xutils3Log(request, "$message isCallbackError $isCallbackError", "error")
            }
        }

        override fun onStart(params: RequestParams?) {
        }
    }
}