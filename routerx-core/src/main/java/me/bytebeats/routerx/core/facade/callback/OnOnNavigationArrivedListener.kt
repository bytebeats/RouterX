package me.bytebeats.routerx.core.facade.callback

import me.bytebeats.routerx.core.facade.Postcard

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 17:10
 * @Version 1.0
 * @Description 简单的路由导航回调, 默认只提供导航到达的监听
 */

abstract class OnOnNavigationArrivedListener : OnNavigationListener {
    override fun onFound(postcard: Postcard) {
        //default implementation, do nothing here by default
    }

    override fun onLost(postcard: Postcard) {
        //default implementation, do nothing here by default
    }

    override fun onInterrupted(postcard: Postcard) {
        //default implementation, do nothing here by default
    }
}