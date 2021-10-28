package me.bytebeats.routerx.core.facade.service

import android.net.Uri
import me.bytebeats.routerx.core.facade.template.IProvider

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:16
 * @Version 1.0
 * @Description 路由路径重定向
 */

interface PathReplaceService : IProvider {

    /**
     * 重定向普通String类型的路由路径
     *
     * @param path raw path
     */

    fun forString(path: String?): String?

    /**
     * 重定向资源uri类型的路由路径
     *
     * @param uri raw uri
     */
    fun forUri(uri: Uri?): Uri?
}