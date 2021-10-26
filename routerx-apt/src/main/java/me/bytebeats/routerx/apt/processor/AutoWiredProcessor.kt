package me.bytebeats.routerx.apt.processor

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import me.bytebeats.routerx.annotation.AutoWired
import me.bytebeats.routerx.annotation.enums.DataType
import me.bytebeats.routerx.apt.*
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/26 11:52
 * @Version 1.0
 * @Description Process the annotation of {@link AutoWired}
 * <p>自动生成依赖注入的辅助类 [ClassName]$$XRouter$$AutoWired </p>
 */

@AutoService(Processor::class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(ANNOTATION_TYPE_AUTOWIRED)
class AutoWiredProcessor : AbstractProcessor() {
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
    private lateinit var mDataTypeUtils: DataTypeUtils

    /**
     * 获取类的工具
     */
    private lateinit var mElements: Elements

    //class annotated with AutoWired and its fields declared by itself or its parent
    private val classToFields = mutableMapOf<TypeElement, MutableList<Element>>()

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        processingEnv?.also {
            mFiler = it.filer // create java file
            mTypes = it.typeUtils
            mElements = it.elementUtils
            mDataTypeUtils = DataTypeUtils(mTypes, mElements)

            mLogger = Logger(it.messager)
            mLogger.info(">>> AutoWiredProcessor init. <<<");
        }
    }


    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (!annotations.isNullOrEmpty()) {
            try {
                roundEnv?.getElementsAnnotatedWith(AutoWired::class.java)?.let {
                    parseAutoWired(it)
                }
            } catch (ignored: IOException) {
                mLogger.error(ignored)
            } catch (ignored: IllegalArgumentException) {
                mLogger.error(ignored)
            }
            return true
        }
        return false
    }

    /**
     * 解析自动依赖注入的注解{@link AutoWired}
     *
     * @param elements 被@AutoWired修饰的字段
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    private fun parseAutoWired(elements: Set<Element>) {
        mLogger.info(">>> Found AutoWired fields, start... <<<")
        /* 对AutoWired字段按所在包装类的类名进行分类 */
        categorize(elements)
        /* 生成对应的依赖注入代码 */
        generateInjectLogic()
    }

    /**
     * 分类字段，寻找他们所在的类（按父类进行分类）
     *
     * @param elements 被@AutoWired修饰的字段
     */
    @Throws(IllegalArgumentException::class)
    private fun categorize(elements: Set<Element>) {
        elements.forEach { element ->
            //getEnclosingElement--返回封装此元素的最里层元素, 即该字段所在的类。这里一般是Activity/Fragment。
            val enclosingElement = element.enclosingElement as TypeElement
            if (element.modifiers.contains(Modifier.PRIVATE)) {
                throw IllegalArgumentException(
                    "The inject fields CAN NOT BE 'PRIVATE'!!! please check field [${element.simpleName}] in class [${enclosingElement.qualifiedName}]"
                )
            }
            if (classToFields.containsKey(enclosingElement)) {
                classToFields[enclosingElement]?.add(element)
            } else {
                val fields = mutableListOf<Element>()
                fields.add(element)
                classToFields[enclosingElement] = fields
            }
        }
        mLogger.info("AutoWiredProcessor#categorize finished.")
    }

    /**
     * 生成依赖注入的代码
     *
     * @throws IOException
     * @throws IllegalAccessException
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    private fun generateInjectLogic() {
        val typeSyringe = mElements.getTypeElement(I_SYRINGE)
        val serialServiceType = mElements.getTypeElement(SERIALIZATION_SERVICE).asType()
        val typeProvider = mElements.getTypeElement(I_PROVIDER).asType()
        val typeActivity = mElements.getTypeElement(ACTIVITY).asType()
        val typeFragment = mElements.getTypeElement(FRAGMENT).asType()
        /* Build input param name.Object target */
        val targetParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build()

        for (entry in classToFields) {
            val superClass = entry.key//封装字段的最里层类
            val fields = entry.value

            val qualifiedName = superClass.qualifiedName.toString()
            val packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'))
            val fileName = "${superClass.simpleName}$AUTOWIRED_NAME"
            mLogger.info(">>> Start processing ${fields.size} fields in ${superClass.simpleName} ... <<<")
            /*  构建自动依赖注入代码的文件  */
            val injectHelper = TypeSpec.classBuilder(fileName)
                .addJavadoc(APT_WARNING_TIP)
                .addSuperinterface(ClassName.get(typeSyringe))
                .addModifiers(Modifier.PUBLIC)
            /*  private SerializationService serializationService  */
            val serialServiceFieldSpec =
                FieldSpec.builder(
                    TypeName.get(serialServiceType),
                    FIELD_NAME_OF_SERIALIZATION_SERVICE,
                    Modifier.PRIVATE
                ).build()
            injectHelper.addField(serialServiceFieldSpec)

            /*  Build method : 'inject'
                   @Override
                   public void inject(Object target) {
                       serializationService = XRouter.getInstance().navigation(SerializationService.class);
                       T substitute = (T)target;
                   }  */
            val injectMethodBuilder = MethodSpec.methodBuilder(METHOD_INJECT)
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(targetParamSpec)
                .addStatement(
                    "$FIELD_NAME_OF_SERIALIZATION_SERVICE = \$T.getInstance().navigation(\$T.class",
                    ROUTER_X_CLASS_NAME,
                    ClassName.get(serialServiceType)
                )
                .addStatement("\$T substitute = (\$)target", ClassName.get(superClass), ClassName.get(superClass))
            fields.forEach { field -> /*  生成依赖注入方法的主体, 开始实现依赖注入的方法  */
                val fieldSetting = field.getAnnotation(AutoWired::class.java)
                val fieldName = field.simpleName.toString()
                if (mTypes.isSubtype(field.asType(), typeProvider)) {//it's an IProvider
                    if (fieldSetting.name.isEmpty()) {// 没有设置服务provider的路径，直接使用类名寻找
                        injectMethodBuilder.addStatement(
                            "substitute.$fileName = \$T.getInstance().navigation(\$T.class);",
                            ROUTER_X_CLASS_NAME,
                            ClassName.get(field.asType())
                        )
                    } else {// 设置类服务provider的路径，使用路径寻找
                        injectMethodBuilder.addStatement(
                            "substitute.$fileName = \$T.getInstance().build(\$S).navigation();",
                            ClassName.get(field.asType()),
                            ROUTER_X_CLASS_NAME,
                            fieldSetting.name
                        )
                    }
                    if (fieldSetting.required) {// 增加校验"字段是否为NULL"的判断代码
                        injectMethodBuilder.beginControlFlow("if (substitute.$fieldName == null)")
                        injectMethodBuilder.addStatement(
                            "throw new RuntimeException(\"The field '$fieldName' is null, in class '\" + \$T.class.getName() + \"!\")",
                            ClassName.get(superClass)
                        )
                        injectMethodBuilder.endControlFlow()
                    }
                } else {// It's normal intent value
                    val originalValue = "substitute.$fieldName"
                    var statement = "substitute.$fieldName = substitute."
                    var isActivity = false
                    if (mTypes.isSubtype(superClass.asType(), typeActivity)) {//Activity, then use getIntent()
                        isActivity = true
                        statement = "${statement}getIntent()."
                    } else if (mTypes.isSubtype(superClass.asType(), typeFragment)) {//Fragment, then use getArguments()
                        isActivity = false
                        statement = "${statement}getArguments()."
                    } else {
                        throw IllegalArgumentException("The field [$fieldName] needs to be autowired from Intent, and its parent must be Activity or Fragment!")
                    }

                    statement = buildInjectStatement(
                        originalValue,
                        statement,
                        mDataTypeUtils.fetchJavaDataType(field),
                        isActivity
                    )

                    if (statement.startsWith("${FIELD_NAME_OF_SERIALIZATION_SERVICE}.")) {// 如果参数是Object，需要反序列化
                        injectMethodBuilder.beginControlFlow("if ($FIELD_NAME_OF_SERIALIZATION_SERVICE != null)")
                        injectMethodBuilder.addStatement(
                            "substitute.$fieldName = $statement",
                            fieldSetting.name.ifEmpty { fieldName },
                            ClassName.get(field.asType())
                        )
                        injectMethodBuilder.nextControlFlow("else")
                        injectMethodBuilder.addStatement(
                            "\$T.e(\"You want automatic inject the field '$fieldName' in class '\$T' , then you should implement 'SerializationService' to support object auto inject!\")",
                            RX_LOG_CLASS_NAME,
                            ClassName.get(superClass)
                        )
                        injectMethodBuilder.endControlFlow()
                    } else {
                        injectMethodBuilder.addStatement(statement, fieldSetting.name.ifEmpty { fieldName })
                    }

                    /* 增加校验"字段是否为NULL"的判断代码 */
                    if (fieldSetting.required && !field.asType().kind.isPrimitive) {// Primitive won't be checked
                        injectMethodBuilder.beginControlFlow("if (substitute.$fieldName == null)")
                        injectMethodBuilder.addStatement(
                            "\$T.e(\"The field '$fieldName' is null, in class '\" + \$T.class.getName() + \"!\")",
                            RX_LOG_CLASS_NAME,
                            ClassName.get(superClass)
                        )
                        injectMethodBuilder.endControlFlow()
                    }
                    /*  添加依赖注入的方法  */
                    injectHelper.addMethod(injectMethodBuilder.build())
                    /* 生成自动依赖注入的类文件[ClassName]$$XRouter$$AutoWired */
                    JavaFile.builder(packageName, injectHelper.build()).build().writeTo(mFiler)
                    mLogger.info(">>> ${superClass.simpleName} has been processed, $fileName has been generated. <<<")
                }
            }
        }
        mLogger.info(">>> AutoWired processor stop. <<<");
    }

    /**
     * 构建普通字段赋值[intent]的表达式
     *
     * @param originalValue 默认值
     * @param statement     表达式
     * @param type          值的类型
     * @param isActivity    是否是activity
     * @return
     */
    private fun buildInjectStatement(originalValue: String, statement: String, type: Int, isActivity: Boolean): String {
        return when (type) {
            DataType.BOOLEAN.ordinal -> "$statement${if (isActivity) "getBooleanExtra(\$S, $originalValue)" else "getBoolean(\$S)"}"
            DataType.BYTE.ordinal -> "$statement${if (isActivity) "getByteExtra(\$S, $originalValue)" else "getByte(\$S)"}"
            DataType.SHORT.ordinal -> "$statement${if (isActivity) "getShortExtra(\$S, $originalValue)" else "getShort(\$S)"}"
            DataType.INT.ordinal -> "$statement${if (isActivity) "getIntExtra(\$S, $originalValue)" else "getInt(\$S)"}"
            DataType.LONG.ordinal -> "$statement${if (isActivity) "getLongExtra(\$S, $originalValue)" else "getLong(\$S)"}"
            DataType.CHAR.ordinal -> "$statement${if (isActivity) "getCharExtra(\$S, $originalValue)" else "getChar(\$S)"}"
            DataType.FLOAT.ordinal -> "$statement${if (isActivity) "getFloatExtra(\$S, $originalValue)" else "getFloat(\$S)"}"
            DataType.DOUBLE.ordinal -> "$statement${if (isActivity) "getDoubleExtra(\$S, $originalValue)" else "getDouble(\$S)"}"
            DataType.STRING.ordinal -> "$statement${if (isActivity) "getStringExtra(\$S)" else "getString(\$S)"}"
            DataType.PARCELABLE.ordinal -> "$statement${if (isActivity) "getParcelableExtra(\$S)" else "getParcelable(\$S)"}"
            DataType.ANY.ordinal -> "serializationService.parseObject(substitute." + (if (isActivity) "getIntent()." else "getArguments().") + (if (isActivity) "getStringExtra(\$S)" else "getString(\$S)") + ", new me.bytebeats.routerx.annotation.meta.TypeWrapper<\$T>(){}.getType())"
            else -> statement
        }
    }

    companion object {
        private val ROUTER_X_CLASS_NAME = ClassName.get("me.bytebeats.routerx.core", "RouterX")
        private val RX_LOG_CLASS_NAME = ClassName.get("me.bytebeats.routerx.core", "RXLog")
        private const val FIELD_NAME_OF_SERIALIZATION_SERVICE = "serializationService"
    }
}