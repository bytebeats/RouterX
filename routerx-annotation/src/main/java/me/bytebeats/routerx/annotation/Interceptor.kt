package me.bytebeats.routerx.annotation

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/10/25 20:27
 * @Version 1.0
 * @Description 路由拦截器 <br>注意 : 该注解只能表注#{IInterceptor}的实现类
 * @param priority  拦截器的优先级, RouterX将按优先级高低依次执行拦截
 * @param name  拦截器的名称
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class Interceptor(
    val priority: Int,
    val name: String = "No name"
)
