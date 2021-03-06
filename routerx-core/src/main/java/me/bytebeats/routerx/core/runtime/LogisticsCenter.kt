package me.bytebeats.routerx.core.runtime

import android.content.Context
import me.bytebeats.routerx.annotation.enums.DataType
import me.bytebeats.routerx.annotation.enums.TargetType
import me.bytebeats.routerx.core.RouterX
import me.bytebeats.routerx.core.exception.HandlerException
import me.bytebeats.routerx.core.exception.RouteNotFoundException
import me.bytebeats.routerx.core.facade.Postcard
import me.bytebeats.routerx.core.facade.template.IInterceptorGroup
import me.bytebeats.routerx.core.facade.template.IProvider
import me.bytebeats.routerx.core.facade.template.IProviderGroup
import me.bytebeats.routerx.core.facade.template.IRouteRoot
import me.bytebeats.routerx.core.logger.RXLog
import me.bytebeats.routerx.core.util.*
import me.bytebeats.routerx.core.util.ROUTERX_SP_CACHE_KEY
import me.bytebeats.routerx.core.util.ROUTE_ROOT_PACKAGE
import me.bytebeats.routerx.core.util.TAG
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import kotlin.jvm.Throws
import kotlin.properties.Delegates

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/28 10:37
 * @Version 1.0
 * @Description 路由中心，存放并操作所有的路由信息
 * <p>实质是操作{@link Warehouse}路由信息仓库的中心，类似物流中心和物流仓库的关系</p>
 */

object LogisticsCenter {
    private lateinit var mContext: WeakReference<Context>
    internal lateinit var mExecutor: ThreadPoolExecutor
    private var mRegisteredByPlugin by Delegates.notNull<Boolean>()

    /**
     * routerx-plugin 将自动生成代码到该方法进行路由信息的注册
     * 该方法注册所有的路由信息、拦截器、接口服务
     */
    private fun loadRouterMap() {
        mRegisteredByPlugin = false
        //auto generate register code by gradle plugin: routerx-plugin
        // looks like below:
        // register(new RouterX..Root..modulejava());
        // register(new RouterX..Root..modulekotlin());
    }

    /**
     * 标记是通过 routerx-plugin 进行注册
     */
    private fun markRegisteredByPlugin() {
        if (!mRegisteredByPlugin) {
            mRegisteredByPlugin = true
        }
    }

    /**
     * 供routerx-plugin注册路由信息的方法
     * @param routeRoot {@link IRouteRoot} implementation class in the package: me.bytebeats.routerx.rooters
     */
    private fun registerRooteRoot(rooteRoot: IRouteRoot?) {
        markRegisteredByPlugin()
        rooteRoot?.loadInfo(WareHouse.groupsIndex)
    }

    /**
     * 供routerx-plugin注册拦截器的方法
     * @param interceptorGroup {@link IInterceptorGroup} implementation class in the package: me.bytebeats.routerx.rooters
     */
    private fun registerInterceptors(interceptorGroup: IInterceptorGroup?) {
        markRegisteredByPlugin()
        interceptorGroup?.loadInto(WareHouse.interceptorsIndex)
    }

    /**
     * 供routerx-plugin注册服务提供者的方法
     * @param providerGroup {@link IProviderGroup} implementation class in the package: me.bytebeats.routerx.rooters
     */
    private fun registerProviders(providerGroup: IProviderGroup?) {
        markRegisteredByPlugin()
        providerGroup?.loadInto(WareHouse.providersIndex)
    }

    /**
     * 通过服务名构建Provider
     *
     * @param serviceName 服务名
     * @return Postcard
     */
    fun buildProviders(serviceName: String): Postcard? {
        val meta = WareHouse.providersIndex[serviceName] ?: return null
        return Postcard(path = meta.path, group = meta.group)
    }

