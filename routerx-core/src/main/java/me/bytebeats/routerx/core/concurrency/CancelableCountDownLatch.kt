package me.bytebeats.routerx.core.concurrency

import java.util.concurrent.CountDownLatch

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 10:22
 * @Version 1.0
 * @Description 可取消的计数线程锁【多线程并发锁】
 */

class CancelableCountDownLatch(private val count: Int) : CountDownLatch(count) {

    /**
     * 取消计数锁
     */
    fun cancel() {
        while (getCount() > 0L) {
            countDown()
        }
    }
}