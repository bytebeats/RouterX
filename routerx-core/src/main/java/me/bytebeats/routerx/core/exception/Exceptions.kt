package me.bytebeats.routerx.core.exception

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 15:47
 * @Version 1.0
 * @Description RouterX相关异常
 */

/**
 * 初始化相关异常
 */
class InitializationException(message: String) : RuntimeException(message)

/**
 * 主流程的处理异常
 */
class HandlerException(message: String) : RuntimeException(message)

/**
 * 路由找不到错误
 */
class RouteNotFoundException(message: String) : RuntimeException(message)