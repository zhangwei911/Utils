package viz.commonlib.http

import com.viz.tools.TimeFormat
import com.viz.tools.l
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import org.json.JSONObject
import viz.commonlib.util.FileUtils.saveFile
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.charset.UnsupportedCharsetException
import java.util.*
import java.util.concurrent.TimeUnit

class ResultInterceptor(
    private val httpResDir: String,
    private val save: Boolean,
    private val excludeUrls: MutableList<String> = mutableListOf()
) :
    Interceptor {
    private val UTF8 = Charset.forName("UTF-8")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val rUrl = URLDecoder.decode(request.url.toString(), StandardCharsets.UTF_8.name())
        val isLog = !excludeUrls.contains(request.url.toUrl().path) && !excludeUrls.contains(
            request.url.toUrl().toString()
        ) && excludeUrls.none {
            request.url.toUrl().toString().endsWith(it)
        } && excludeUrls.none {
            request.url.toUrl().toString().startsWith(it)
        }

        var body: String? = null

        if (isLog) {
            l.ifof("编码后url：%s", request.url.toString())
            val requestBody = request.body

            if (requestBody != null) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)

                var charset = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                body = buffer.readString(charset)
            }
            l.ifof(
                "发送请求\nmethod：%s\nurl：%s\nheaders: %s",
                request.method, rUrl, request.headers.toString()
            )
            parseBody(body, rUrl, "body")
        }

        val response = chain.proceed(request)
        if (isLog) {
            val startNs = System.nanoTime()

            val responseBody = response.body
            var rBody: String? = null

            if (response.promisesBody()) {
                val source = responseBody!!.source()
                source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                val buffer = source.buffer()

                var charset = UTF8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8)
                    } catch (e: UnsupportedCharsetException) {
                        e.printStackTrace()
                    }

                }
                rBody = buffer.clone().readString(charset)
            }
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

            l.ifof(
                "收到响应 %s%s %ss\n请求url：%s",
                response.code.toString(), response.message, tookMs, rUrl
            )

            parseBody(body, rUrl, "请求body")
            if (rBody.isNullOrEmpty()) {
                l.ifof("响应body(%s)：%s", rUrl, rBody)
            } else {
                l.ifof(
                    "响应body(%s)：%s", rUrl, if (rBody.length > 1024) {
                        rBody.substring(0, 1010) + "...数据超长省略"
                    } else {
                        rBody
                    }
                )
            }
            if (save && httpResDir.isNotEmpty()) {
                try {
                    val httpResFilePath =
                        httpResDir + TimeFormat.getCurrentTime("yyyyMMdd") + "/" + TimeFormat.getCurrentTime(
                            "yyyyMMddHHmmss"
                        ) + "-" + UUID.randomUUID() + ".log"
                    val httpResFile = File(httpResFilePath)
                    httpResFile.parentFile?.apply {
                        if (!exists()) {
                            mkdirs()
                        }
                        httpResFile.createNewFile()
                    }
                    httpResFile.createNewFile()
                    val allRes = String.format(
                        "记录时间:%s\nurl:\n%s\n\n请求方式:\n%s\n\n请求头:\n%s\n\n响应code:\n%d\n\n请求body:\n%s\n\n响应body:\n%s",
                        TimeFormat.getCurrentTime(),
                        rUrl,
                        request.method,
                        request.headers.toString(),
                        response.code,
                        body ?: "[null]",
                        rBody ?: "[null]"
                    )
                    saveFile(allRes, httpResFilePath)
                    l.i("请求结果保存在[${httpResFilePath}]")
                } catch (e: Exception) {
                    l.e(e.message)
                }
            }
        }
        return response
    }

    private fun parseBody(body: String?, rUrl: String, tag: String) {
        if (body != null && body.length > 1024) {
            try {
                val bodyJson = JSONObject(body)
                for (key in bodyJson.keys()) {
                    val value = bodyJson[key].toString()
                    if (value.length > 1024) {
                        l.ifo("$tag(%s)：%s>>%s", rUrl, key, value.substring(0, 1010) + "...数据超长省略")
                    } else {
                        l.ifo("$tag(%s)：%s>>%s", rUrl, key, value)
                    }
                }
            } catch (e: Exception) {
                l.ifof("$tag(%s)：%s", rUrl, body)
            }
        } else {
            l.ifof("$tag(%s)：%s", rUrl, body)
        }
    }

}