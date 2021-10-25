package me.bytebeats.routerx.apt

import javax.annotation.processing.Messager
import javax.tools.Diagnostic

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/25 20:37
 * @Version 1.0
 * @Description 日志记录
 */

class Logger(private val msgr: Messager) {
    /* Print info log */
    fun info(info: CharSequence?) {
        if (!info.isNullOrEmpty()) {
            msgr.printMessage(Diagnostic.Kind.NOTE, "${PREFIX_OF_LOGGER}$info")
        }
    }

    fun warning(warning: CharSequence?) {
        if (!warning.isNullOrEmpty()) {
            msgr.printMessage(Diagnostic.Kind.WARNING, "${PREFIX_OF_LOGGER}An exception is encountered, [\"$warning\"]")
        }
    }

    fun error(error: CharSequence?) {
        if (!error.isNullOrEmpty()) {
            msgr.printMessage(Diagnostic.Kind.ERROR, "${PREFIX_OF_LOGGER}An exception is encountered, [\"$error\"]")
        }
    }

    fun error(error: Throwable?) {
        error?.run {
            msgr.printMessage(
                Diagnostic.Kind.ERROR,
                "${PREFIX_OF_LOGGER}An exception is encountered, [\"${error.message}\"] \n${formatStackTrace(error.stackTrace)}"
            )
        }
    }

    private fun formatStackTrace(stackTrace: Array<StackTraceElement>) = stackTrace.joinToString(separator = "\n\t ")
}