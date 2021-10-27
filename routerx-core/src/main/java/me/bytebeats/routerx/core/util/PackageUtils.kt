package me.bytebeats.routerx.core.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 10:53
 * @Version 1.0
 * @Description Package Utils
 */
private var NEW_VERSION_NAME: String = ""
private var NEW_VERSION_CODE: Int = 0

/**
 * 当前应用是否是新版本
 * @param context
 * @return
 */
fun isLatestVersion(context: Context): Boolean {
    val packageInfo = packageInfo(context)
    return if (packageInfo != null) {
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.versionCode
        val sp = context.getSharedPreferences(ROUTERX_SP_CACHE_KEY, Context.MODE_PRIVATE)
        if (versionName != sp.getString(LAST_VERSION_NAME, null)
            || versionCode != sp.getInt(LAST_VERSION_CODE, -1)) {
            NEW_VERSION_NAME = versionName
            NEW_VERSION_CODE = versionCode
            true
        } else {
            false
        }
    } else true
}

/**
 * 更新版本信息
 * @param context
 */
fun updateVersion(context: Context) {
    if (NEW_VERSION_NAME.isNotEmpty() && NEW_VERSION_CODE != 0) {
        context.getSharedPreferences(ROUTERX_SP_CACHE_KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(LAST_VERSION_NAME, NEW_VERSION_NAME)
            .putInt(LAST_VERSION_CODE, NEW_VERSION_CODE)
            .apply()
    }
}

private fun packageInfo(context: Context): PackageInfo? = try {
    context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_CONFIGURATIONS)
} catch (ex: Exception) {
    null
}