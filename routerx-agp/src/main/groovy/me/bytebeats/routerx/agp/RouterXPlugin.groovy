package me.bytebeats.routerx.agp

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import me.bytebeats.routerx.agp.util.LoggerX
import me.bytebeats.routerx.agp.util.ScanConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/29 12:08
 * @Version 1.0
 * @Description 自动注册路由表的插件
 */

class RouterXPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        boolean hasApp = project.plugins.hasPlugin(AppPlugin)
        //only application module needs this plugin to generate register code
        if (hasApp) {
            LoggerX.make(project)
            LoggerX.i("routerx-agp is enabled")
            def android = project.extensions.getByType(AppExtension)
            def transformX = new RouterXTransform(project)

            //初始化 routerx-agp 扫描设置
            def scanConfigs = new ArrayList<ScanConfiguration>(3)
            scanConfigs.add(new ScanConfiguration("IRooteRoot"))
            scanConfigs.add(new ScanConfiguration("IInterceptorGroup"))
            scanConfigs.add(new ScanConfiguration("IProviderGroup"))

            android.registerTransform(transformX)
        }
    }
}
