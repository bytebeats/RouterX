package me.bytebeats.routerx.core.facade.template

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 17:17
 * @Version 1.0
 * @Description 拦截器组
 */

interface IInterceptorGroup {
    /**
     * 加载拦截器
     *
     * @param interceptors input
     */
    fun loadInto(interceptors: Map<Int, Class<in IInterceptor>>)
}