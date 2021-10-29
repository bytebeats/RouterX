package me.bytebeats.routerx.core

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import me.bytebeats.routerx.annotation.enums.TargetType
import me.bytebeats.routerx.core.concurrency.XPoolExecutor
import me.bytebeats.routerx.core.exception.HandlerException
import me.bytebeats.routerx.core.exception.InitializationException
import me.bytebeats.routerx.core.exception.RouteNotFoundException
import me.bytebeats.routerx.core.facade.Postcard
import me.bytebeats.routerx.core.facade.callback.InterceptorCallback
import me.bytebeats.routerx.core.facade.callback.OnNavigationListener
import me.bytebeats.routerx.core.facade.service.AutoWiredService
import me.bytebeats.routerx.core.facade.service.DemoteService
import me.bytebeats.routerx.core.facade.service.InterceptorService
import me.bytebeats.routerx.core.facade.service.PathReplaceService
import me.bytebeats.routerx.core.logger.ILogger
import me.bytebeats.routerx.core.logger.RXLog
import me.bytebeats.routerx.core.runtime.LogisticsCenter
import me.bytebeats.routerx.core.util.*
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
 * @Description RouterX 核心功能
 */

internal class _RouterX private constructor() {

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

    /**
     * 真正执行导航的方法
     *
     * @param context     ComponentActivity or null
     * @param postcard    路由容器
     * @param requestCode 请求code
     * @param listener    导航回调
     * @return
     */
    @JvmOverloads
    internal fun navigation(
        context: Context?,
        postcard: Postcard?,
        requestCode: Int = 0,
        listener: OnNavigationListener? = null
    ): Any? {
        val inUseCtx = context ?: mContext.get()
        if (inUseCtx != null && postcard != null) {
            try {
                LogisticsCenter.completion(postcard)
            } catch (exp: RouteNotFoundException) {
                RXLog.e(exp)
                handleRouteNotFoundException(postcard, listener, inUseCtx)
                return null
            }
            listener?.onFound(postcard)

            if (postcard.greenChannel) {
                return _navigation(context, postcard, requestCode, listener)
            } else {// It must be run in async thread, maybe interceptor cost too much time made ANR.
                interceptorService?.doIntercept(postcard, object : InterceptorCallback {
                    override fun onContinue(postcard: Postcard) {
                        _navigation(context, postcard, requestCode, listener)
                    }

                    override fun onInterrupted(exp: Exception) {
                        listener?.onInterrupted(postcard)
                        RXLog.i("Navigation failed, termination by interceptor : ${exp.message}")
                    }
                })
            }
        }
        return null
    }

    /**
     * 真正执行导航的方法[Fragment]
     *
     * @param context     ComponentActivity or null
     * @param postcard    路由容器
     * @param requestCode 请求code
     * @param listener    导航回调
     * @return
     */
    private fun _navigation(
        context: Context?,
        postcard: Postcard?,
        requestCode: Int = 0,
        listener: OnNavigationListener? = null
    ): Any? {
        if (postcard != null) {
            val inUseCtx = context ?: mContext.get()
            if (inUseCtx != null) {
                when (postcard.type) {
                    TargetType.ACTIVITY -> {
                        val intent = buildIntent(inUseCtx, postcard)

                        // Navigation in main looper.
                        mMainHandler.post {
                            if (requestCode > 0 && inUseCtx is ComponentActivity) {// Need start for result
                                ActivityCompat.startActivityForResult(
                                    inUseCtx,
                                    intent,
                                    requestCode,
                                    postcard.optionsCompat
                                )
                            } else {
                                ActivityCompat.startActivity(inUseCtx, intent, postcard.optionsCompat)
                            }
                            if (postcard.enterAnim > 0 && postcard.exitAnim > 0 && inUseCtx is ComponentActivity) {
                                inUseCtx.overridePendingTransition(postcard.enterAnim, postcard.exitAnim)
                            }
                            listener?.onArrived(postcard)// Navigation over.
                        }
                    }
                    TargetType.PROVIDER -> {
                        return postcard.provider
                    }
                    TargetType.RECEIVER, TargetType.CONTENT_PROVIDER, TargetType.FRAGMENT -> {
                        return obtainComponent(postcard)
                    }
                    else -> {
                    }
                }
            }
        }
        return null
    }

