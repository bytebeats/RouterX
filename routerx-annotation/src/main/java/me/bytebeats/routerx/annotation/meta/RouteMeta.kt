package me.bytebeats.routerx.annotation.meta

import me.bytebeats.routerx.annotation.Router
import me.bytebeats.routerx.annotation.enums.TargetType
import javax.lang.model.element.Element

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/10/25 20:27
 * @Version 1.0
 * @Description 编译期路由元信息
 */
data class RouteMeta internal constructor(
    /* 路由的目标类型 */
    val type: TargetType?,
    /* raw type of route */
    val rawType: Element?,
    /* 路由目标类 */
    val destination: Class<*>?,
    /* 路由路径 */
    val path: String,
    /* 路由所在的组名 */
    var group: String?,
    /* 路由的优先级【数字越小，优先级越高】 */
    val priority: Int = -1,
    /* 拓展属性 */
    val extras: Int,
    /* 被{@link AutoWired}所有字段的类型集合【key为参数的key，value为参数的类型 {@link DataType}】 */
    val paramsType: MutableMap<String, Int>?
) {
    companion object {
        /**
         * 构建路由信息（RouterProcessor自动构造使用）
         */
        fun build(
            type: TargetType,
            destination: Class<*>,
            path: String,
            group: String,
            priority: Int,
            extras: Int,
            paramsType: MutableMap<String, Int>?,
        ): RouteMeta = RouteMeta(
            type = type,
            rawType = null,
            destination = destination,
            path = path,
            group = group,
            priority = priority,
            extras = extras,
            paramsType = paramsType
        )

        /**
         * 构建路由信息（RouterProcessor自动构造使用）
         */
        fun build(
            router: Router,
            rawType: Element?,
            type: TargetType,
            paramsType: MutableMap<String, Int>?
        ): RouteMeta = RouteMeta(
            type = type,
            rawType = rawType,
            destination = null,
            path = router.path,
            group = router.group,
            priority = router.priority,
            extras = router.extras,
            paramsType = paramsType
        )
    }
}
