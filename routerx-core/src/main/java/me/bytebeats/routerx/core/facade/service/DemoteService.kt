package me.bytebeats.routerx.core.facade.service

import android.content.Context
import me.bytebeats.routerx.core.facade.Postcard

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:19
 * @Version 1.0
 * @Description 路由降级服务
 */

interface DemoteService {
    /**
     * 路由丢失.
     *
     * @param postcard 路由信息
     */

    fun onLost(context: Context, postcard: Postcard)
}