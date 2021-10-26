package me.bytebeats.routerx.core.logger

import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull


/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/26 11:51
 * @Version 1.0
 * @Description RouterX Log
 */

object RXLog {
    //==============常量================//
    /**
     * 默认tag
     */
    const val DEFAULT_LOG_TAG = "[RouterX]"

    /**
     * 最大日志优先级【日志优先级为最大等级，所有日志都不打印】
     */
    private const val MAX_LOG_PRIORITY = 10

    /**
     * 最小日志优先级【日志优先级为最小等级，所有日志都打印】
     */
    private const val MIN_LOG_PRIORITY = 0

    //==============属性================//
    /**
     * 默认的日志记录为Logcat
     */
    private var sILogger: ILogger? = LogcatLogger()

    private var sTag = DEFAULT_LOG_TAG

    /**
     * 是否是调试模式
     */
    private var sIsDebug = false

    /**
     * 日志打印优先级
     */
    private var sLogPriority = MAX_LOG_PRIORITY

    //==============属性设置================//
    /**
     * 设置日志记录者的接口
     *
     * @param logger
     */
    fun setLogger(@NonNull logger: ILogger?) {
        sILogger = logger
    }

    /**
     * 设置日志的tag
     *
     * @param tag
     */
    fun setTag(tag: String) {
        sTag = tag
    }

    /**
     * 设置是否是调试模式
     *
     * @param isDebug
     */
    fun setDebug(isDebug: Boolean) {
        sIsDebug = isDebug
    }

    /**
     * 设置打印日志的等级（只打印改等级以上的日志）
     *
     * @param priority
     */
    fun setPriority(priority: Int) {
        sLogPriority = priority
    }

    //===================对外接口=======================//

    //===================对外接口=======================//
    /**
     * 设置是否打开调试
     *
     * @param isDebug
     */
    fun debug(isDebug: Boolean) {
        if (isDebug) {
            debug(DEFAULT_LOG_TAG)
        } else {
            debug("")
        }
    }

    /**
     * 设置调试模式
     *
     * @param tag
     */
    fun debug(tag: String) {
        if (!TextUtils.isEmpty(tag)) {
            setDebug(true)
            setPriority(MIN_LOG_PRIORITY)
            setTag(tag)
        } else {
            setDebug(false)
            setPriority(MAX_LOG_PRIORITY)
            setTag("")
        }
    }

    //=============打印方法===============//

    //=============打印方法===============//
    /**
     * 打印任何（所有）信息
     *
     * @param msg
     */
    fun v(msg: String?) {
        if (isPriorityAtLeast(Log.VERBOSE)) {
            sILogger?.log(Log.VERBOSE, sTag, msg, null)
        }
    }

    /**
     * 打印任何（所有）信息
     *
     * @param tag
     * @param msg
     */
    fun vTag(tag: String?, msg: String?) {
        if (isPriorityAtLeast(Log.VERBOSE)) {
            sILogger?.log(Log.VERBOSE, tag!!, msg, null)
        }
    }

    /**
     * 打印调试信息
     *
     * @param msg
     */
    fun d(msg: String?) {
        if (isPriorityAtLeast(Log.DEBUG)) {
            sILogger?.log(Log.DEBUG, sTag, msg, null)
        }
    }

    /**
     * 打印调试信息
     *
     * @param tag
     * @param msg
     */
    fun dTag(tag: String?, msg: String?) {
        if (isPriorityAtLeast(Log.DEBUG)) {
            sILogger?.log(Log.DEBUG, tag!!, msg, null)
        }
    }

    /**
     * 打印提示性的信息
     *
     * @param msg
     */
    fun i(msg: String?) {
        if (isPriorityAtLeast(Log.INFO)) {
            sILogger?.log(Log.INFO, sTag, msg, null)
        }
    }

    /**
     * 打印提示性的信息
     *
     * @param tag
     * @param msg
     */
    fun iTag(tag: String?, msg: String?) {
        if (isPriorityAtLeast(Log.INFO)) {
            sILogger?.log(Log.INFO, tag!!, msg, null)
        }
    }

    /**
     * 打印warning警告信息
     *
     * @param msg
     */
    fun w(msg: String?) {
        if (isPriorityAtLeast(Log.WARN)) {
            sILogger?.log(Log.WARN, sTag, msg, null)
        }
    }

    /**
     * 打印warning警告信息
     *
     * @param tag
     * @param msg
     */
    fun wTag(tag: String?, msg: String?) {
        if (isPriorityAtLeast(Log.WARN)) {
            sILogger?.log(Log.WARN, tag!!, msg, null)
        }
    }

    /**
     * 打印出错信息
     *
     * @param msg
     */
    fun e(msg: String?) {
        if (isPriorityAtLeast(Log.ERROR)) {
            sILogger?.log(Log.ERROR, sTag, msg, null)
        }
    }

    /**
     * 打印出错信息
     *
     * @param tag
     * @param msg
     */
    fun eTag(tag: String?, msg: String?) {
        if (isPriorityAtLeast(Log.ERROR)) {
            sILogger?.log(Log.ERROR, tag!!, msg, null)
        }
    }

    /**
     * 打印出错堆栈信息
     *
     * @param t
     */
    fun e(t: Throwable?) {
        if (isPriorityAtLeast(Log.ERROR)) {
            sILogger?.log(Log.ERROR, sTag, null, t)
        }
    }

    /**
     * 打印出错堆栈信息
     *
     * @param tag
     * @param t
     */
    fun eTag(tag: String?, t: Throwable?) {
        if (isPriorityAtLeast(Log.ERROR)) {
            sILogger?.log(Log.ERROR, tag!!, null, t)
        }
    }


    /**
     * 打印出错堆栈信息
     *
     * @param msg
     * @param t
     */
    fun e(msg: String?, t: Throwable?) {
        if (isPriorityAtLeast(Log.ERROR)) {
            sILogger?.log(Log.ERROR, sTag, msg, t)
        }
    }

    /**
     * 打印出错堆栈信息
     *
     * @param tag
     * @param msg
     * @param t
     */
    fun eTag(tag: String?, msg: String?, t: Throwable?) {
        if (isPriorityAtLeast(Log.ERROR)) {
            sILogger?.log(Log.ERROR, tag!!, msg, t)
        }
    }

    /**
     * 打印严重的错误信息
     *
     * @param msg
     */
    fun wtf(msg: String?) {
        if (isPriorityAtLeast(Log.ASSERT)) {
            sILogger?.log(Log.ASSERT, sTag, msg, null)
        }
    }

    /**
     * 打印严重的错误信息
     *
     * @param tag
     * @param msg
     */
    fun wtfTag(tag: String?, msg: String?) {
        if (isPriorityAtLeast(Log.ASSERT)) {
            sILogger?.log(Log.ASSERT, tag!!, msg, null)
        }
    }

    /**
     * 能否打印
     *
     * @param priority
     * @return
     */
    private fun isPriorityAtLeast(priority: Int): Boolean {
        return sILogger != null && sIsDebug && priority >= sLogPriority
    }
}