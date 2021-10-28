package me.bytebeats.routerx.core.facade

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.activity.ComponentActivity
import androidx.annotation.IntDef
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import me.bytebeats.routerx.annotation.enums.TargetType
import me.bytebeats.routerx.core.facade.callback.OnNavigationListener
import me.bytebeats.routerx.core.facade.service.SerializationService
import me.bytebeats.routerx.core.facade.template.IProvider
import java.io.Serializable

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 15:56
 * @Version 1.0
 * @Description 路由路标的容器，存放了路由信息、数据内容、切换动画等
 */
/**
 * @param path      路由路径
 * @param group     路由所在的组名
 * @param uri       统一资源标识符
 * @param bundle    路由携带的数据
 */
class Postcard(
    var path: String? = null,
    var group: String? = null,
    var uri: Uri? = null,
    var bundle: Bundle = Bundle()
) {
    /*  暂时存放出错的信息  */
    var tag: Any? = null

    /*  路由的flags  */
    var flags: Int = -1

    /*  路由超时时间（包括拦截器执行的时间，单位: 秒）  */
    var timeout: Int = 300

    /*  RouteType = PROVIDER 时，将会被赋值  */
    var provider: IProvider? = null

    /*  绿色通道，跳过所有的拦截器  */
    var greenChannel: Boolean = false

    /*  序列化服务  */
    var serializationService: SerializationService? = null

    /*  activity的切换动画  */
    var optionsCompat: Bundle? = null

    /*  进入动画  */
    var enterAnim: Int = 0

    /*  退出动画  */
    var exitAnim: Int = 0

    /*  增加设置intent的action  */
    var action: String? = null

    /* 路由目标类 */
    var destination: Class<*>? = null

    /* 路由的优先级【数字越小，优先级越高】 */
    var priority: Int = -1

    /* 路由的目标类型 */
    var type: TargetType? = null

    /* 拓展属性 */
    var extras: Int = 0

    fun getData(): Bundle = bundle

    /**
     * 路由导航
     *
     * @param context Activity and so on.
     * @param listener OnNavigationListener or null.
     */
    @JvmOverloads
    fun navigation(context: Context? = null, listener: OnNavigationListener? = null) {
        // TODO: 2021/10/28 implement this by RouterX
    }

    /**
     * 路由导航（ComponentActivity#startActivityForResult）
     *
     * @param activity  Activity.
     * @param requestCode   startActivityForResult's requestCode
     * @param listener  OnNavigationListener or null
     */
    @JvmOverloads
    fun navigation(activity: ComponentActivity, requestCode: Int, listener: OnNavigationListener? = null) {
        // TODO: 2021/10/28 implement this by RouterX
    }

    /**
     * 路由导航（Fragment#startActivityForResult）
     *
     * @param fragment    fragment
     * @param requestCode startActivityForResult's param
     * @param listener  OnNavigationListener or null
     */
    @JvmOverloads
    fun navigation(fragment: Fragment, requestCode: Int, listener: OnNavigationListener? = null) {
        // TODO: 2021/10/28 implement this by RouterX
    }

    /**
     * Set special flags controlling how this intent is handled.  Most values
     * here depend on the type of component being executed by the Intent,
     * specifically the FLAG_ACTIVITY_* flags are all for use with
     * {@link Context#startActivity Context.startActivity()} and the
     * FLAG_RECEIVER_* flags are all for use with
     * {@link Context#sendBroadcast(Intent) Context.sendBroadcast()}.
     */
    fun withFlag(@FlagInt flag: Int): Postcard = apply { flags = flag }

    /**
     * 设置Object参数【使用前需要设置序列化服务 {@link SerializationService}】
     *
     * @param key   a String, or null
     * @param value a Object, or null
     */
    fun withObject(key: String?, value: Any?): Postcard = apply {
//        serializationService =
        // TODO: 2021/10/28 serializationService
        bundle.putString(key, serializationService?.toJson(value))
    }

    // Follow api copy from #{Bundle}

    /**
     * Inserts a String value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a String, or null
     */
    fun withString(key: String?, value: String?): Postcard = apply { bundle.putString(key, value) }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a boolean
     */
    fun withBoolean(key: String?, value: Boolean): Postcard = apply { bundle.putBoolean(key, value) }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a short
     */
    fun withShort(key: String?, value: Short): Postcard = apply { bundle.putShort(key, value) }

    /**
     * Inserts an int value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value an int
     */
    fun withInt(key: String?, value: Int): Postcard = apply { bundle.putInt(key, value) }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a long
     */
    fun withLong(key: String?, value: Long): Postcard = apply { bundle.putLong(key, value) }

    /**
     * Inserts a double value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a double
     */
    fun withDouble(key: String?, value: Double): Postcard = apply { bundle.putDouble(key, value) }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a byte
     */
    fun withByte(key: String?, value: Byte): Postcard = apply { bundle.putByte(key, value) }

    /**
     * Inserts a char value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a char
     */
    fun withChar(key: String?, value: Char): Postcard = apply { bundle.putChar(key, value) }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key   a String, or null
     * @param value a float
     */
    fun withFloat(key: String?, value: Float): Postcard = apply { bundle.putFloat(key, value) }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a CharSequence, or null
     */
    fun withCharSequence(key: String?, value: CharSequence?): Postcard = apply { bundle.putCharSequence(key, value) }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Parcelable object, or null
     */
    fun withParcelable(key: String?, value: Parcelable?): Postcard = apply { bundle.putParcelable(key, value) }

    /**
     * Inserts an array of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key   a String, or null
     * @param value an array of Parcelable objects, or null
     */
    fun withParcelableArray(key: String?, value: Array<out Parcelable>?) =
        apply { bundle.putParcelableArray(key, value) }

    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key.  Either key or value may
     * be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList of Parcelable objects, or null
     */
    fun withParcelableArrayList(key: String?, value: ArrayList<out Parcelable>?) =
        apply { bundle.putParcelableArrayList(key, value) }

    /**
     * Inserts a SparceArray of Parcelable values into the mapping of this
     * Bundle, replacing any existing value for the given key.  Either key
     * or value may be null.
     *
     * @param key   a String, or null
     * @param value a SparseArray of Parcelable objects, or null
     */
    fun withSparseParcelableArray(key: String?, value: SparseArray<out Parcelable>?) =
        apply { bundle.putSparseParcelableArray(key, value) }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     */
    fun withIntegerArrayList(key: String?, value: ArrayList<Int>?) =
        apply { bundle.putIntegerArrayList(key, value) }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     */
    fun withStringArrayList(key: String?, value: ArrayList<String>?) =
        apply { bundle.putStringArrayList(key, value) }

    /**
     * Inserts an ArrayList value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value an ArrayList object, or null
     */
    fun withCharSequenceArrayList(key: String?, value: ArrayList<CharSequence>?) =
        apply { bundle.putCharSequenceArrayList(key, value) }

    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Serializable object, or null
     */
    fun withSerializable(key: String?, value: Serializable?) = apply { bundle.putSerializable(key, value) }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a byte array object, or null
     */
    fun withByteArray(key: String?, value: ByteArray?) = apply { bundle.putByteArray(key, value) }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a short array object, or null
     */
    fun withShortArray(key: String?, value: ShortArray?) = apply { bundle.putShortArray(key, value) }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a char array object, or null
     */
    fun withCharArray(key: String?, value: CharArray?) = apply { bundle.putCharArray(key, value) }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a float array object, or null
     */
    fun withFloatArray(key: String?, value: FloatArray?) = apply { bundle.putFloatArray(key, value) }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a CharSequence array object, or null
     */
    fun withCharSequenceArray(key: String?, value: Array<CharSequence>?) =
        apply { bundle.putCharSequenceArray(key, value) }

    /**
     * Inserts a int array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a float array object, or null
     */
    fun withIntArray(key: String?, value: IntArray?) = apply { bundle.putIntArray(key, value) }

    /**
     * Inserts a Bundle value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key   a String, or null
     * @param value a Bundle object, or null
     */
    fun withBundle(key: String?, value: Bundle?) = apply { bundle.putBundle(key, value) }

    /**
     * 设置切换动画
     *
     * @param enterAnim 进入的动画
     * @param exitAnim  退出的动画
     */
    fun withTransition(enterAnim: Int, exitAnim: Int): Postcard = apply {
        this@Postcard.enterAnim = enterAnim
        this@Postcard.exitAnim = exitAnim
    }

    /**
     * Set options compat
     *
     * @param optionsCompat compat
     * @return this
     */
    fun withOptionsCompat(optionsCompat: ActivityOptionsCompat): Postcard =
        apply { this@Postcard.optionsCompat = optionsCompat.toBundle() }

    override fun toString(): String {
        return "Postcard(path=$path, group=$group, uri=$uri, bundle=$bundle, tag=$tag, flags=$flags, timeout=$timeout, provider=$provider, greenChannel=$greenChannel, serializationService=$serializationService, optionsCompat=$optionsCompat, enterAnim=$enterAnim, exitAnim=$exitAnim, action=$action)"
    }

    @IntDef(
        Intent.FLAG_ACTIVITY_SINGLE_TOP,
        Intent.FLAG_ACTIVITY_NEW_TASK,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        Intent.FLAG_DEBUG_LOG_RESOLUTION,
        Intent.FLAG_FROM_BACKGROUND,
        Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT,
        Intent.FLAG_ACTIVITY_CLEAR_TASK,
        Intent.FLAG_ACTIVITY_CLEAR_TOP,
        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
        Intent.FLAG_ACTIVITY_FORWARD_RESULT,
        Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY,
        Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
        Intent.FLAG_ACTIVITY_NO_ANIMATION,
        Intent.FLAG_ACTIVITY_NO_USER_ACTION,
        Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP,
        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED,
        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
        Intent.FLAG_ACTIVITY_TASK_ON_HOME,
        Intent.FLAG_RECEIVER_REGISTERED_ONLY
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class FlagInt
}