package me.bytebeats.routerx.agp

import me.bytebeats.routerx.agp.util.LoggerX
import me.bytebeats.routerx.agp.util.ScanConfiguration
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/29 12:11
 * @Version 1.0
 * @Description 向 LogisticsCenter.class 的 loadRouterMap 中 注入路由注册的代码
 */

class RouterXGenerator {
    private ScanConfiguration scanConfiguration

    private RouterXGenerator(ScanConfiguration configuration) {
        scanConfiguration = configuration
    }
    /**
     * 插入路由注册代码
     * @param registerSetting 扫描到的注册内容
     */
    static void insertInitCodeTo(ScanConfiguration config) {
        if (config != null && !config.classList.isEmpty()) {
            def generator = new RouterXGenerator(config)
            def file = RouterXTransform.initClassFile
            if (file.name.endsWith(".jar")) {
                generator.insertInitCodesIntoJarFile(file)
            }
        }
    }

    /**
     * 遍历jar包找到 LogisticsCenter.class 文件，向其中加入注册的代码
     * @param jarFile the jar file which contains LogisticsCenter.class
     * @return
     */
    private File insertInitCodesIntoJarFile(File jar) {
        if (jar) {
            def optFile = new File(jar.parent, "${jar.name}.opt")
            if (optFile.exists()) {
                optFile.delete()
            }
            def jarFile = new JarFile(jar)
            def enumeration = jarFile.entries()
            def jarOutputStream = new JarOutputStream(new FileOutputStream(optFile))
            while (enumeration.hasMoreElements()) {
                def entry = enumeration.nextElement()
                def entryName = entry.name
                def zipEntry = new ZipEntry(entryName)
                def inputStream = jarFile.getInputStream(entry)
                jarOutputStream.putNextEntry(zipEntry)
                if (ScanConfiguration.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                    LoggerX.i("Insert init code to class >> ${entryName}")
                    def bytes = referHackWhenInit(inputStream)
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()
            if (jar.exists()) {
                jar.delete()
            }
            optFile.renameTo(jar)
        }
        return jar
    }

    /**
     * 访问class类，动态添加执行方法代码
     * @param inputStream
     * @return
     */
    private byte[] referHackWhenInit(InputStream inputStream) {
        def reader = new ClassReader(inputStream)
        def writer = new ClassWriter(reader, 0)
        def visitor = new RouteClassVisitor(Opcodes.ASM5, writer)
        reader.accept(visitor, ClassReader.EXPAND_FRAMES)
        return writer.toByteArray()
    }

    class RouteClassVisitor extends ClassVisitor {
        RouteClassVisitor(int opcodes, ClassVisitor visitor) {
            super(opcodes, visitor)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            def mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            //generate code into this method
            if (name == ScanConfiguration.GENERATE_TO_METHOD_NAME) {// 找到动态生成注册代码需要注入的 loadRouterMap 方法
                mv = new RouteMethodVisitor(Opcodes.ASM5, mv)
            }
            return mv
        }
    }

    class RouteMethodVisitor extends MethodVisitor {
        RouteMethodVisitor(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor)
        }

        @Override
        void visitInsn(int opcode) {
            /*  generate code before return  */
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                scanConfiguration.classList.each { className ->
                    /* 将类文件的路径转化为包的路径 */
                    def name = className.replaceAll("/", ".")
                    /* 访问方法的参数--搜索到的接口类名 */
                    mv.visitLdcInsn(name)
                    /* 生成注册代码到 LogisticsCenter.loadRouterMap() 方法中 */
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC,// 操作码
                            ScanConfiguration.GENERATE_TO_CLASS_NAME,// 访问类的类名
                            ScanConfiguration.METHOD_REGISTER,// 访问的方法
                            "(Ljava/lang/String;)V",// 访问参数的类型
                            false// 访问的类是否是接口
                    )
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }
}
