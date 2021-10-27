package me.bytebeats.routerx.annotation


/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/10/25 21:27
 * @Version 1.0
 * @Description 路由创建注解
 * @param path  路由的路径，必填
 * @param group 路由所在的组
 * @param name  路由的名称
 * @param extras  路由的拓展属性，这个属性是一个int值，换句话说，单个int有4字节，也就是32位，可以配置32个开关;
 * Ps. U should use the integer num sign the switch, by bits. 10001010101010
 * @param priority  路由的优先级
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Router(
    val path: String,
    val group: String = "",
    val name: String = "undefined",
    val extras: Int = Int.MIN_VALUE,
    val priority: Int = -1,
)
