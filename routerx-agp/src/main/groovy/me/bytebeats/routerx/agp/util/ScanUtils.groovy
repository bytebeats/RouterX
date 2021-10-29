package me.bytebeats.routerx.agp.util

import me.bytebeats.routerx.agp.RouterXTransform
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/29 15:31
 * @Version 1.0
 * @Description * 扫描 me/bytebeats/routerx/ 所有的class文件
 * <p>寻找到自动生成的路由注册接口：routers、interceptors、providers</p>
 * <p>接口包括：IRouteRoot、IInterceptorGroup、IProviderGroup</p>
 */

class ScanUtils {
    private ScanUtils() {

    }

    /**
     * 扫描jar文件
     * @param jarFile 所有被打包依赖进apk的jar文件
     * @param destFile dest file after this transform
     */
    static void scanJar(File jar, File destFile) {
        if (jar != null) {
            def jarFile = new JarFile(jar)
            Enumeration enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement()
                String entryName = jarEntry.getName()
                if (entryName.startsWith(ScanConfiguration.ROUTER_CLASS_PACKAGE_NAME)) {
                    InputStream inputStream = jarFile.getInputStream(jarEntry)
                    scanClass(inputStream)
                    inputStream.close()
                } else if (ScanConfiguration.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                    // 标记这个jar文件中是否存在 LogisticsCenter.class -- 需要动态注入注册代码的类
                    // 在扫描完成后,将向 LogisticsCenter.class 的loadRouterMap方法中注入注册代码
                    RouterXTransform.initClassFile = destFile
                }
            }
        }
    }

    /**
     * 判断jar文件是否可能注册了路由【android的library可以直接排除】
     * @param jarFilepath jar文件的路径
     */
    static boolean shouldProcessPreDexJar(String jarFilePath) {
        return jarFilePath != null && !jarFilePath.contains("com.android.support") && !jarFilePath.contains("/android/m2repository")
    }

    /**
     * 判断扫描的类的包名是否是 annotationProcessor自动生成路由代码的包名：me/bytebeats/routerx/routes/
     * @param classFilePath 扫描的class文件的路径
     */
    static boolean shouldProcessClass(String classFilePath) {
        return classFilePath != null && classFilePath.startsWith(ScanConfiguration.ROUTER_CLASS_PACKAGE_NAME)
    }

    /**
     * 扫描class文件
     * @param file class文件
     */
    static void scanClass(File file) {
        scanClass(new FileInputStream(file))
    }

    /**
     * 扫描class文件
     * @param inputStream 文件流
     */
    static void scanClass(InputStream inputStream) {
        def classReader = new ClassReader(inputStream)
        def classWriter = new ClassWriter(classReader, 0)
        def classVisitor = new ScanClassVisitor(Opcodes.ASM5, classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    static class ScanClassVisitor extends ClassVisitor {
        ScanClassVisitor(int api, ClassVisitor visitor) {
            super(api, visitor)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            RouterXTransform.scanConfigs.each { config ->
                if (config.interfaceName && interfaces != null) {
                    interfaces.each {
                        if (it == config.interfaceName) {
                            //搜索所有实现接口是IRouteRoot、IInterceptorGroup、IProviderGroup的类
                            config.classList.add(name)
                        }
                    }
                }
            }
        }
    }
}
