package me.bytebeats.routerx.core.runtime

import java.util.*
import kotlin.jvm.Throws

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:44
 * @Version 1.0
 * @Description 存放唯一KEY的TreeMap
 */

class UniqueKeyTreeMap<K, V>(private val warning: String) : TreeMap<K, V>() {
    @Throws(RuntimeException::class)
    override fun put(key: K, value: V): V? {
        if (containsKey(key)) {
            throw RuntimeException(warning.format(key))
        } else {
            return super.put(key, value)
        }
    }
}