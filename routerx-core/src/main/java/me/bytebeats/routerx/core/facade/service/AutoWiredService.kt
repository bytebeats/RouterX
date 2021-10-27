package me.bytebeats.routerx.core.facade.service

import me.bytebeats.routerx.core.facade.template.IProvider

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 15:57
 * @Version 1.0
 * @Description 实现自动装配（依赖注入）的服务
 */

interface AutoWiredService : IProvider {
    /**
     * 自动装配
     * @param instance 自动装配的目标
     */
    fun autoWire(instance: Any)
}