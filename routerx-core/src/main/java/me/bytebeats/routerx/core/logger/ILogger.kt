package me.bytebeats.routerx.core.logger

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/26 21:12
 * @Version 1.0
 * @Description 日志接口
 */

interface ILogger {
    /**
     * 打印信息
     *
     * @param priority 优先级
     * @param tag      标签
     * @param message  信息
     * @param t        出错信息
     */
    fun log(priority: Int, tag: String, message: String?, t: Throwable?)
}