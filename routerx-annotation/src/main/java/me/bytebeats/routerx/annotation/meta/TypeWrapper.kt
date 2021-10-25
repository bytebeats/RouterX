package me.bytebeats.routerx.annotation.meta

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/25 19:59
 * @Version 1.0
 * @Description 获得泛型的类型
 */

class TypeWrapper<T>() {
    val type: Type
        get() = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
}