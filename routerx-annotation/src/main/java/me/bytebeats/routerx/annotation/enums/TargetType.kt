package me.bytebeats.routerx.annotation.enums

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/25 19:16
 * @Version 1.0
 * @Description 路由的目标类型
 * @param id index
 * @param className class name of route target
 */
enum class TargetType(val id: Int, val className: String) {
    ACTIVITY(0, "androidx.activity.ComponentActivity"),
    SERVICE(1, "android.app.Service"),

    // TODO: 2021/10/25 finished class path of PROVIDER
    PROVIDER(2, "me.bytebeats.routerx.facade.template.IProvider"),
    CONTENT_PROVIDER(-1, "android.content.ContentProvider"),
    BROADCAST(-1, "android.content.BroadcastReceiver"),
    METHOD(-1, ""),
    FRAGMENT(-1, "androidx.fragment.app.Fragment"),
    UNKNOWN(-1, "UNKNOWN");

    companion object {
        /**
         * 根据名称解析路由类型
         * @param name class path
         * @return route TargetType
         */
        fun from(name: String): TargetType = values().firstOrNull { name == it.className } ?: UNKNOWN
    }
}