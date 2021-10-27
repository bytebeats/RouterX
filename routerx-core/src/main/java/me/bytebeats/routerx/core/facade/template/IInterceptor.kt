package me.bytebeats.routerx.core.facade.template

import me.bytebeats.routerx.core.facade.Postcard
import me.bytebeats.routerx.core.facade.callback.InterceptorCallback

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 15:59
 * @Version 1.0
 * @Description 路由拦截器，在路由导航时可注入一些自定义逻辑
 */

interface IInterceptor : IProvider {
    /**
     * 拦截器的执行操作
     *
     * @param postcard 路由信息
     * @param callback 拦截回调
     */
    fun process(postcard: Postcard, callback: InterceptorCallback)
}