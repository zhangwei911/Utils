package viz.commonlib.util

import android.content.Context
import android.text.TextUtils
import com.facebook.imagepipeline.image.EncodedImage
import com.facebook.imagepipeline.producers.*
import com.viz.tools.apk.PreferencesUtils
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*

class OkHttpNetworkFetcher(private val mAppContext: Context) :
    BaseNetworkFetcher<FetchState>() {
    private var mCallMap: MutableMap<String, Call>? = null

    companion object {
        private var mOkClient: OkHttpClient? = null

        init {
            mOkClient =
                OkHttpClient.Builder() //                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                    .build()
        }
    }

    override fun createFetchState(
        consumer: Consumer<EncodedImage>?,
        producerContext: ProducerContext
    ): FetchState? {
        return FetchState(consumer, producerContext)
    }

    override fun fetch(
        fetchState: FetchState,
        callback: NetworkFetcher.Callback
    ) {
        val request = buildRequest(fetchState)
        val fetchCall =
            mOkClient!!.newCall(request)
        if (mCallMap == null) {
            mCallMap = HashMap()
        }
        mCallMap!![fetchState.id] = fetchCall
        val fetchId = fetchState.id
        fetchState.context.addCallbacks(object : BaseProducerContextCallbacks() {
            override fun onCancellationRequested() {
                cancelAndRemove(fetchId)
                callback.onCancellation()
            }
        })
        try {
            val response = fetchCall.execute()
            val responseBody = response.body
            callback.onResponse(responseBody!!.byteStream(), responseBody.contentLength().toInt())
        } catch (e: IOException) {
            callback.onFailure(e)
        }
        removeCall(fetchId)
    }

    private fun removeCall(id: String) {
        if (mCallMap != null) {
            mCallMap!!.remove(id)
        }
    }

    private fun cancelAndRemove(id: String) {
        if (mCallMap != null) {
            val call = mCallMap!!.remove(id)
            call?.cancel()
        }
    }

    private fun buildRequest(fetchState: FetchState): Request {
        val dm = mAppContext.resources.displayMetrics
        val density = dm.densityDpi
        val token =
            PreferencesUtils.getString(mAppContext, "token", "")
        val builder = Request.Builder()
        if (!TextUtils.isEmpty(token)) {
            builder.addHeader("Authorization", token)
        }
        builder.addHeader("x-density", density.toString())
        return builder
            .get()
            .url(fetchState.uri.toString())
            .build()
    }

}