package me.bytebeats.routerx.core

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.fragment.app.Fragment
import me.bytebeats.routerx.core.exception.InitializationException
import me.bytebeats.routerx.core.facade.Postcard
import me.bytebeats.routerx.core.facade.callback.OnNavigationListener
import me.bytebeats.routerx.core.logger.ILogger
import me.bytebeats.routerx.core.logger.RXLog
import java.util.concurrent.ThreadPoolExecutor


/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/26 11:50
 * @Version 1.0
 * @Description XRouter对外统一的API
 */

class RouterX private constructor() {

    /**
     * 注入参数和服务
     */
    fun inject(target: Any) {
        _RouterX.getInstance().inject(target)
    }

    /**
     * 构建一个路由表, draw a postcard.
     *
     * @param path Where you go.
     */
    fun build(path: String?): Postcard? = _RouterX.getInstance().build(path)

    /**
     * 构建一个路由表, draw a postcard.
     *
     * @param url the path
     */
    fun build(url: Uri?): Postcard? = _RouterX.getInstance().build(url)

    /**
     * 获取服务/服务发现
     *
     * @param service interface of service
     * @param <T>     return type
     * @return instance of service
    </T> */
    fun <T> navigation(service: Class<out T>): T? = _RouterX.getInstance().navigation(service)

    /**
     * 启动路由导航
     *
     * @param context
     * @param postcard
     * @param requestCode Set for startActivityForResult
     * @param listener    路由导航回调
     */
    fun navigation(context: Context, postcard: Postcard, requestCode: Int, listener: OnNavigationListener?): Any? {
        return _RouterX.getInstance().navigation(context, postcard, requestCode, listener)
    }

    /**
     * 启动路由导航
     *
     * @param fragment
     * @param postcard
     * @param requestCode Set for startActivityForResult
     * @param listener    路由导航回调
     */
    fun navigation(fragment: Fragment, postcard: Postcard, requestCode: Int, listener: OnNavigationListener?): Any? {
        return _RouterX.getInstance().navigation(fragment, postcard, requestCode, listener)
    }

    companion object {


        // Key of raw uri
        internal const val RAW_URI = "NTeRQWvye18AkPd6G"
        internal const val AUTO_INJECT = "wmHzgD4lOj5o4241"

        @Volatile
        private var sInstance: RouterX? = null

        @Volatile
        private var hasInitialized = false

        /**
         * 初始化XRouter，必须先初始化
         *
         * @param application
         */
        fun init(application: Application) {
            if (!hasInitialized) {
                RXLog.i("RouterX init start.")
                hasInitialized = _RouterX.init(application)
                if (hasInitialized) {
                    _RouterX.afterInitialized()
                }
                RXLog.i("XRouter init over.");
            }
        }

        /**
         * 获取XRouter的实例
         */
        fun getInstance(): RouterX {
            if (!hasInitialized) {
                throw InitializationException("RouterX#Init#Invoke init(context) first!")
            } else {
                if (sInstance == null) {
                    synchronized(RouterX::class) {
                        if (sInstance == null) {
                            sInstance = RouterX()
                        }
                    }
                }
                return sInstance!!
            }
        }

        /**
         * 打开调试模式
         */
        fun enableDebug() {
            _RouterX.enableDebug()
        }

        /**
         * 打开日志
         */
        fun enableLog() {
            _RouterX.enableLog()
        }

        /**
         * 设置拦截器执行的线程
         *
         * @param tpe
         */
        @Synchronized
        fun setExecutor(poolExecutor: ThreadPoolExecutor) {
            _RouterX.setExecutor(poolExecutor)
        }

        @Synchronized
        fun destroy() {
            _RouterX.destroy()
            hasInitialized = false
        }

        fun debuggable(): Boolean {
            return _RouterX.debuggable()
        }

        fun monitorMode(): Boolean {
            return _RouterX.monitorMode()
        }

        /**
         * 设置日志接口
         *
         * @param logger
         */
        fun setLogger(logger: ILogger) {
            _RouterX.setLogger(logger)
        }
    }
}