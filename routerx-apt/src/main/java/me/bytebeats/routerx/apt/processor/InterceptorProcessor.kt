package me.bytebeats.routerx.apt.processor

import com.google.auto.service.AutoService
import me.bytebeats.routerx.apt.*
import me.bytebeats.routerx.apt.ANNOTATION_TYPE_INTERCEPTOR
import me.bytebeats.routerx.apt.INTERCEPTOR
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/25 21:04
 * @Version 1.0
 * @Description TO-DO
 */

/**
 * Process the annotation of {@link Interceptor}
 * 自动生成IInterceptor注册接口 XRouter$$Interceptors$$[moduleName]
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

    private lateinit var interceptor: TypeMirror

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
            interceptor = elements.getTypeElement(INTERCEPTOR).asType()
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

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        return true
    }
}