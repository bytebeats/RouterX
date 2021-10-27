package me.bytebeats.routerx.core.util

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 11:46
 * @Version 1.0
 * @Description Constants
 */

internal const val SDK_NAME = "RouterX"
internal const val TAG = "$SDK_NAME::"
internal const val SEPARATOR = "$$"
internal const val SUFFIX_ROOT = "Root"
internal const val SUFFIX_INTERCEPTORS = "Interceptors"
internal const val SUFFIX_PROVIDERS = "Providers"
internal const val SUFFIX_AUTOWIRED = SEPARATOR + SDK_NAME + SEPARATOR + "AutoWired"
internal const val DOT = "."
internal const val ROUTE_ROOT_PACKAGE = "me.bytebeats.routerx.core.routes"

internal const val ROUTE_ROOT_SERVICE = "me.bytebeats.routerx.core.facade.service"

internal const val ROUTE_SERVICE_INTERCEPTORS = "/routerx/service/interceptor"
internal const val ROUTE_SERVICE_AUTOWIRED = "/routerx/service/autowired"

/**
 * 路由缓存
 */
internal const val ROUTERX_SP_CACHE_KEY = "SP_ROUTERX_CACHE"
internal const val ROUTEX_SP_KEY_MAP = "SP_ROUTERX_MAP"
internal const val LAST_VERSION_NAME = "LAST_VERSION_NAME"
internal const val LAST_VERSION_CODE = "LAST_VERSION_CODE"