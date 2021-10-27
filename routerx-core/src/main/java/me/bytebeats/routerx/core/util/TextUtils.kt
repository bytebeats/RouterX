package me.bytebeats.routerx.core.util

import android.net.Uri

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 10:53
 * @Version 1.0
 * @Description Text Utils
 */


/**
 * 打印程序堆栈信息
 */
fun Array<StackTraceElement>?.format(): String = this?.joinToString(separator = "\tat ", postfix = "\n") ?: ""

/**
 * 分割查询的参数
 */
fun Uri.transformToMap(): Map<String, String> {
    val paramPairs = mutableMapOf<String, String>()
    val query = encodedQuery
    if (!query.isNullOrEmpty()) {
        var start = 0
        do {
            val next = query.indexOf('&', start)
            val end = if (next == -1) query.length else next
            var separator = query.indexOf('=', start)
            if (separator > end || separator == -1) {
                separator = end
            }
            val key = query.substring(start, separator)
            if (key.isNotEmpty()) {
                val value = if (separator == end) "" else query.substring(separator + 1, end)
                paramPairs += (key to value)
            }
            start = end + 1
        } while (start < query.length)
    }
    return paramPairs
}

/**
 * Split key with |
 *
 * @param key raw key
 * @return left key
 */
fun String.left(): String = if (contains("|") && !endsWith("|")) substring(0, indexOf("|")) else this

/**
 * Split key with |
 *
 * @param key raw key
 * @return right key
 */
fun String.right(): String = if (contains("|") && !startsWith("|")) substring(indexOf("|") + 1) else this