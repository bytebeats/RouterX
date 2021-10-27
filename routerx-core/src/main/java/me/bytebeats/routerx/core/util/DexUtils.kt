package me.bytebeats.routerx.core.util

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Build
import dalvik.system.DexFile
import me.bytebeats.routerx.core.RouterX
import me.bytebeats.routerx.core.concurrency.CancelableCountDownLatch
import me.bytebeats.routerx.core.concurrency.XPoolExecutor
import me.bytebeats.routerx.core.logger.RXLog
import java.io.File
import java.io.IOException
import java.util.regex.Pattern

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/27 11:57
 * @Version 1.0
 * @Description Dex Utils
 */

private const val EXTRACTED_NAME_EXT = ".classes"
private const val EXTRACTED_SUFFIX = ".zip"

private val SECONDARY_DIR_NAME = "code_cache" + File.separator.toString() + "secondary-dexes"

private const val MULTI_DEX_PREFS_FILE = "multidex.version"
private const val KEY_DEX_NUMBER = "dex.number"

private const val VM_WITH_MULTIDEX_VERSION_MAJOR = 2
private const val VM_WITH_MULTIDEX_VERSION_MINOR = 1

fun multiDexPreferences(context: Context): SharedPreferences =
    context.getSharedPreferences(MULTI_DEX_PREFS_FILE, Context.MODE_PRIVATE or Context.MODE_MULTI_PROCESS)

/**
 * 通过指定包名，扫描包下面包含的所有的ClassName
 * @param context     上下文
 * @param packageName 包名
 * @return 所有class的集合
 */
fun fileNamesByPackageName(context: Context, packageName: String): Set<String> {
    val classNames = mutableSetOf<String>()
    val paths = dexDirs(context)
    val parserCtl = CancelableCountDownLatch(paths.size)
    for (path in paths) {
        XPoolExecutor.getInstance().execute {
            var dexFile: DexFile? = null
            try {
                dexFile = if (path.endsWith(EXTRACTED_SUFFIX)) {
                    /*  NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"  */
                    DexFile.loadDex(path, "$path.tmp", 0)
                } else {
                    DexFile(path)
                }
                val dexEntries = dexFile?.entries() ?: return@execute
                while (dexEntries.hasMoreElements()) {
                    val className = dexEntries.nextElement()
                    if (className.startsWith(packageName)) {
                        classNames.add(className)
                    }
                }

            } catch (ignore: Exception) {
                RXLog.e("Scan map file in dex files made error.", ignore)
            } finally {
                try {
                    dexFile?.close()
                } catch (ignore: Exception) {
                    RXLog.e(ignore)
                }
                parserCtl.countDown()
            }
        }
    }
    parserCtl.await()
    RXLog.i("Filter ${classNames.size} classes by packageName <${packageName}>")
    return classNames
}

/**
 * 获取所有dex文件的目录
 *
 * @param context the application context
 * @return all the dex path
 * @throws PackageManager.NameNotFoundException
 * @throws IOException
 */
fun dexDirs(context: Context): List<String> {
    val dexDirs = mutableListOf<String>()

    val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
    val sourceApk = File(appInfo.sourceDir)

    /*  add the default apk path  */
    dexDirs.add(appInfo.sourceDir)

    /*  the prefix of extracted file, ie: test.classes  */
    val extractedFilePrefix = "${sourceApk.name}$EXTRACTED_NAME_EXT"
    // 如果VM已经支持了MultiDex，就不要去Secondary Folder加载 Classesx.zip了，那里已经么有了
    // 通过是否存在sp中的multidex.version是不准确的，因为从低版本升级上来的用户，是包含这个sp配置的
    if (!isMultiDexSupported()) {
        /*  the total dex numbers  */
        val totalDexCount = multiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1)
        val dexDir = File(appInfo.dataDir, SECONDARY_DIR_NAME)
        /*  for each dex file, ie: test.classes2.zip, test.classes3.zip...  */
        for (secondaryDexIdx in 2..totalDexCount) {
            val fileName = "$extractedFilePrefix$secondaryDexIdx$EXTRACTED_SUFFIX"
            val extractedFile = File(dexDir, fileName)
            if (extractedFile.isFile) {
                dexDirs.add(extractedFile.absolutePath)
                ////we ignore verifying zip part
            } else {
                throw IOException("Missing extracted secondary dex file '${extractedFile.path}'")
            }
        }
    }
    if (RouterX.debuggable()) {//Search instant run support only debuggable
        dexDirs.addAll(loadInstantRunDexFiles(appInfo))
    }
    return dexDirs
}

/**
 * Get instant run dex path, used to catch the branch usingApkSplits=false.
 */
fun loadInstantRunDexFiles(appInfo: ApplicationInfo?): List<String> {
    val instantRunDexFiles = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && appInfo != null) {
        instantRunDexFiles.addAll(appInfo.splitSourceDirs)
        RXLog.i("Found InstantRun support");
    } else {
        try {
            /*  // This man is reflection from Google instant run sdk, he will tell me where the dex files go.  */
            val pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths")
            val getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String::class.java)
            val instantRunDexPath = getDexFileDirectory.invoke(null, appInfo?.packageName) as String

            val instantRunDexFile = File(instantRunDexPath)
            if (instantRunDexFile.exists() && instantRunDexFile.isDirectory) {
                instantRunDexFile.listFiles()?.forEach { dexFile ->
                    if (dexFile != null && dexFile.exists() && dexFile.isFile && dexFile.name.endsWith(".dex")) {
                        instantRunDexFiles.add(dexFile.absolutePath)
                    }
                }
                RXLog.i("Found InstantRun support")
            }
        } catch (ignore: Exception) {
            RXLog.e("InstantRun support error: ${ignore.message}")
        }
    }
    return instantRunDexFiles
}


/**
 * Identifies if the current VM has a native support for multidex, meaning there is no need for
 * additional installation by this library.
 *
 * @return true if the VM handles multidex
 */
private fun isMultiDexSupported(): Boolean {
    var supported = false
    var vmName = ""
    try {
        if (isYunOS()) {
            vmName = "'YunOS'"
            supported = (System.getProperty("ro.build.version.sdk")?.toInt() ?: 0) > 21
        } else {
            vmName = "'Android'"
            val versionString = System.getProperty("java.vm.version")
            if (!versionString.isNullOrEmpty()) {
                val matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString)
                if (matcher.matches()) {
                    val major = matcher.group(0)?.toInt() ?: 0
                    val minor = matcher.group(1)?.toInt() ?: 0
                    supported = major > VM_WITH_MULTIDEX_VERSION_MAJOR
                            || major >= VM_WITH_MULTIDEX_VERSION_MAJOR && minor >= VM_WITH_MULTIDEX_VERSION_MINOR
                }
            }
        }
    } catch (ignore: Exception) {
        RXLog.e(ignore.message)
    }
    RXLog.i("VM with name $vmName ${if (supported) "has multidex support" else "does not have multidex support"}")
    return supported
}

/**
 * 判断系统是否为YunOS系统
 */
private fun isYunOS(): Boolean {
    return try {
        val version = System.getProperty("ro.yunos.version")
        val vmName = System.getProperty("java.vm.name")
        vmName?.lowercase()?.contains("lemur") == true || (version?.trim()?.length ?: 0) > 0
    } catch (ignore: Exception) {
        false
    }
}