package me.bytebeats.routerx.core.runtime

import android.content.Context
import me.bytebeats.routerx.core.concurrency.CancelableCountDownLatch
import me.bytebeats.routerx.core.exception.HandlerException
import me.bytebeats.routerx.core.facade.Postcard
import me.bytebeats.routerx.core.facade.callback.InterceptorCallback
import me.bytebeats.routerx.core.facade.service.InterceptorService
import me.bytebeats.routerx.core.logger.RXLog
import me.bytebeats.routerx.core.util.TAG
import java.util.concurrent.TimeUnit

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/28 10:14
 * @Version 1.0
 * @Description 拦截器服务的实现类，实现全局路由拦截服务
 */

class InterceptorServiceImpl : InterceptorService {

    /**
     * 初始化所有的拦截器，并将其加入到路由表中
     *
     * @param context 上下文
     */
    override fun init(context: Context) {
        LogisticsCenter.mExecutor.execute {
            if (WareHouse.interceptorsIndex.isNotEmpty()) {
                for (entry in WareHouse.interceptorsIndex) {
                    val interceptorClass = entry.value
                    try {
                        //初始化所有的拦截器，并将其加入到路由表中
                        val interceptorInstance = interceptorClass.getConstructor().newInstance()
                        interceptorInstance.init(context)
                        WareHouse.interceptors.add(interceptorInstance)
                    } catch (exp: Exception) {
                        throw HandlerException("${TAG}XRouter init interceptor error! name = [${interceptorClass.name}], reason = [${exp.message}]")
                    }
                }
                isInitialized = true
                RXLog.i("RouterX interceptors init finished.")
                synchronized(initLock) {
                    initLock.notifyAll()
                }
            }
        }
    }

    /**
     * 执行拦截器
     *
     * @param postcard
     * @param callback 拦截器的回调监听
     */
    override fun doIntercept(postcard: Postcard, callback: InterceptorCallback) {
        if (WareHouse.interceptors.isNotEmpty()) {
            checkInit()
            if (!isInitialized) {
                callback.onInterrupted(HandlerException("Interceptors initialization takes too much time."))
                return
            }
            LogisticsCenter.mExecutor.execute {
                val interceptorCounter = CancelableCountDownLatch(WareHouse.interceptors.size)
                try {
                    execute(0, interceptorCounter, postcard)
                    interceptorCounter.await(postcard.timeout, TimeUnit.SECONDS)
                    if (interceptorCounter.count > 0) {// Cancel the navigation this time, if it hasn't return anything.
                        callback.onInterrupted(HandlerException("The interceptor processing timed out."))
                    } else if (postcard.tag != null) {
                        callback.onInterrupted(HandlerException(postcard.tag.toString()))
                    } else {
                        callback.onContinue(postcard)
                    }
                } catch (e: Exception) {
                    callback.onInterrupted(e)
                }
            }
        } else {
            callback.onContinue(postcard)
        }
    }

    companion object {
        private var isInitialized = false
        private val initLock = Object()

        /**
         * 执行拦截器
         *
         * @param index    档期执行拦截器的索引
         * @param counter  拦截器的执行计数锁
         * @param postcard 路由信息
         */
        private fun execute(index: Int, counter: CancelableCountDownLatch, postcard: Postcard) {
            if (index < WareHouse.interceptors.size) {
                WareHouse.interceptors[index].process(postcard, object : InterceptorCallback {
                    // 执行拦截器的process方法
                    override fun onContinue(postcard: Postcard) {
                        /*  Last interceptor execute over with no exception  */
                        counter.countDown()
                        execute(index + 1, counter, postcard)
                    }

                    override fun onInterrupted(exp: Exception) {
                        /*  Last interceptor execute over with fatal exception  */
                        postcard.tag = exp.message ?: HandlerException("No message.")
                        counter.cancel()
                        // Be attention, maybe the thread in callback has been changed,
                        // then the catch block(L207) will be invalid.
                        // The worst is the thread changed to main thread, then the app will be crash, if you throw this exception!
//                        if (Looper.getMainLooper() != (Looper.myLooper())) {    // You shouldn't throw the exception if the thread is main thread.
//                            throw HandlerException(exp.message ?: "")
//                        }
                    }
                })
            }
        }

        /**
         * 检查拦截器的初始化状态
         */
        private fun checkInit() {
            synchronized(initLock) {
                while (!isInitialized) {
                    try {
                        initLock.wait(10L * 1000L)
                    } catch (ee: InterruptedException) {
                        throw HandlerException("$TAG Interceptor init cost too much time error! reason = [${ee.message}]")
                    }
                }
            }
        }
    }
}