package viz.commonlib.http

import com.google.gson.GsonBuilder
import com.viz.tools.l
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

interface HttpUtil {
    companion object {
        inline fun <reified T> createHttp(
            url: String,
            debug: Boolean = false,
            interceptorList: MutableList<Interceptor> = mutableListOf<Interceptor>(),
            connectTimeout: Long = 60,
            readTimeout: Long = 60,
            writeTimeout: Long = 60,
            excludeUrls: MutableList<String> = mutableListOf(),
            httpResDir: String = "",
            save: Boolean = false,
            addGsonConverterFactory: Boolean = true
        ): T {
            val gson = GsonBuilder()
                //配置你的Gson
                .setDateFormat("yyyy-MM-dd hh:mm:ss")
                .create()
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                l.d("API", URLDecoder.decode(it, StandardCharsets.UTF_8.name()))
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC
            val builder = OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .addInterceptor(logger)
            try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts =
                    arrayOf<TrustManager>(
                        object : X509TrustManager {
                            @Throws(CertificateException::class)
                            override fun checkClientTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                            ) {
                            }

                            @Throws(CertificateException::class)
                            override fun checkServerTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                            ) {
                            }

                            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                                return null
                            }
                        }
                    )

                // Install the all-trusting trust manager
                val sslContext =
                    SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory
                builder.sslSocketFactory(sslSocketFactory, object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }).hostnameVerifier(HostnameVerifier { _, _ -> true })
            } catch (e: Exception) {
                e.printStackTrace()
            }
            interceptorList.forEach {
                builder.addInterceptor(it)
            }
            if (debug) {
                builder.addInterceptor(ResultInterceptor(httpResDir, save, excludeUrls))
            }
            val rBuilder = Retrofit.Builder()
                .baseUrl(url)
                .client(builder.build())
            rBuilder.addConverterFactory(NullOnEmptyConverterFactory())
            if (addGsonConverterFactory) {
                rBuilder.addConverterFactory(GsonConverterFactory.create(gson))
            }
            return rBuilder
                .build().create(T::class.java)
        }
    }
}