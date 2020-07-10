package viz.commonlib.util

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference

open class BaseHandler<T>(p: T) : Handler(Looper.getMainLooper()) {
    private var wr: WeakReference<T>? = null
    var p:T? = null

    init {
        wr = WeakReference(p)
        this.p = wr!!.get()
    }
}