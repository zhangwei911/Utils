package viz.commonlib.http

/**
 * @title: NullOnEmptyConverterFactory
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author zhangwei
 * @date 2020/5/29 11:34
 */
import okhttp3.ResponseBody
import retrofit2.Converter

import retrofit2.Retrofit
import java.lang.reflect.Type

class NullOnEmptyConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val delegate: Converter<ResponseBody, *> =
            retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
        return Converter<ResponseBody, Any> { body ->
            val contentLength = body.contentLength()
            if (contentLength == 0L) {
                null
            } else delegate.convert(body)
        }
    }
}