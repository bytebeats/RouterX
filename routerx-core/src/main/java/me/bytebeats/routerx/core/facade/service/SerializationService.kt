package me.bytebeats.routerx.core.facade.service

import me.bytebeats.routerx.core.facade.template.IProvider
import java.lang.reflect.Type

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:13
 * @Version 1.0
 * @Description 序列化服务
 */

interface SerializationService : IProvider {
    /**
     * 对象序列化为json
     *
     * @param instance obj
     * @return json string
     */

    fun toJson(instance: Any): String

    /**
     * json反序列化为对象
     *
     * @param json json string
     * @param clazz object type
     * @return instance of object
     */
    fun <T> parseObject(json: String, clazz: Type): T

}