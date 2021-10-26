package me.bytebeats.routerx.core.logger

import android.util.Log
import android.util.Log.getStackTraceString
import org.jetbrains.annotations.NotNull
import java.io.PrintWriter
import java.io.StringWriter

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/26 21:14
 * @Version 1.0
 * @Description TO-DO
 */

class LogcatLogger : ILogger {

    /**
     * 打印信息
     *
     * @param priority 优先级
     * @param tag      标签
     * @param message  信息
     * @param t        出错信息
     */
    override fun log(priority: Int, tag: String, message: String?, t: Throwable?) {
        var msg = ""
        if (message.isNullOrEmpty()) {
            if (t == null) {
                return // Swallow message if it's null and there's no throwable.
            }
            msg = stackTraces(t)
        } else {
            if (t != null) {
                msg = "$message\n" + stackTraces(t)
            }
        }

        log(priority, tag, msg)
    }

    private fun stackTraces(t: Throwable): String {
        /**
         * Don't replace this with Log.getStackTraceString() - it hides
         * UnknownHostException, which is not what we want.
         */
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    /**
     * 打印信息
     *
     * @param priority 优先级
     * @param tag      标签
     * @param message  信息
     */
    fun log(priority: Int, tag: String, message: String) {
        val block = message.length / MAX_LOG_LENGTH
        if (block > 0) {
            var start = 0
            for (i in 0 until block) {
                val end = start + MAX_LOG_LENGTH
                logStub(priority, tag, message.substring(start, end))
                start = end
            }
        } else {
            logStub(priority, tag, message)
        }
    }

    /**
     * 使用LogCat输出日志.
     *
     * @param priority 优先级
     * @param tag      标签
     * @param stub      信息
     */
    private fun logStub(priority: Int, @NotNull tag: String, @NotNull stub: String) {
        when (priority) {
            Log.DEBUG -> Log.d(tag, stub)
            Log.INFO -> Log.i(tag, stub)
            Log.WARN -> Log.w(tag, stub)
            Log.ERROR -> Log.e(tag, stub)
            Log.ASSERT -> Log.wtf(tag, stub)
            else -> Log.v(tag, stub)
        }
    }

    companion object {
        /*  Logcat能够打印日志的最大长度  */
        private const val MAX_LOG_LENGTH = 4000
    }
}