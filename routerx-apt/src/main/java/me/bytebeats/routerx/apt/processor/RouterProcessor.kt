package me.bytebeats.routerx.apt.processor

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import me.bytebeats.routerx.annotation.AutoWired
import me.bytebeats.routerx.annotation.Router
import me.bytebeats.routerx.annotation.enums.TargetType
import me.bytebeats.routerx.annotation.meta.RouteMeta
import me.bytebeats.routerx.apt.*
import java.io.IOException
import java.util.*
import java.util.concurrent.Flow
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.jvm.Throws


/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/26 16:12
 * @Version 1.0
 * @Description Process the annotation of {@link Router}
 *
 * <p>自动生成路由组注册接口 XRouter$$Group$$[groupName] </p>
 * <p>自动生成根路由注册接口 XRouter$$Root$$[moduleName] </p>
 * <p>自动生成IProvider注册接口 XRouter$$Providers$$[moduleName] </p>
 */

@AutoService(Flow.Processor::class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(ANNOTATION_TYPE_ROUTE, ANNOTATION_TYPE_AUTOWIRED)
class RouterProcessor : AbstractProcessor() {

    /**
     * 组路由表【GroupName and routeMetas】
     */
    private val rootGroupRouteMetas = mutableMapOf<String, TreeSet<RouteMeta>>()

    /**
     * 根路由表【GroupName and GroupFileName】，用于自动生成路由组注册信息
     * Map of root metas, used for generate class file in order.
     */
    private val groupFiles = sortedMapOf<String, String>()

    /**
     * 写Java文件到disk
     */
    private lateinit var mFiler: Filer

    /**
     * 日志打印工具
     */
    private lateinit var mLogger: Logger

    /**
     * 类型工具
     */
    private lateinit var mTypes: Types

    /**
     * 获取类的工具
     */
    private lateinit var mElements: Elements
    private lateinit var dataTypeUtils: DataTypeUtils

    /**
     * 模块名，可以是'app'或者其他
     */
    private lateinit var sModuleName: String
    private lateinit var mProviderType: TypeMirror

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        processingEnv?.also {
            mFiler = it.filer
            mTypes = it.typeUtils
            mElements = it.elementUtils

            dataTypeUtils = DataTypeUtils(mTypes, mElements)

            mLogger = Logger(it.messager)

            obtainModuleName(it)

            mProviderType = mElements.getTypeElement(I_PROVIDER).asType()

            mLogger.info(">>> RouterProcessor init. <<<");
        }
    }

    private fun obtainModuleName(processingEnv: ProcessingEnvironment?) {
        val options = processingEnv?.options ?: throw IllegalArgumentException("ProcessingEnvironment is null")
        if (options.isNotEmpty()) {
            sModuleName = options[KEY_MODULE_NAME] ?: ""
        }
        if (sModuleName.isNotEmpty()) {
            sModuleName = sModuleName.replace("[^0-9a-zA-Z_]+".toRegex(), "")
            mLogger.info("The user has configuration the module name, it was [$sModuleName]")
        } else {
            mLogger.error(
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
     * 扫描被{@link Router}注解所修饰的类
     *
     * @param annotations
     * @param roundEnv
     */
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (!annotations.isNullOrEmpty()) {
            try {
                roundEnv?.getElementsAnnotatedWith(Router::class.java)?.let {
                    parseRouters(it)
                }
            } catch (ignore: IOException) {
                mLogger.error(ignore)
            } catch (ignore: IllegalArgumentException) {
                mLogger.error(ignore)
            }
            return true
        }
        return false
    }

    /**
     * 解析路由创建注解{@link Router}
     *
     * @param elements
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    private fun parseRouters(elements: Set<Element>) {
        if (elements.isEmpty()) return
        /* Prepare the type and so on */
        mLogger.info(">>> Found routes, size is ${elements.size} <<<")
        groupFiles.clear()

        val typeActivity = mElements.getTypeElement(ACTIVITY).asType()
        val typeFragment = mElements.getTypeElement(FRAGMENT).asType()
        val typeService = mElements.getTypeElement(SERVICE).asType()

        /* RouterX interfaces */
        val typeRouteGroup = mElements.getTypeElement(I_ROUTE_GROUP).asType()
        val typeProviderGroup = mElements.getTypeElement(I_PROVIDER_GROUP).asType()
        val routeMetaClassName = ClassName.get(RouteMeta::class.java)
        val targetTypeClassName = ClassName.get(TargetType::class.java)

        /**
         * Build input type, format as :  存放路由组的接口类（生成应用根路由）
         * ```Map<String, Class<? extends IRouteGroup>>```
         * */
        val rootGroupParamTypeName = ParameterizedTypeName.get(
            ClassName.get(java.util.Map::class.java),
            ClassName.get(java.lang.String::class.java),
            ParameterizedTypeName.get(
                ClassName.get(Class::class.java),
                WildcardTypeName.subtypeOf(ClassName.get(typeRouteGroup))
            )
        )

        /**
         *
         * Build input type, format as : 存放路由表的接口类（生成路由组）
         * ```Map<String, RouteInfo>```
         */
        val groupFileParamTypeName = ParameterizedTypeName.get(
            ClassName.get(java.util.Map::class.java),
            ClassName.get(java.lang.String::class.java),
            ClassName.get(RouteMeta::class.java)
        )

        /**
         * Build input param name.
         */
        val rootGroupParamSpec = ParameterSpec.builder(rootGroupParamTypeName, "rootGroups").build()
        val groupMetaParamSpec = ParameterSpec.builder(groupFileParamTypeName, "routeMetas").build()
        /*  providerParamSpec is same with groupMetaParamSpec!  */
        val providerParamSpec = ParameterSpec.builder(groupFileParamTypeName, "providers").build()

        /**
         * Build method : 'loadInto'
         * @Override
         * public void loadInto(Map<String, Class<? extends IRouteGroup>> routeGroups) {}
         */
        val loadIntoMethodBuilderOfRoot =
            MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(rootGroupParamSpec)

        /*  扫描所有被@Router注解的类，构建RouteInfo，然后将他们进行分组，填充groupMap  */
        elements.forEach { element ->//TypeElement annotated with Router
            val type = element.asType()
            val router = element.getAnnotation(Router::class.java)
            val routeMeta = if (mTypes.isSubtype(type, typeActivity)) {//it's An Activity
                mLogger.info(">>> Found Activity router: $type <<<")
                val paramsType = mutableMapOf<String, Int>()
                for (field in element.enclosedElements) {
                    if (field.kind.isField && field.getAnnotation(AutoWired::class.java) != null && !mTypes.isSubtype(
                            field.asType(), mProviderType
                        )) {// It must be a field, then it is annotated, but it is not a provider.
                        val fieldSetting = field.getAnnotation(AutoWired::class.java)
                        paramsType[fieldSetting.name.ifEmpty { field.simpleName.toString() }] =
                            dataTypeUtils.fetchJavaDataType(field)
                    }
                }
                RouteMeta.build(
                    router = router,
                    rawType = element,
                    type = TargetType.ACTIVITY,
                    paramsType = null
                )
            } else if (mTypes.isSubtype(type, mProviderType)) {// it's an IProvider
                mLogger.info(">>> Found Provider router: $type <<<")
                RouteMeta.build(
                    router = router,
                    rawType = element,
                    type = TargetType.PROVIDER,
                    paramsType = null
                )
            } else if (mTypes.isSubtype(type, typeService)) {// it's a Service
                mLogger.info(">>> Found Service router: $type <<<")
                RouteMeta.build(
                    router = router,
                    rawType = element,
                    type = TargetType.SERVICE,
                    paramsType = null
                )
            } else if (mTypes.isSubtype(type, typeFragment)) {//it's a Fragment
                mLogger.info(">>> Found fragment router: $type <<<")
                RouteMeta.build(
                    router = router,
                    rawType = element,
                    type = TargetType.FRAGMENT,
                    paramsType = null
                )
            } else {
                throw RuntimeException("$PREFIX_OF_LOGGER>>> Found unsupported class type, type = [$mTypes].")
            }
            categorize(routeMeta)
        }

        /**
         * Build method : 'loadInto'
         * @Override
         * public void loadInto(Map<String, RouteInfo> providers) {}
         * */
        val loadIntoMethodBuilderOfProvider = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(providerParamSpec)

        /*  开始自动生成路由信息注册代码  */
        for (groupRoutMeta in rootGroupRouteMetas) {
            val group = groupRoutMeta.key

            /**
             * Build method : 'loadInto'
             * @Override
             * public void loadInto(Map<String, RouteInfo> routeMetas) {}
             */
            val loadIntoMethodBuilderOfGroup = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(groupMetaParamSpec)
            for (routeMeta in groupRoutMeta.value) {
                when (routeMeta.type) {
                    TargetType.PROVIDER -> {//cache provider's super class
                        val interfaces = (routeMeta.rawType as TypeElement).interfaces
                        for (tm in interfaces) {//TypeMirror
                            if (mTypes.isSameType(tm, mProviderType)) {
                                // It implements iProvider interface himself.（IProvider的实现类，就将该类的类名作为Key）
                                loadIntoMethodBuilderOfProvider.addStatement(
                                    "providers.put(\$S, \$T.build(\$T.${routeMeta.type}, \$T.class, \$S, \$S, null, ${routeMeta.priority}, ${routeMeta.extras}))",
                                    routeMeta.rawType.toString(),
                                    routeMetaClassName,
                                    targetTypeClassName,
                                    ClassName.get(routeMeta.rawType as TypeElement),
                                    routeMeta.path,
                                    routeMeta.group
                                )
                            } else if (mTypes.isSubtype(tm, mProviderType)) {
                                //（接口是IProvider的继承类，就将该接口类的类名作为Key
                                loadIntoMethodBuilderOfProvider.addStatement(
                                    "providers.put(\$S, \$T.build(\$T.${routeMeta.type}, \$T.class, \$S, \$S, null, ${routeMeta.priority}, ${routeMeta.extras}))",
                                    tm,
                                    routeMetaClassName,
                                    targetTypeClassName,
                                    ClassName.get(routeMeta.rawType as TypeElement),
                                    routeMeta.path,
                                    routeMeta.group
                                )
                            }
                        }
                    }
                    else -> {
                    }
                }
                /*  填充构建RouterX$$Group$$信息，生成对应代码  */
                val bodyBuilder = StringBuilder()
                routeMeta.paramsType?.let {
                    for (typeEntry in it) {
                        bodyBuilder.append("put(\"${typeEntry.key}\", ${typeEntry.value});")
                    }
                }
                val body = bodyBuilder.toString()
                loadIntoMethodBuilderOfGroup.addStatement(
                    "routeMetas.put(\$S, \$T.build(\$T.${routeMeta.type}, \$T.class, \$S, \$S, ${if (body.isEmpty()) null else "new java.util.HashMap<String, Integer>(){{$bodyBuilder}}"}, ${routeMeta.priority}, ${routeMeta.extras}))",
                    routeMeta.path,
                    routeMetaClassName,
                    targetTypeClassName,
                    ClassName.get(routeMeta.rawType as TypeElement),
                    routeMeta.path.lowercase(),
                    routeMeta.group?.lowercase()
                )
            }
            /*  生成RouterX$$Group$$[groupName] 路由注册接口类  */
            val groupFileName = "$GROUP_NAME$group"
            JavaFile.builder(
                PACKAGE_GENERATED,
                TypeSpec.classBuilder(groupFileName)
                    .addJavadoc(APT_WARNING_TIP)
                    .addSuperinterface(ClassName.get(typeRouteGroup))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(loadIntoMethodBuilderOfGroup.build())
                    .build()
            ).build().writeTo(mFiler)
            mLogger.info(">>> Generated group map, name is : $group<<<")
            groupFiles[group] = groupFileName//注册Group的信息，为下面构建根路由提供数据
        }

        for (groupFile in groupFiles) {
            /* 根据Group的信息，填充构建RouterX$$Root$$信息，生成对应代码 */
            loadIntoMethodBuilderOfRoot.addStatement(
                "routeGroups.put(\$S, \$T.class)",
                groupFile.key,// 路由组名
                ClassName.get(PACKAGE_GENERATED, groupFile.value)// 路由组注册接口的类名
            )
        }

        /*  生成RouterX$$Providers$$[moduleName] provider注册接口类  */
        val providersFileName = "$PROVIDER_NAME$sModuleName"
        JavaFile.builder(
            PACKAGE_GENERATED, TypeSpec.classBuilder(providersFileName)
                .addJavadoc(APT_WARNING_TIP)
                .addSuperinterface(ClassName.get(typeProviderGroup))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(loadIntoMethodBuilderOfProvider.build())
                .build()
        )
            .build()
            .writeTo(mFiler)

        mLogger.info(">>> Generated provider map, name is : $providersFileName <<<")

        /*  生成XRouter$$Root$$[moduleName] 根路由注册接口类  */
        val rootFileName = "$ROOT_NAME$sModuleName"
        JavaFile.builder(
            PACKAGE_GENERATED, TypeSpec.classBuilder(rootFileName)
                .addJavadoc(APT_WARNING_TIP)
                .addSuperinterface(ClassName.get(mElements.getTypeElement(I_ROUTE_ROOT)))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(loadIntoMethodBuilderOfRoot.build())
                .build()
        )
            .build()
            .writeTo(mFiler)

        mLogger.info(">>> Generated root map, name is : $rootFileName <<<")
    }

    /**
     * 进行路由分组
     *
     * @param meta 路由元数据.
     */
    private fun categorize(meta: RouteMeta) {
        if (verifyRoute(meta)) {
            mLogger.info(">>> Start categories, group = ${meta.group}, path = ${meta.path} <<<")
            val routeMetas = rootGroupRouteMetas[meta.group]
            if (routeMetas.isNullOrEmpty()) {
                val metas = sortedSetOf<RouteMeta>(comparator = { meta1, meta2 ->
                    try {
                        meta1.path.compareTo(meta2.path)
                    } catch (npe: NullPointerException) {
                        mLogger.error(npe.message)
                        0
                    }
                })
                metas.add(meta)
                meta.group?.let { rootGroupRouteMetas.put(it, metas) }
            } else {
                routeMetas.add(meta)
            }
        } else {
            mLogger.warning(">>> RouteInfo verify error, group is : ${meta.group} <<<")
        }
    }

    /**
     * 验证路由信息的有效性（没有填写路由组，就默认截取路由路径中的第一位"/"之前的字符作为组名
     *
     * @param meta 路由元数据.
     */
    private fun verifyRoute(meta: RouteMeta): Boolean {
        val path = meta.path
        if (path.isEmpty() || !path.startsWith('/')) {
            return false
        }
        return if (meta.group.isNullOrEmpty()) {
            try {
                val defaultGroup = path.substring(0, path.indexOf('/', 1))
                if (defaultGroup.isEmpty()) {
                    false
                } else {
                    meta.group = defaultGroup
                    true
                }
            } catch (exp: Exception) {
                mLogger.error("Failed to extract default group! ${exp.message}")
                false
            }
        } else true
    }
}