package me.bytebeats.routerx.core.concurrency

import me.bytebeats.routerx.core.logger.RXLog
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 10:25
 * @Version 1.0
 * @Description 线程池工厂类
 */

class XThreadFactory : ThreadFactory {
    private val threadGroup: ThreadGroup? =
        System.getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup
    private val threadNamePrefix: String = "XRouter task pool No.${THREAD_POOL_COUNTER.getAndIncrement()}, thread No."
    private val threadCounter: AtomicInteger = AtomicInteger(1)

    override fun newThread(r: Runnable?): Thread {
        val threadName = "$threadNamePrefix${threadCounter.getAndIncrement()}"
        RXLog.i("Thread produced, name is [$threadName}]")
        val thread = Thread(threadGroup, r, threadName, 0)
        if (thread.isDaemon) {
            thread.isDaemon = false
        }
        if (thread.priority != Thread.NORM_PRIORITY) {
            thread.priority = Thread.NORM_PRIORITY
        }
        /*  捕获多线程处理中的异常  */
        thread.setUncaughtExceptionHandler { t, e ->
            RXLog.e("Running task appeared exception! Thread [$t.name}], because [${e.message}]", e)
        }
        return thread
    }

    companion object {
        private val THREAD_POOL_COUNTER = AtomicInteger(1)
    }

}