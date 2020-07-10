package viz.commonlib.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * @author wei
 */
class MyObserver(var lifecycle: Lifecycle, var className: String) :
    LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun ON_CREATE() {
        val var1 = className + "@@@@@@@@MyObserver:ON_CREATE"
        val var2 = false
        println(var1)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun ON_START() {
        if (lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        ) {
            val var1 = className + "@@@@@@@@MyObserver:ON_START"
            val var2 = false
            println(var1)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun ON_RESUME() {
        if (lifecycle.currentState
                .isAtLeast(Lifecycle.State.RESUMED)
        ) {
            val var1 = className + "@@@@@@@@MyObserver:ON_RESUME"
            val var2 = false
            println(var1)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun ON_PAUSE() {
        if (lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        ) {
            val var1 = className + "@@@@@@@@MyObserver:ON_PAUSE"
            val var2 = false
            println(var1)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun ON_STOP() {
        if (lifecycle.currentState
                .isAtLeast(Lifecycle.State.CREATED)
        ) {
            val var1 = className + "@@@@@@@@MyObserver:ON_STOP"
            val var2 = false
            println(var1)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun ON_DESTROY() {
        if (lifecycle.currentState
                .isAtLeast(Lifecycle.State.DESTROYED)
        ) {
            val var1 = className + "@@@@@@@@MyObserver:ON_DESTROY"
            val var2 = false
            println(var1)
        }
    }

    init {
        lifecycle.addObserver((this as LifecycleObserver))
    }
}