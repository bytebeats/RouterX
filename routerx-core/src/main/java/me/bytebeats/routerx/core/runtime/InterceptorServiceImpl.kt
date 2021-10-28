package me.bytebeats.routerx.core.runtime

import android.content.Context
import me.bytebeats.routerx.core.concurrency.CancelableCountDownLatch
import me.bytebeats.routerx.core.exception.HandlerException
import me.bytebeats.routerx.core.facade.Postcard
import me.bytebeats.routerx.core.facade.callback.InterceptorCallback
import me.bytebeats.routerx.core.facade.service.InterceptorService
import me.bytebeats.routerx.core.util.TAG

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/28 10:14
 * @Version 1.0
 * @Description 拦截器服务的实现类，实现全局路由拦截服务
 */

class InterceptorServiceImpl : InterceptorService {

    override fun init(context: Context) {

    }

    override fun doIntercept(postCard: Postcard, callback: InterceptorCallback) {

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