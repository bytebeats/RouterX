package me.bytebeats.routerx.core

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import me.bytebeats.routerx.core.concurrency.XPoolExecutor
import me.bytebeats.routerx.core.exception.InitializationException
import me.bytebeats.routerx.core.facade.service.InterceptorService
import me.bytebeats.routerx.core.logger.ILogger
import me.bytebeats.routerx.core.logger.RXLog
import me.bytebeats.routerx.core.runtime.LogisticsCenter
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

        }
    }
}