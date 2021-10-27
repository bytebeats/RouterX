package me.bytebeats.routerx.core.facade.template

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:09
 * @Version 1.0
 * @Description 注射器实现接口，实现依赖注入的方法
 */

interface ISyringe {
    /**
     * 依赖注入
     * @param target 依赖注入的目标
     */
    fun inject(target: Any)
}