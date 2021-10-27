package me.bytebeats.routerx.core.facade.callback

import me.bytebeats.routerx.core.facade.Postcard

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 16:05
 * @Version 1.0
 * @Description 拦截器的回调
 */

interface InterceptorCallback {
    /**
     * 继续执行下一个拦截器
     *
     * @param postcard 路由信息
     */
    fun onContinue(postcard: Postcard)

    /**
     * 拦截中断, 当该方法执行后，通道将会被销毁
     *
     * @param exp 中断的原因.
     */
    fun onInterrupted(exp: Exception)
}