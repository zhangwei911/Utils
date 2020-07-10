package viz.commonlib.util

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresPermission

object NetUtil {
    fun getSize(size: Long): String {
        return when (size) {
            in 0 until 1024 -> {
                "${size}B"
            }
            in 1024 until 1024 * 1024 -> {
                "${String.format("%.2f", size * 1.00f / 1024)}KB"
            }
            in 1024 * 1024 until 1024 * 1024 * 1024 -> {
                "${String.format("%.2f", size * 1.00f / (1024 * 1024))}MB"
            }
            else -> {
                "${String.format(
                    "%.2f",
                    size * 1.00f / (1024 * 1024 * 1024)
                )}GB"
            }
        }
    }

    /**
     * 网络是否已连接
     *
     * @return true:已连接 false:未连接
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun iConnected(@NonNull context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities =
                    manager.getNetworkCapabilities(manager.activeNetwork)
                if (networkCapabilities != null) {
                    return (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                }
            } else {
                val networkInfo = manager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
        }
        return false
    }

    /**
     * Wifi是否已连接
     *
     * @return true:已连接 false:未连接
     */
    fun isWifiConnected(@NonNull context: Context): Boolean {
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities =
                    manager.getNetworkCapabilities(manager.activeNetwork)
                if (networkCapabilities != null) {
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                }
            } else {
                val networkInfo = manager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
            }
        }
        return false
    }

    /**
     * 是否为流量
     */
    fun isMobileData(@NonNull context: Context): Boolean {
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities =
                    manager.getNetworkCapabilities(manager.activeNetwork)
                if (networkCapabilities != null) {
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                }
            } else {
                val networkInfo = manager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_MOBILE
            }
        }
        return false
    }
}