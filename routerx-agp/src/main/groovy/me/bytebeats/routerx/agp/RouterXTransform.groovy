package me.bytebeats.routerx.agp

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import me.bytebeats.routerx.agp.util.LoggerX
import me.bytebeats.routerx.agp.util.ScanConfiguration
import me.bytebeats.routerx.agp.util.ScanUtils
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/29 12:11
 * @Version 1.0
 * @Description 自动注册路由的插件（核心功能就是在 LogisticsCenter 的 loadRouterMap中动态插入注册路由的代码）
 */

class RouterXTransform extends Transform {
    private Project project

    /**
     * 扫描接口的集合
     */
    static ArrayList<ScanConfiguration> scanConfigs

    /**
     * 包含 LogisticsCenter 类的jar文件
     */
    static File initClassFile

    RouterXTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return ScanConfiguration.PLUGIN_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        LoggerX.i("Start scan register info in jar file.")
        long startTimeMillis = System.currentTimeMillis()
        boolean isLeftSlash = File.separator == '/'
        def outputProvider = transformInvocation.outputProvider

        transformInvocation.inputs.each { input ->
            /*  扫描所有的jar包，因为jar包中可能包含其他module的注册的路由  */
            input.jarInputs.each { jarInput ->
                def destName = jarInput.name
                /*  rename jar files  */
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.size() - 4)
                }
                def src = jarInput.file
                def dest = outputProvider.getContentLocation("${destName}_${hexName}", jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (ScanUtils.shouldProcessPreDexJar(src.absolutePath)) {
                    ScanUtils.scanJar(src, dest)
                }
                FileUtils.copyFile(src, dest)
            }

            input.directoryInputs.each { dir ->
                def dest = outputProvider.getContentLocation(dir.name, dir.contentTypes, dir.scopes, Format.DIRECTORY)
                def root = dir.file.absolutePath
                if (!root.endsWith(File.separator)) {
                    root += File.separator
                }
                dir.file.eachFileRecurse { file ->
                    def path = file.absolutePath.replace(root, "")
                    if (!isLeftSlash) {
                        path = path.replaceAll("\\\\", "/")
                    }
                    if (file.isFile() && ScanUtils.shouldProcessClass(path)) {
                        ScanUtils.scanClass(file)
                    }
                }
                FileUtils.copyFile(dir.file, dest)
            }
        }

        LoggerX.i("Scan finish, current cost time ${System.currentTimeMillis() - startTimeMillis}ms")

        /*  扫描结束后，将扫描到的路由注册结果自动注入到 LogisticsCenter.class 的loadRouterMap方法中  */
        if (initClassFile) {
            scanConfigs.each { config ->
                LoggerX.i("Insert register code to file ${initClassFile.absolutePath}")

                if (config.classList.isEmpty()) {
                    LoggerX.e("No class implements found for interface: ${config.interfaceName}")
                } else {
                    config.classList.each {
                        LoggerX.i(it)
                    }
                    RouterXGenerator.insertInitCodeTo(config)
                }
            }
        }
        LoggerX.i("Generate code finish, current cost time: ${System.currentTimeMillis() - startTimeMillis}ms")
    }
}
