package me.bytebeats.routerx.apt.processor

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import me.bytebeats.routerx.annotation.Interceptor
import me.bytebeats.routerx.apt.*
import me.bytebeats.routerx.apt.ANNOTATION_TYPE_INTERCEPTOR
import me.bytebeats.routerx.apt.I_INTERCEPTOR
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import kotlin.jvm.Throws

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/25 21:04
 * @Version 1.0
 * @Description Process the annotation of {@link Interceptor}
 * 自动生成IInterceptor注册接口 RouterX$$Interceptors$$[moduleName]
 */

@AutoService(Processor::class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(ANNOTATION_TYPE_INTERCEPTOR)
class InterceptorProcessor : AbstractProcessor() {
    /**
     * 拦截器表【拦截器的优先级 and 拦截器的类】
     */
    private val interceptors = sortedMapOf<Int, Element>()

    /**
     * 写class文件到disk
     */
    private lateinit var filer: Filer

    /**
     * 日志打印工具
     */
    private lateinit var logger: Logger

    /**
     * 获取类的工具
     */
    private lateinit var elements: Elements

    /**
     * 模块名，可以是'app'或者其他
     */
    private var moduleName: String = ""

    private lateinit var interceptorTypeMirror: TypeMirror

    /**
     * Initializes the processor with the processing environment by
     * setting the {@code processingEnv} field to the value of the
     * {@code processingEnv} argument.  An {@code
     * IllegalStateException} will be thrown if this method is called
     * more than once on the same object.
     *
     * @param processingEnv environment to access facilities the tool framework
     *                      provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        processingEnv?.also {
            filer = it.filer
            elements = it.elementUtils
            logger = Logger(it.messager)
            obtainModuleName(it)
            interceptorTypeMirror = elements.getTypeElement(I_INTERCEPTOR).asType()
            logger.info(">>> InterceptorProcessor init. <<<")
        }
    }

    /**
     * 获取用户在annotationProcessorOptions中定义的[moduleName]
     *
     * @param processingEnv
     */

    private fun obtainModuleName(processingEnv: ProcessingEnvironment?) {
        val options = processingEnv?.options ?: mutableMapOf()
        if (options.isNotEmpty()) {
            moduleName = options[KEY_MODULE_NAME] ?: ""
        }

        if (moduleName.isNotEmpty()) {
            moduleName = moduleName.replace("[^0-9a-zA-Z_]+".toRegex(), "")
            logger.info("The user has configuration the module name, it was [$moduleName]")
        } else {
            logger.error(
                "These no module name, at 'build.gradle', like :\n" +
                        "apt {\n" +
                        "    arguments {\n" +
                        "        moduleName project.getName();\n" +
                        "    }\n" +
                        "}\n"
            );
            throw RuntimeException("$PREFIX_OF_LOGGER>>> No module name, for more information, look at gradle log.");
        }
    }

    /**
     * 扫描被{@link Interceptor}注解所修饰的类
     *
     * @param annotations
     * @param roundEnv
     */
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (!annotations.isNullOrEmpty()) {
            try {
                roundEnv?.getElementsAnnotatedWith(Interceptor::class.java)?.run {
                    parseInterceptors(this)
                }
            } catch (ignored: IOException) {
                logger.error(ignored)
            } catch (ignored: IllegalArgumentException) {
                logger.error(ignored)
            }
            return true
        }
        return false
    }

    /**
     * 解析路由拦截器注解{@link Interceptor}
     *
     * @param elements elements of Interceptor.
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    private fun parseInterceptors(elements: Set<Element>) {
        logger.info(">>> Found Interceptors, size is ${elements.size} <<<")
        elements.forEach { element ->// Verify and cache, sort incidentally.
            val isInterceptor = (element as TypeElement).interfaces.contains(interceptorTypeMirror)
            val interceptor = element.getAnnotation(Interceptor::class.java)
            if (isInterceptor && interceptor != null) {// 验证@Interceptor标注拦截器类的有效性
                logger.info("An interceptor verify over, it is " + element.asType())
                val lastInterceptor = interceptors[interceptor.priority]
                if (lastInterceptor != null) {
                    throw IllegalArgumentException(
                        "More than one interceptors use same priority [%d], They are [%s] and [%s].".format(
                            Locale.getDefault(), interceptor.priority, lastInterceptor.simpleName, element.simpleName
                        )
                    )
                } else {
                    interceptors[interceptor.priority] = element
                }
            } else {
                logger.error("An interceptor verify failed, it is " + element.asType());
            }
        }

        //RouterX Interfaces
        val typeIInterceptor = this.elements.getTypeElement(I_INTERCEPTOR)
        val typeIInterceptorGroup = this.elements.getTypeElement(I_INTERCEPTOR_GROUP)

        /**
         * Build input type, format as : 存放拦截器的接口类
         * ```Map<Integer, Class<? extends IInterceptor>>```
         */
        val parameterizedTypeNameOfInterceptor = ParameterizedTypeName.get(
            ClassName.get(Map::class.java),
            ClassName.get(Integer::class.java),
            ParameterizedTypeName.get(
                ClassName.get(Class::class.java),
                WildcardTypeName.subtypeOf(ClassName.get(typeIInterceptor))
            )
        )

        /**
         * Build input param name.
         * namely ```interceptors: Map<Integer, Class<? extends IInterceptor>>```
         */
        val paramSpec = ParameterSpec.builder(parameterizedTypeNameOfInterceptor, "interceptors").build()

        /**
         * Build method : 'loadInto'
         * @Override
         * public void loadInto(Map<Integer, Class<? extends IInterceptor>> interceptors) {}
         */
        val loadIntoMethodSpecBuilder =
            MethodSpec.methodBuilder(METHOD_LOAD_INTO).addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC)
                .addParameter(paramSpec)
        // 填充构建RouterX$$Interceptors$$信息，生成对应代码
        if (interceptors.isNotEmpty()) {
            for (entry in interceptors) {
                loadIntoMethodSpecBuilder.addStatement(
                    "interceptors.put(${entry.key}, \$T.class)",
                    ClassName.get(entry.value as TypeElement)
                )
            }
        }
        // 生成RouterX$$Interceptors$$[moduleName] 拦截器组注册接口类
        JavaFile.builder(
            PACKAGE_GENERATED, TypeSpec.classBuilder("$INTERCEPTORS_NAME$moduleName")
                .addModifiers(Modifier.PUBLIC).addJavadoc(APT_WARNING_TIP).addMethod(loadIntoMethodSpecBuilder.build())
                .addSuperinterface(ClassName.get(typeIInterceptorGroup))
                .build()
        ).build().writeTo(filer)
        logger.info(">>> Interceptor group write over. <<<");
    }
}