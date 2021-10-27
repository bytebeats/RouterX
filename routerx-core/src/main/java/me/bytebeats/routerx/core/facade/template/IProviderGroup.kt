package me.bytebeats.routerx.core.facade.template

import me.bytebeats.routerx.annotation.meta.RouteMeta

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 17:21
 * @Version 1.0
 * @Description Provider组
 */

interface IProviderGroup {
    /**
     * 加载Provider注册信息表
     * @param providers input
     */
    fun loadInto(providers: Map<String, RouteMeta>)
}