package me.bytebeats.routerx.core

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import me.bytebeats.routerx.core.concurrency.XPoolExecutor
import me.bytebeats.routerx.core.exception.HandlerException
import me.bytebeats.routerx.core.exception.InitializationException
import me.bytebeats.routerx.core.exception.RouteNotFoundException
import me.bytebeats.routerx.core.facade.Postcard
import me.bytebeats.routerx.core.facade.callback.OnNavigationListener
import me.bytebeats.routerx.core.facade.service.AutoWiredService
import me.bytebeats.routerx.core.facade.service.InterceptorService
import me.bytebeats.routerx.core.facade.service.PathReplaceService
import me.bytebeats.routerx.core.logger.ILogger
import me.bytebeats.routerx.core.logger.RXLog
import me.bytebeats.routerx.core.runtime.LogisticsCenter
import me.bytebeats.routerx.core.util.ROUTE_ROOT_PACKAGE
import me.bytebeats.routerx.core.util.ROUTE_SERVICE_AUTOWIRED
import me.bytebeats.routerx.core.util.ROUTE_SERVICE_INTERCEPTORS
import me.bytebeats.routerx.core.util.TAG
import java.lang.ref.WeakReference
import java.util.concurrent.ThreadPoolExecutor

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/26 21:07
 * @Version 1.0
 * @Description XRouter 核心功能
 */

internal class _RouterX private constructor() {
    companion object {
        @Volatile
        private var monitorMode = false

        @Volatile
        private var debuggable = false

        @Volatile
        private var sInstance: _RouterX? = null

        @Volatile
        private var isInitialized = false
        private var defaultExecutor: ThreadPoolExecutor = XPoolExecutor.getInstance()
        private val mMainHandler = Handler(Looper.getMainLooper())
        private lateinit var mContext: WeakReference<Context>
        private var interceptorService: InterceptorService? = null

        internal fun init(application: Application): Boolean {
            mContext = WeakReference(application)
            LogisticsCenter.init(application, defaultExecutor)
            RXLog.i("RouterX is initialized successfully.")
            isInitialized = true

            // It's not a good idea.
            // if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            //     application.registerActivityLifecycleCallbacks(new AutowiredLifecycleCallback());
            // }

            return true
        }

        internal fun getInstance(): _RouterX {
            if (!isInitialized) {
                throw InitializationException("RouterX#init(context) is not called yet.")
            }
            if (sInstance == null) {
                synchronized(_RouterX::class.java) {
                    if (sInstance == null) {
                        sInstance = _RouterX()
                    }
                }
            }
            return sInstance!!
        }

        internal fun enableDebug() {
            debuggable = true
            RXLog.i("RouterX is able to debug")
        }

        internal fun enableLog() {
            RXLog.enableDebug(true)
            RXLog.i("RouterX is able to log")
        }

        internal fun enableMonitorMode() {
            monitorMode = true
            RXLog.i("RouterX enable monitor mode")
        }

        internal fun debuggable(): Boolean = debuggable

        internal fun monitorMode(): Boolean = monitorMode

        internal fun setExecutor(threadPool: ThreadPoolExecutor) {
            defaultExecutor = threadPool
        }

        internal fun setLogger(logger: ILogger?) {
            RXLog.setLogger(logger)
        }

        internal fun inject(target: Any) {
            (build(ROUTE_SERVICE_AUTOWIRED)?.navigation() as AutoWiredService?)?.autoWire(target)
        }

        /**
         * 通过path和default group构建路由表
         *
         * @param path 路由路径
         */
        internal fun build(path: String?): Postcard? = if (path.isNullOrEmpty()) {
            throw HandlerException(TAG + "Parameter is invalid!")
        } else {
            Postcard(
                path = navigation(PathReplaceService::class.java)?.forString(path) ?: path,
                group = extractGroup(path)
            )
        }

        /**
         * 从路由路径中抽出路由组
         *
         * @param path 路由路径
         */
        private fun extractGroup(path: String?): String? {
            if (path.isNullOrEmpty()) {
                throw HandlerException("${TAG}Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!")
            }
            return try {
                val group = path.substring(1, path.indexOf('/', 1))
                if (group.isNullOrEmpty()) {
                    throw HandlerException("${TAG}Extract the default group failed! There's nothing between 2 '/'!")
                } else {
                    group
                }
            } catch (exp: Exception) {
                null
            }
        }

        /**
         * 通过path和group构建路由表
         *
         * @param path  路由路径
         * @param group 路由组
         */
        internal fun build(path: String?, group: String?): Postcard {
            if (path.isNullOrEmpty() || group.isNullOrEmpty()) {
                throw HandlerException("${TAG}Parameter is invalid!")
            } else {
                return Postcard(path = navigation(PathReplaceService::class.java)?.forString(path) ?: path, group)
            }
        }

        /**
         * 通过uri构建路由表
         *
         * @param uri 资源路径
         */
        internal fun build(uri: Uri?): Postcard? = if (uri?.toString().isNullOrEmpty()) {
            throw HandlerException("${TAG}Parameter invalid!")
        } else {
            Postcard(
                path = navigation(PathReplaceService::class.java)?.forUri(uri)?.path ?: uri?.path,
                group = extractGroup(uri?.path),
                uri = uri,
                bundle = null
            )
        }

        /**
         * 服务发现（需要实现{@link IProvider}接口）
         *
         * @param service
         * @param Class<T>
         */
        internal fun <T> navigation(service: Class<out T>): T? = try {
            var postcard = LogisticsCenter.buildProviders(service.name)
            LogisticsCenter.completion(postcard)
            postcard?.provider as T
        } catch (exp: RouteNotFoundException) {
            RXLog.e(exp.message)
            if (debuggable() && !service.name.contains(ROUTE_ROOT_PACKAGE)) {// Show friendly tips for user
                val tips = "There's no service matched!\n service name = [${service.name}]"
                Toast.makeText(mContext.get(), tips, Toast.LENGTH_LONG).show()
                RXLog.i(tips)
            }
            null
        }

        @JvmOverloads
        private fun _navigation(
            context: Context?,
            postcard: Postcard?,
            requestCode: Int? = 0,
            listener: OnNavigationListener? = null
        ): Any? {
            return null
        }

        /**
         * Trigger interceptor init, use byName.
         */
        internal fun afterInitialized() {
            interceptorService = build(ROUTE_SERVICE_INTERCEPTORS)?.navigation() as InterceptorService
        }

        internal fun destroy() {

        }
    }
}