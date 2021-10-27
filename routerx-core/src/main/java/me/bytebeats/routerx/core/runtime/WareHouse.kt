package me.bytebeats.routerx.core.runtime

import me.bytebeats.routerx.annotation.meta.RouteMeta
import me.bytebeats.routerx.core.facade.template.IInterceptor
import me.bytebeats.routerx.core.facade.template.IProvider
import me.bytebeats.routerx.core.facade.template.IRouteGroup

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/26 11:50
 * @Version 1.0
 * @Description 存放路由信息的仓库
 */


object WareHouse {
    private const val TIPS_UNIQUE_KEY = "More than one interceptors use same priority [%s]"


    /*  Cache routes and metas  */
    internal val groupsIndex = mutableMapOf<String, Class<in IRouteGroup>>()

    /*  Cache metas  */
    internal val routes = mutableMapOf<String, RouteMeta>()

    /*  Cache provider  */
    internal val providersIndex = mutableMapOf<String, RouteMeta>()
    internal val providers = mutableMapOf<Class<*>, IProvider>()

    /*  Cache interceptor，因为使用的是TreeMap，且Key为Integer,从而实现拦截器的优先级  */
    internal val interceptorsIndex = UniqueKeyTreeMap<Int, Class<in IInterceptor>>(TIPS_UNIQUE_KEY)
    internal val interceptors = mutableListOf<IInterceptor>()

    fun clear() {
        groupsIndex.clear()
        routes.clear()
        providersIndex.clear()
        providers.clear()
        interceptorsIndex.clear()
        interceptors.clear()
    }

}