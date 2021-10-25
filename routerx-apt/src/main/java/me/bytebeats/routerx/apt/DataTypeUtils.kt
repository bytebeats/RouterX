package me.bytebeats.routerx.apt

import me.bytebeats.routerx.annotation.enums.DataType
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/25 20:51
 * @Version 1.0
 * @Description TO-DO
 */

class DataTypeUtils(private val types: Types, private val elements: Elements) {
    private val parcelableType = elements.getTypeElement(PARCELABLE).asType()

    fun fetchJavaDataType(element: Element): Int {
        val typeMirror = element.asType()
        /* primitive */
        return if (typeMirror.kind.isPrimitive) {
            typeMirror.kind.ordinal
        } else when (typeMirror.toString()) {
            BYTE -> DataType.BYTE.ordinal
            SHORT -> DataType.SHORT.ordinal
            INTEGER -> DataType.INT.ordinal
            LONG -> DataType.LONG.ordinal
            FLOAT -> DataType.FLOAT.ordinal
            DOUBLE -> DataType.DOUBLE.ordinal
            BOOLEAN -> DataType.BOOLEAN.ordinal
            STRING -> DataType.STRING.ordinal
            else -> if (types.isSubtype(typeMirror, parcelableType)) DataType.PARCELABLE.ordinal
            else DataType.ANY.ordinal
        }
    }
}