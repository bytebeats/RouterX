package me.bytebeats.routerx.core.facade.template

import me.bytebeats.routerx.annotation.meta.RouteMeta

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:06
 * @Version 1.0
 * @Description 路由的组元素
 */

interface IRouteGroup {
    /**
     * 将路由信息填充至路由组
     */
    fun loadInfo(atlas: Map<String, RouteMeta>)
}