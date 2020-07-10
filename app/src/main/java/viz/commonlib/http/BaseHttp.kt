package viz.commonlib.http

import okhttp3.Interceptor

interface BaseHttp {
    companion object {
        inline fun <reified T> createHttp(
            url: String = "",
            commonHeader: Map<String, CommonInfo> = mapOf(),
            commonPostJson: Map<String, CommonInfo> = mapOf(),
            debug: Boolean = true,
            jsonContentType: Boolean = true,
            contentType: String = "",
            commonDataMethod: CommonInterceptor.CommonRequestType = CommonInterceptor.CommonRequestType.POST_JSON,
            excludeUrls: MutableList<String> = mutableListOf(),
            connectTimeout: Long = 15,
            readTimeout: Long = 15,
            writeTimeout: Long = 15,
            interceptorList: MutableList<Interceptor> = mutableListOf(),
            httpResDir: String = "",
            save: Boolean = false,
            addGsonConverterFactory: Boolean = true
        ): T {
            if (commonHeader.isNotEmpty()) {
                val mapUtilHeader = MapUtil<String, CommonInfo>()
                    .add("Accept", CommonInfo("application/json"))
                    .addAll(commonHeader)
                if (jsonContentType) {
                    mapUtilHeader.add("Content-Type", CommonInfo("application/json"))
                } else {
                    if (contentType.isNotEmpty()) {
                        mapUtilHeader.add("Content-Type", CommonInfo(contentType))
                    }
                }
                interceptorList.add(
                    CommonInterceptor(
                        mapUtilHeader.map
                    )
                )
            }
            if (commonPostJson.isNotEmpty()) {
                val mapUtilPost = MapUtil<String, CommonInfo>()
                    .addAll(commonPostJson)
                interceptorList.add(
                    CommonInterceptor(
                        mapUtilPost.map,
                        type = commonDataMethod
                    )
                )
            }
            return HttpUtil.createHttp<T>(
                url,
                debug,
                interceptorList,
                excludeUrls = excludeUrls,
                connectTimeout = connectTimeout,
                readTimeout = readTimeout,
                writeTimeout = writeTimeout,
                httpResDir = httpResDir,
                save = save,
                addGsonConverterFactory = addGsonConverterFactory
            )
        }
    }
}