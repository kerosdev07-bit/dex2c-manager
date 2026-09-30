package com.kerosdev.dex2c.manager.data.dex

import android.content.Context
import com.kerosdev.dex2c.manager.domain.model.DexClass
import com.kerosdev.dex2c.manager.domain.model.DexField
import com.kerosdev.dex2c.manager.domain.model.DexFile
import com.kerosdev.dex2c.manager.domain.model.DexMethod
import org.androguard.apk.APK
import org.androguard.core.bytecodes.dvm.ClassDefFormat
import org.androguard.core.bytecodes.dvm.DalvikVMFormat
import timber.log.Timber
import java.io.File
import java.util.*

class DexExtractor(private val context: Context) {

    suspend fun extractFromApk(apkPath: String): Result<List<DexFile>> = try {
        Timber.d("Extracting DEX files from APK: $apkPath")
        
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            return Result.failure(Exception("APK file not found: $apkPath"))
        }

        val apk = APK(apkPath)
        val allDex = apk.get_all_dex()
        
        if (allDex.isEmpty()) {
            return Result.failure(Exception("No DEX files found in APK"))
        }

        val dexFiles = mutableListOf<DexFile>()
        
        allDex.forEachIndexed { index, dexData ->
            try {
                val dvm = DalvikVMFormat(dexData)
                val classes = extractClasses(dvm)
                
                dexFiles.add(
                    DexFile(
                        dexId = index,
                        classes = classes,
                        totalMethods = classes.sumOf { it.methods.size },
                        totalClasses = classes.size
                    )
                )
                Timber.d("DEX $index: ${classes.size} classes, ${classes.sumOf { it.methods.size }} methods")
            } catch (e: Exception) {
                Timber.e(e, "Error processing DEX file $index")
            }
        }

        Result.success(dexFiles)
    } catch (e: Exception) {
        Timber.e(e, "Error extracting DEX from APK")
        Result.failure(e)
    }

    private fun extractClasses(dvm: DalvikVMFormat): List<DexClass> {
        val classes = mutableListOf<DexClass>()
        
        try {
            for (cls in dvm.get_classes()) {
                val className = cls.get_name()  // Lcom/example/MainActivity;
                val packageName = extractPackageName(className)
                val simpleName = extractSimpleName(className)
                
                val methods = cls.get_methods().mapNotNull { method ->
                    try {
                        DexMethod(
                            name = method.get_name(),
                            descriptor = method.get_descriptor(),
                            accessFlags = method.get_access_flags_string(),
                            isNative = "native" in method.get_access_flags_string(),
                            isSynthetic = "synthetic" in method.get_access_flags_string(),
                            isStatic = "static" in method.get_access_flags_string()
                        )
                    } catch (e: Exception) {
                        Timber.w(e, "Error parsing method in $className")
                        null
                    }
                }
                
                val fields = cls.get_fields().mapNotNull { field ->
                    try {
                        DexField(
                            name = field.get_name(),
                            type = field.get_descriptor(),
                            accessFlags = field.get_access_flags_string()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                classes.add(
                    DexClass(
                        className = className,
                        packageName = packageName,
                        simpleName = simpleName,
                        methods = methods,
                        fields = fields,
                        isInterface = "interface" in cls.get_access_flags_string(),
                        isSynthetic = "synthetic" in cls.get_access_flags_string()
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting classes from DEX")
        }
        
        return classes
    }

    private fun extractPackageName(className: String): String {
        // Lcom/example/MainActivity; -> com.example
        if (!className.startsWith("L")) return ""
        val path = className.substring(1, className.length - 1) // Remove L and ;
        return path.substring(0, path.lastIndexOf("/")).replace("/", ".")
    }

    private fun extractSimpleName(className: String): String {
        // Lcom/example/MainActivity; -> MainActivity
        val simpleName = className.substringAfterLast("/")
        return simpleName.removeSuffix(";")
    }
}
