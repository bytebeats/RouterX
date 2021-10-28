package me.bytebeats.routerx.core

import android.app.Application
import me.bytebeats.routerx.core.exception.InitializationException
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

        /**
         * 注入参数和服务
         */
        fun inject(target: Any) {
            _RouterX.inject(target)
        }
    }
}