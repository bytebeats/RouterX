package me.bytebeats.routerx.core.facade.service

import me.bytebeats.routerx.core.facade.Postcard
import me.bytebeats.routerx.core.facade.callback.InterceptorCallback
import me.bytebeats.routerx.core.facade.template.IProvider

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:11
 * @Version 1.0
 * @Description 拦截服务
 */

interface InterceptorService : IProvider {
    /**
     * 执行拦截操作
     */
    fun doIntercept(postcard: Postcard, callback: InterceptorCallback)
}