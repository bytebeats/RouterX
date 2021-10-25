package me.bytebeats.routerx.annotation.enums

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/25 19:25
 * @Version 1.0
 * @Description 被{@link AutoWired}标注的字段的类型
 */

enum class DataType {
    // primitive type
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    CHAR,
    FLOAT,
    DOUBLE,

    // other data type
    STRING,
    PARCELABLE,
    ANY;
}