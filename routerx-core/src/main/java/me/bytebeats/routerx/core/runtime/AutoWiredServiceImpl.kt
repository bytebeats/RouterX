package me.bytebeats.routerx.core.runtime

import android.content.Context
import androidx.collection.LruCache
import me.bytebeats.routerx.annotation.Router
import me.bytebeats.routerx.core.facade.service.AutoWiredService
import me.bytebeats.routerx.core.facade.template.ISyringe
import me.bytebeats.routerx.core.util.ROUTE_SERVICE_AUTOWIRED
import me.bytebeats.routerx.core.util.SUFFIX_AUTOWIRED

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 21:48
 * @Version 1.0
 * @Description 全局自动注入属性服务
 */

@Router(path = ROUTE_SERVICE_AUTOWIRED)
class AutoWiredServiceImpl : AutoWiredService {
    /**
     * 存放自动注入属性的注射器缓存
     */
    private lateinit var mClassCache: LruCache<String, ISyringe>

    /**
     * 存放不需要自动注入属性的类类名
     */
    private lateinit var mBlackList: MutableSet<String>


    override fun init(context: Context) {
        mClassCache = LruCache(60)
        mBlackList = mutableSetOf()
    }

    override fun autoWire(instance: Any) {
        val className = instance.javaClass.name
        try {
            if (!mBlackList.contains(className)) {
                var autoWireSyringe = mClassCache.get(className)
                if (autoWireSyringe == null) {
                    ////根据生成规则反射生成APT自动生成的自动依赖注入注射器，如果没有对应的类可生成，证明该类无需自动注入属性
                    autoWireSyringe =
                        Class.forName("$className$SUFFIX_AUTOWIRED").getConstructor().newInstance() as ISyringe
                }
                autoWireSyringe.inject(instance)
                mClassCache.put(className, autoWireSyringe)
            }
        } catch (ignore: Exception) {//反射生成自动依赖注入注射器失败，证明该类无需自动注入属性
            mBlackList.add(className)
        }
    }
}