package viz.commonlib.util

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions

/**
 * @title: NavEx
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author wei
 * @date 2020-04-10 9:49
 */
fun NavController.navigate(fromId: Int, toId: Int, bundle: Bundle? = null, isSingleTop: Boolean = true){
    val bundleUse = bundle ?: Bundle()
    bundleUse.putInt("from", fromId)
    navigate(toId, bundleUse, isSingleTop)
}

fun singleTopNavOptions(): NavOptions {
    val no = NavOptions.Builder()
    no.setLaunchSingleTop(true)
    return no.build()
}

fun cleartaskNavOptions(destId: Int): NavOptions {
    val no = NavOptions.Builder()
    no.setPopUpTo(destId, true)
    return no.build()
}

fun NavController.navigate(id: Int, bundle: Bundle?, isSingleTop: Boolean) {
    if (isSingleTop) {
        navigate(id, bundle, singleTopNavOptions())
    } else {
        navigate(id, bundle)
    }
}

fun NavController.navigate(id: Int, isSingleTop: Boolean) {
    if (isSingleTop) {
        navigate(id, null, singleTopNavOptions())
    } else {
        navigate(id)
    }
}