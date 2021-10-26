package me.bytebeats.routerx.apt

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/25 20:09
 * @Version 1.0
 * @Description constants
 */
/* Generated */
internal const val SEPARATOR = "$$"
internal const val PROJECT = "RouterX"
internal const val PACKAGE_OF_ROUTERX = "me.bytebeats.routerx"
internal const val TAG = "$PROJECT::"
internal const val APT_WARNING_TIP = "DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY RouterX"
internal const val METHOD_LOAD_INTO = "loadInto"
internal const val METHOD_INJECT = "inject"
internal const val ROOT_NAME = "$PROJECT${SEPARATOR}Root$SEPARATOR"
internal const val PROVIDER_NAME = "$PROJECT${SEPARATOR}Providers$SEPARATOR"
internal const val GROUP_NAME = "$PROJECT${SEPARATOR}Group$SEPARATOR"
internal const val INTERCEPTORS_NAME = "$PROJECT${SEPARATOR}Interceptors$SEPARATOR"
internal const val AUTOWIRED_NAME = "$SEPARATOR$PROJECT${SEPARATOR}AutoWired"
internal const val PACKAGE_GENERATED = "$PACKAGE_OF_ROUTERX.route"

/* Android classes */
internal const val ACTIVITY = "androidx.activity.ComponentActivity"
internal const val SERVICE = "android.app.Service"
internal const val CONTENT_PROVIDER = "android.content.ContentProvider"
internal const val RECEIVER = "android.content.BroadcastReceiver"
internal const val FRAGMENT = "androidx.fragment.app.Fragment"
internal const val PARCELABLE = "android.os.Parcelable"

// Java primitive types
private const val LANG = "java.lang"
internal const val BYTE = "$LANG.Byte"
internal const val SHORT = "$LANG.Short"
internal const val INTEGER = "$LANG.Integer"
internal const val LONG = "$LANG.Long"
internal const val FLOAT = "$LANG.Float"
internal const val DOUBLE = "$LANG.Double"
internal const val BOOLEAN = "$LANG.Boolean"
internal const val STRING = "$LANG.String"

// Custom interface
private const val FACADE_PACKAGE = "$PACKAGE_OF_ROUTERX.core.facade"
private const val TEMPLATE_PACKAGE = ".template"
private const val SERVICE_PACKAGE = ".service"
internal const val I_PROVIDER = "$FACADE_PACKAGE$TEMPLATE_PACKAGE.IProvider"
internal const val I_PROVIDER_GROUP = "$FACADE_PACKAGE$TEMPLATE_PACKAGE.IProviderGroup"
internal const val I_INTERCEPTOR = "$FACADE_PACKAGE$TEMPLATE_PACKAGE.IInterceptor"
internal const val I_INTERCEPTOR_GROUP = "$FACADE_PACKAGE$TEMPLATE_PACKAGE.IInterceptorGroup"
internal const val I_ROUTE_ROOT = "$FACADE_PACKAGE$TEMPLATE_PACKAGE.IRouteRoot"
internal const val I_ROUTE_GROUP = "$FACADE_PACKAGE$TEMPLATE_PACKAGE.IRouteGroup"
internal const val I_SYRINGE = "$FACADE_PACKAGE$TEMPLATE_PACKAGE.ISyringe"
internal const val SERIALIZATION_SERVICE = "$FACADE_PACKAGE$SERVICE_PACKAGE.SerializationService"

/* Log */
const val PREFIX_OF_LOGGER = "$PROJECT::APT "

/* Options of processor */
const val KEY_MODULE_NAME = "moduleName"

/* Annotation types */
internal const val ANNOTATION_TYPE_INTERCEPTOR = "$PACKAGE_OF_ROUTERX.annotation.Interceptor"
internal const val ANNOTATION_TYPE_ROUTE = "$PACKAGE_OF_ROUTERX.annotation.Router"
internal const val ANNOTATION_TYPE_AUTOWIRED = "$PACKAGE_OF_ROUTERX.annotation.AutoWired"