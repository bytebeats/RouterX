package me.bytebeats.routerx.core.facade.template

import android.content.Context

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 16:00
 * @Version 1.0
 * @Description 对外提供接口的基类接口
 */

interface IProvider {
    /**
     * 进程初始化的方法
     *
     * @param context 上下文
     */
    fun init(context: Context)
}