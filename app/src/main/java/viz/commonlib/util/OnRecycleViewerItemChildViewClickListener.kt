package viz.commonlib.util

import android.view.View

interface OnRecycleViewerItemChildViewClickListener<T> {
    fun onChildViewClick(view: View, data: T, position: Int)
}