    /**
     * 进行路由导航 [Fragment]
     *
     * @param fragment    Fragment
     * @param postcard    路由容器
     * @param requestCode 请求code
     * @param listener    导航回调
     * @return
     */
    @JvmOverloads
    internal fun navigation(
        fragment: Fragment,
        postcard: Postcard?,
        requestCode: Int = 0,
        listener: OnNavigationListener? = null
    ): Any? {
        if (postcard != null) {
            try {
                LogisticsCenter.completion(postcard)
            } catch (exp: RouteNotFoundException) {
                RXLog.e(exp)
                handleRouteNotFoundException(postcard, listener, fragment.requireContext())
                return null
            }
            listener?.onFound(postcard)

            if (postcard.greenChannel) {
                return _navigation(fragment, postcard, requestCode, listener)
            } else {// It must be run in async thread, maybe interceptor cost too much time made ANR.
                interceptorService?.doIntercept(postcard, object : InterceptorCallback {
                    override fun onContinue(postcard: Postcard) {
                        _navigation(fragment, postcard, requestCode, listener)
                    }

                    override fun onInterrupted(exp: Exception) {
                        listener?.onInterrupted(postcard)
                        RXLog.i("Navigation failed, termination by interceptor : ${exp.message}")
                    }
                })
            }
        }
        return null
    }

    /**
     * 真正执行导航的方法
     *
     * @param fragment    Fragment
     * @param postcard    路由容器
     * @param requestCode 请求code
     * @param listener    导航回调
     * @return
     */
    private fun _navigation(
        fragment: Fragment,
        postcard: Postcard?,
        requestCode: Int = 0,
        listener: OnNavigationListener? = null
    ): Any? {
        if (postcard != null) {
            val inUseCtx = fragment.requireActivity()
            when (postcard.type) {
                TargetType.ACTIVITY -> {
                    val intent = buildIntent(inUseCtx, postcard)

                    // Navigation in main looper.
                    mMainHandler.post {
                        if (requestCode > 0) {// Need start for result
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                fragment.startActivityForResult(
                                    intent,
                                    requestCode
                                )
                            } else {
                                fragment.startActivityForResult(
                                    intent,
                                    requestCode,
                                    postcard.optionsCompat
                                )
                            }
                        } else {
                            ActivityCompat.startActivity(inUseCtx, intent, postcard.optionsCompat)
                        }
                        if (postcard.enterAnim > 0 && postcard.exitAnim > 0) {
                            inUseCtx.overridePendingTransition(postcard.enterAnim, postcard.exitAnim)
                        }
                        listener?.onArrived(postcard)// Navigation over.
                    }
                }
                TargetType.PROVIDER -> {
                    return postcard.provider
                }
                TargetType.RECEIVER, TargetType.CONTENT_PROVIDER, TargetType.FRAGMENT -> {
                    return obtainComponent(postcard)
                }
                else -> {
                }
            }
        }
        return null
    }

    /**
     * 构建intent
     *
     * @param context
     * @param postcard
     * @return
     */
    private fun buildIntent(context: Context, postcard: Postcard): Intent {
        val intent = Intent(context, postcard.destination)
        postcard.bundle?.let { intent.putExtras(it) }
        /*  set flags for intent  */
        val flags = postcard.flags
        if (flags != -1) {
            intent.flags = flags
        } else if (context !is ComponentActivity) {// Non activity, need less one flag
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        /*  set action  */
        val action = postcard.action
        if (!action.isNullOrEmpty()) {
            intent.action = action
        }
        return intent
    }

    /**
     * obtain Class<out Fragment>
     */
    private fun obtainComponent(postcard: Postcard): Any? {
        return try {
            val componentClass = postcard.destination
            val componentInstance = componentClass?.getConstructor()?.newInstance()
            if (componentInstance is Fragment) {
                componentInstance.arguments = postcard.bundle
            }
            componentInstance
        } catch (exp: Exception) {
            RXLog.e("Fetch fragment instance error, ${exp.stackTrace.format()}")
            null
        }
    }

    /**
     * 处理路由丢失的错误
     *
     * @param postcard
     * @param listener
     * @param activity
     */
    private fun handleRouteNotFoundException(
        postcard: Postcard,
        listener: OnNavigationListener?,
        activity: Context
    ) {
        if (debuggable()) {
            val tips = "There's no route matched!\n Path = [${postcard?.path}]\nGroup = [${postcard?.group}]"
            Toast.makeText(activity, tips, Toast.LENGTH_LONG).show()
            RXLog.i(tips)
        }
        listener?.onLost(postcard) ?: run {
            navigation(DemoteService::class.java)?.onLost(activity, postcard)
        }
    }

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

        /**
         * Trigger interceptor init, use byName.
         */
        internal fun afterInitialized() {
            interceptorService = getInstance().build(ROUTE_SERVICE_INTERCEPTORS)?.navigation() as InterceptorService
        }

        /**
         * Destroy RouterX, it can be used only in debug mode.
         */
        @Synchronized
        internal fun destroy() {
            if (debuggable()) {
                isInitialized = false
                LogisticsCenter.suspend()
                RXLog.i("RouterX destroy success!")
            } else {
                RXLog.e("Destroy can be used in debug mode only!")
            }
        }
    }
}