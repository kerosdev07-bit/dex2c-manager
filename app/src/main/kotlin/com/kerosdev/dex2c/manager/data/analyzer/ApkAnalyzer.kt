package com.kerosdev.dex2c.manager.data.analyzer

import android.content.Context
import com.kerosdev.dex2c.manager.domain.model.ApkAnalysis
import com.kerosdev.dex2c.manager.domain.model.ClassInfo
import com.kerosdev.dex2c.manager.domain.model.MethodInfo
import org.androguard.apk.APK
import org.androguard.core.bytecodes.dvm.DalvikVMFormat
import timber.log.Timber
import java.io.File

class ApkAnalyzer(private val context: Context) {

    suspend fun analyzeApk(apkPath: String): Result<ApkAnalysis> = try {
        Timber.d("Analyzing APK: $apkPath")
        
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            return Result.failure(Exception("APK file not found: $apkPath"))
        }

        val apk = APK(apkPath)
        val manifest = apk.get_android_manifest_xml()

        val packageName = apk.get_package()
        val appName = apk.get_app_name() ?: packageName
        val versionName = apk.androidversion["name"] as? String ?: "1.0"
        val versionCode = (apk.androidversion["code"] as? String)?.toIntOrNull() ?: 1
        val minSdk = getMinSdk(manifest)
        val targetSdk = getTargetSdk(manifest)

        val classes = extractClasses(apk)
        val dexFileCount = apk.get_all_dex().size

        val analysis = ApkAnalysis(
            packageName = packageName,
            appName = appName,
            versionName = versionName,
            versionCode = versionCode,
            minSdk = minSdk,
            targetSdk = targetSdk,
            classes = classes,
            dexFileCount = dexFileCount
        )

        Result.success(analysis)
    } catch (e: Exception) {
        Timber.e(e, "Error analyzing APK")
        Result.failure(e)
    }

    private fun extractClasses(apk: APK): List<ClassInfo> {
        val classes = mutableListOf<ClassInfo>()
        
        try {
            for (dex in apk.get_all_dex()) {
                val dvm = DalvikVMFormat(dex)
                
                for (cls in dvm.get_classes()) {
                    val className = cls.get_name()
                    val packagePath = className.substring(1, className.length - 1) // Remove L and ;
                    
                    val methods = cls.get_methods().map { method ->
                        val name = method.get_name()
                        val proto = method.get_proto()
                        val signature = "(${proto.get_parameters_type().joinToString("")})${proto.get_return_type()}"
                        
                        MethodInfo(
                            name = name,
                            signature = signature,
                            returnType = proto.get_return_type(),
                            accessFlags = method.get_access_flags_string(),
                            isNative = "native" in method.get_access_flags_string(),
                            isSynthetic = "synthetic" in method.get_access_flags_string()
                        )
                    }
                    
                    classes.add(
                        ClassInfo(
                            className = className,
                            packagePath = packagePath,
                            methods = methods,
                            isSynthetic = "synthetic" in cls.get_access_flags_string()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting classes")
        }
        
        return classes
    }

    private fun getMinSdk(manifest: Any?): Int {
        // Parse from manifest
        return 26 // Default
    }

    private fun getTargetSdk(manifest: Any?): Int {
        // Parse from manifest
        return 34 // Default
    }
}
