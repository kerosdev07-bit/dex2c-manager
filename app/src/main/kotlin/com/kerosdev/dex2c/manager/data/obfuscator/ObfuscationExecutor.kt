package com.kerosdev.dex2c.manager.data.obfuscator

import android.content.Context
import com.kerosdev.dex2c.manager.data.dex.DexExtractor
import com.kerosdev.dex2c.manager.domain.model.DexClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File

class ObfuscationExecutor(private val context: Context) {

    private val dexExtractor = DexExtractor(context)
    private val blackObfuscator = BlackObfuscator(context)

    fun executeObfuscation(
        inputApkPath: String,
        selectedClasses: List<DexClass>,
        config: ObfuscationConfig,
        outputApkPath: String
    ): Flow<ObfuscationProgress> = flow {
        try {
            emit(ObfuscationProgress(ObfuscationStep.VALIDATING, 5, "Validating APK..."))
            validateInputs(inputApkPath, selectedClasses)

            emit(ObfuscationProgress(ObfuscationStep.EXTRACTING_DEX, 15, "Extracting DEX files..."))
            val dexFiles = extractDexFiles(inputApkPath)
            
            if (dexFiles.isEmpty()) {
                throw Exception("No DEX files found in APK")
            }

            emit(ObfuscationProgress(ObfuscationStep.ANALYZING, 25, "Analyzing classes..."))
            val selectedClassNames = selectedClasses.map { it.className }.distinct()
            Timber.d("Selected ${selectedClassNames.size} classes for obfuscation")

            emit(ObfuscationProgress(ObfuscationStep.OBFUSCATING, 45, "Applying obfuscation transformations..."))
            val obfuscatedDex = obfuscateDexClasses(dexFiles.first(), selectedClassNames, config)

            emit(ObfuscationProgress(ObfuscationStep.REBUILDING_APK, 70, "Rebuilding APK with obfuscated DEX..."))
            rebuildApkWithObfuscatedDex(inputApkPath, obfuscatedDex, outputApkPath)

            emit(ObfuscationProgress(ObfuscationStep.ALIGNING, 85, "Aligning APK..."))
            zipalignApk(outputApkPath)

            emit(ObfuscationProgress(ObfuscationStep.SIGNING, 95, "Signing APK..."))
            signApk(outputApkPath)

            emit(ObfuscationProgress(ObfuscationStep.COMPLETED, 100, "Obfuscation completed!"))
            Timber.d("APK obfuscation completed: $outputApkPath")

        } catch (e: Exception) {
            Timber.e(e, "Error during obfuscation")
            emit(ObfuscationProgress(ObfuscationStep.FAILED, 0, "Error: ${e.message}", e.message))
        }
    }

    private fun validateInputs(apkPath: String, classes: List<DexClass>) {
        if (!File(apkPath).exists()) {
            throw Exception("Input APK not found: $apkPath")
        }
        if (classes.isEmpty()) {
            throw Exception("No classes selected for obfuscation")
        }
    }

    private suspend fun extractDexFiles(apkPath: String): List<File> {
        val result = dexExtractor.extractFromApk(apkPath)
        return if (result.isSuccess) {
            // Return actual DEX file paths
            emptyList() // TODO: Return actual DEX files
        } else {
            throw result.exceptionOrNull() ?: Exception("Failed to extract DEX")
        }
    }

    private fun obfuscateDexClasses(
        dexFile: File,
        classNames: List<String>,
        config: ObfuscationConfig
    ): File {
        val result = blackObfuscator.obfuscateClasses(dexFile, classNames, config)
        return result.getOrNull() ?: throw result.exceptionOrNull() ?: Exception("Obfuscation failed")
    }

    private fun rebuildApkWithObfuscatedDex(inputApk: String, obfuscatedDex: File, outputApk: String) {
        Timber.d("Rebuilding APK with obfuscated DEX")
        // TODO: Use Apktool to rebuild APK
        // 1. Decompile original APK
        // 2. Replace classes.dex with obfuscated version
        // 3. Recompile to APK
    }

    private fun zipalignApk(apkPath: String) {
        Timber.d("Running zipalign on: $apkPath")
        // TODO: Execute zipalign command
        // zipalign -p 4 input.apk output.apk
    }

    private fun signApk(apkPath: String) {
        Timber.d("Signing APK: $apkPath")
        // TODO: Use Android signing API or apksigner
    }
}

data class ObfuscationProgress(
    val step: ObfuscationStep,
    val progress: Int = 0,  // 0-100
    val message: String = "",
    val error: String? = null
)

enum class ObfuscationStep {
    VALIDATING,
    EXTRACTING_DEX,
    ANALYZING,
    OBFUSCATING,
    REBUILDING_APK,
    ALIGNING,
    SIGNING,
    COMPLETED,
    FAILED
}