    @Synchronized
    @Throws(HandlerException::class)
    fun init(context: Context, executor: ThreadPoolExecutor) {
        mContext = WeakReference(context)
        mExecutor = executor
        try {
            var startTimeMillis = System.currentTimeMillis()
            loadRouterMap()
            if (mRegisteredByPlugin) {
                RXLog.i("Load router map by routerx-plugin.")
            } else {// 非routerx-plugin加载，就扫描"me.bytebeats.core.routes"包手动加载路由信息表
                val routerMap: Set<String>
                // 只有当应用是调试模式或者是新版本时，才会加载路由表到内存中
                if (RouterX.debuggable() || isLatestVersion(context)) {
                    RXLog.i("Run with debug mode or new install, rebuild router map.")
                    // These class was generated by routerx-compiler.
                    routerMap = fileNamesByPackageName(context, ROUTE_ROOT_PACKAGE)
                    if (routerMap.isNotEmpty()) {
                        // 将扫描到的路由表的class文件名储存在SP中，下次进来直接从SP里面读
                        context.getSharedPreferences(ROUTERX_SP_CACHE_KEY, Context.MODE_PRIVATE)
                            .edit()
                            .putStringSet(ROUTEX_SP_KEY_MAP, routerMap)
                            .apply()
                    }
                    updateVersion(context)
                } else {
                    RXLog.i("Load router map from cache[SP].")
                    routerMap = context.getSharedPreferences(ROUTERX_SP_CACHE_KEY, Context.MODE_PRIVATE)
                        .getStringSet(ROUTEX_SP_KEY_MAP, mutableSetOf())!!
                }

                RXLog.i("Find router map finished, map size = ${routerMap.size}, cost ${(System.currentTimeMillis() - startTimeMillis)} ms.")
                startTimeMillis = System.currentTimeMillis()

                for (className in routerMap) {
                    if (className.startsWith("$ROUTE_ROOT_PACKAGE$DOT$SDK_NAME$SEPARATOR$SUFFIX_ROOT")) {
                        //me.bytebeats.routerx.core.XRouter$$Root
                        // This one of root elements, load root
                        (Class.forName(className).getConstructor()
                            .newInstance() as IRouteRoot).loadInfo(WareHouse.groupsIndex)
                    } else if (className.startsWith("$ROUTE_ROOT_PACKAGE$DOT$SDK_NAME$SEPARATOR$SUFFIX_INTERCEPTORS")) {
                        //me.bytebeats.routerx.core.XRouter$$Interceptors
                        // Load interceptorMeta
                        (Class.forName(className).getConstructor()
                            .newInstance() as IInterceptorGroup).loadInto(WareHouse.interceptorsIndex)
                    } else if (className.startsWith("$ROUTE_ROOT_PACKAGE$DOT$SDK_NAME$SEPARATOR$SUFFIX_PROVIDERS")) {
                        //me.bytebeats.routerx.core.XRouter$$Providers
                        // Load providerIndex
                        (Class.forName(className).getConstructor()
                            .newInstance() as IProviderGroup).loadInto(WareHouse.providersIndex)
                    }
                }

                RXLog.i("Load root element finished, cost ${(System.currentTimeMillis() - startTimeMillis)} ms.")

                if (WareHouse.groupsIndex.isEmpty()) {
                    RXLog.e("No mapping files were found, check your configuration please!")
                }
                if (RouterX.debuggable()) {
                    RXLog.d(
                        "LogisticsCenter has already been loaded, GroupIndex[%d], InterceptorIndex[%d], ProviderIndex[%d]".format(
                            Locale.getDefault(),
                            WareHouse.groupsIndex.size,
                            WareHouse.interceptorsIndex.size,
                            WareHouse.providersIndex.size
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw HandlerException("${TAG}XRouter init logistics center exception! [${e.message}]")
        }
    }

    /**
     * 通过存放在内存中的路由表信息组装postcard【加载路由表（将路由组IRouteGroup里的路由信息加入到路由表中）（初始化IProvider并加入到路由表中）】
     *
     * @param postcard Incomplete postcard, should complete by this method.
     */
    @Synchronized
    fun completion(postcard: Postcard?) {
        postcard ?: throw RouteNotFoundException("$TAG Postcard was not found!")
        val meta = WareHouse.routes[postcard.path]
        if (meta == null) {// 路由信息不存在内存中的话，先加载
            val groupClass = WareHouse.groupsIndex[postcard.group]// Load routeInfo
            if (groupClass == null) {
                throw RouteNotFoundException("${TAG}There is no route match the path [${postcard.path}], in group [${postcard.group}]")
            } else {
                // Load route and cache it into memory, then delete it.
                try {
                    if (RouterX.debuggable()) {
                        RXLog.d(
                            "The group [%s] starts loading, trigger by [%s]".format(
                                Locale.getDefault(),
                                postcard.group,
                                postcard.path
                            )
                        )
                    }
                    // 将路由组里的路由信息加载至内存中，然后从内存中删除路由组
                    val groupInstance = groupClass.getConstructor().newInstance()
                    groupInstance.loadInfo(WareHouse.routes)
                    WareHouse.groupsIndex.remove(postcard.group)
                    if (RouterX.debuggable()) {
                        RXLog.d(
                            "The group [%s] has already been loaded, trigger by [%s]".format(
                                Locale.getDefault(),
                                postcard.group,
                                postcard.path
                            )
                        )
                    }
                } catch (exp: Exception) {
                    throw HandlerException("${TAG}Fatal exception when loading group info. [${exp.message}]")
                }
                completion(postcard)//Reload
            }
        } else {
            //build postcard
            postcard.destination = meta.destination
            postcard.type = meta.type
            postcard.priority = meta.priority
            postcard.extras = meta.extras

            val rawUri = postcard.uri
            if (rawUri != null) {// Try to set params into bundle.
                val params = rawUri.transformToMap()
                val paramsType = meta.paramsType
                paramsType?.let {
                    it.forEach { entry ->// Set value by its type, just for params which annotation by @Param
                        inflate(postcard, entry.value, entry.key, params[entry.key])
                    }
                    // Save params name which need auto inject.
                    postcard.bundle?.putStringArray(RouterX.AUTO_INJECT, it.keys.toTypedArray())
                }
                postcard.withString(RouterX.RAW_URI, rawUri.toString())
            }

            when (meta.type) {
                TargetType.PROVIDER -> {// 如果路由是服务接口IProvider，就需要找到他的实例
                    // Its provider, so it must implement IProvider
                    val providerClass = meta.destination as Class<out IProvider>
                    var providerInstance = WareHouse.providers[providerClass]
                    if (providerInstance == null) {
                        try {
                            val provider = providerClass.getConstructor().newInstance()
                            mContext.get()?.let { provider.init(it) }
                            WareHouse.providers[providerClass] = provider
                            providerInstance = provider
                        } catch (exp: Exception) {
                            throw HandlerException("Init provider failed! ${exp.message}")
                        }
                    }
                    postcard.provider = providerInstance
                    postcard.greenChannel = true // Provider should skip all of interceptors
                }
                TargetType.FRAGMENT -> {
                    postcard.greenChannel = true // Fragment should skip all of interceptors
                }
                else -> {
                    //do nothing here
                }
            }
        }
    }

    /**
     * 根据类型设置字段值
     *
     * @param postcard postcard
     * @param typeDef  type 字段类型
     * @param key      key
     * @param value    value
     */
    private fun inflate(postcard: Postcard, typeDef: Int?, key: String?, value: String?) {
        if (key.isNullOrEmpty() || value.isNullOrEmpty()) {
            return
        }
        try {
            when (typeDef) {
                DataType.BOOLEAN.ordinal -> postcard.withBoolean(key, value.toBoolean())
                DataType.BYTE.ordinal -> postcard.withByte(key, value.toByte())
                DataType.SHORT.ordinal -> postcard.withShort(key, value.toShort())
                DataType.INT.ordinal -> postcard.withInt(key, value.toInt())
                DataType.LONG.ordinal -> postcard.withLong(key, value.toLong())
                DataType.FLOAT.ordinal -> postcard.withFloat(key, value.toFloat())
                DataType.DOUBLE.ordinal -> postcard.withDouble(key, value.toDouble())
                DataType.STRING.ordinal -> postcard.withString(key, value)
                // TODO: 2021/10/28 how to handle Parcelable and String?
//                DataType.PARCELABLE.ordinal -> postcard.withParcelable(key, value)
                DataType.ANY.ordinal -> postcard.withString(key, value)
                else -> postcard.withString(key, value)
            }
        } catch (ignore: Exception) {
            RXLog.e("LogisticsCenter setValue failed! ${ignore.message}", ignore)
        }
    }

    /**
     * 暂停业务, 清理缓存.
     */
    fun suspend() {
        WareHouse.clear()
    }
}