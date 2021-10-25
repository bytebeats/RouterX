package me.bytebeats.routerx.annotation

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/10/25 20:27
 * @Version 1.0
 * @Description 实现自动装配（依赖注入）的注解
 * @param name  参数的字段名／服务名, 默认是字段的参数名
 * @param required  是否是非空字段，If required, app will be crash when value is null.
 * @param description   字段的描述
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class AutoWired(
    val name: String = "",
    val required: Boolean = false,
    val description: String = "No descriptions"
)
