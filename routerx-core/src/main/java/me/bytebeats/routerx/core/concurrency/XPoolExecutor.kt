package me.bytebeats.routerx.core.concurrency

import me.bytebeats.routerx.core.logger.RXLog
import me.bytebeats.routerx.core.util.format
import java.util.concurrent.*

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 10:38
 * @Version 1.0
 * @Description TO-DO
 */

class XPoolExecutor private constructor(
    corePoolSize: Int,
    maxPoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit,
    workQueue: BlockingQueue<Runnable>,
    threadFactory: ThreadFactory
) : ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, threadFactory,
    RejectedExecutionHandler { r, executor -> RXLog.e("Task $r was rejected by $executor, too many task!") }) {

    /**
     * 线程执行结束，检查是否存在异常
     */
    override fun afterExecute(r: Runnable?, t: Throwable?) {
        super.afterExecute(r, t)
        var tt: Throwable? = t
        if (t == null && r is Future<*>) {
            try {
                r.get()
            } catch (ce: CancellationException) {
                tt = ce
            } catch (ee: ExecutionException) {
                tt = ee
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        tt?.let {
            RXLog.e(
                "Running task appeared exception! Thread [${Thread.currentThread().name}], because [${tt.message}]\n${
                    tt.stackTrace.format()
                }"
            )
        }
    }

    companion object {
        /**
         * CPU处理数量
         */
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val INIT_THREAD_COUNT = CPU_COUNT + 1
        private val MAX_THREAD_COUNT = INIT_THREAD_COUNT
        private const val SURPLUS_THREAD_LIFE = 30L

        private var mInstance: XPoolExecutor? = null

        fun getInstance(): XPoolExecutor {
            if (mInstance == null) {
                synchronized(XPoolExecutor::class.java) {
                    if (mInstance == null) {
                        mInstance = XPoolExecutor(
                            CPU_COUNT,
                            MAX_THREAD_COUNT,
                            SURPLUS_THREAD_LIFE,
                            TimeUnit.SECONDS,
                            ArrayBlockingQueue(64),
                            XThreadFactory()
                        )
                    }
                }
            }
            return mInstance!!
        }
    }
}