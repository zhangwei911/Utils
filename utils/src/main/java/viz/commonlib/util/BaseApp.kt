package viz.commonlib.util

import android.annotation.SuppressLint
import android.app.Application
import bolts.Task
import com.facebook.common.logging.FLog
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.facebook.imagepipeline.listener.RequestListener
import com.facebook.imagepipeline.listener.RequestLoggingListener
import com.viz.tools.l


@SuppressLint("Registered")
open class BaseApp : Application() {
    var TOKEN = ""
    var DEVICE = ""
    var VERSION = "1"

    fun initFresco() {
        Task.callInBackground {
            l.start("fresco")
            val requestListeners = mutableSetOf<RequestListener>()
            requestListeners.add(RequestLoggingListener())
            val progressiveJpegConfig = SimpleProgressiveJpegConfig()
            val config = ImagePipelineConfig.newBuilder(this)
//            .setBitmapMemoryCacheParamsSupplier(bitmapCacheParamsSupplier)
//            .setCacheKeyFactory(cacheKeyFactory)
                .setDownsampleEnabled(true)
//            .setWebpSupportEnabled(true)
//            .setEncodedMemoryCacheParamsSupplier(encodedCacheParamsSupplier)
//            .setExecutorSupplier(executorSupplier)
//            .setImageCacheStatsTracker(imageCacheStatsTracker)
//            .setMainDiskCacheConfig(mainDiskCacheConfig)
//            .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
//            .setNetworkFetchProducer(networkFetchProducer)
//            .setPoolFactory(poolFactory)
                .setNetworkFetcher(OkHttpNetworkFetcher(this))
                .setProgressiveJpegConfig(progressiveJpegConfig)
                .setRequestListeners(requestListeners)
//            .setSmallImageDiskCacheConfig(smallImageDiskCacheConfig)
                .build()
            Fresco.initialize(this, config)
            FLog.setMinimumLoggingLevel(FLog.VERBOSE)
            l.end("fresco")
        }.continueWithEnd("初始化Fresco")
    }
}