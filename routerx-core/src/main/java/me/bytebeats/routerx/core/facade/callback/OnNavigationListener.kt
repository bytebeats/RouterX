package me.bytebeats.routerx.core.facade.callback

import me.bytebeats.routerx.core.facade.Postcard

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 17:06
 * @Version 1.0
 * @Description 执行navigation（导航）时的回调
 */

interface OnNavigationListener {
    /**
     * 发现导航目标的回调
     *
     * @param postcard 路由信息
     */
    fun onFound(postcard: Postcard)

    /**
     * 路由丢失（找不到）的回调
     *
     * @param postcard 路由信息
     */
    fun onLost(postcard: Postcard)

    /**
     * 导航到达的回调
     *
     * @param postcard 路由信息
     */
    fun onArrived(postcard: Postcard)

    /**
     * 被拦截的回调
     *
     * @param postcard 路由信息
     */
    fun onInterrupted(postcard: Postcard)
}