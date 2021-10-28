package me.bytebeats.routerx.core.facade.template

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:08
 * @Version 1.0
 * @Description 路由的根元素
 */

interface IRouteRoot {
    /**
     * 加载路由组元素
     */
    fun loadInfo(routes: Map<String, Class<out IRouteGroup>>)
}