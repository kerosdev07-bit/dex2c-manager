package com.kerosdev.dex2c.manager.data.processor

import android.content.Context
import com.kerosdev.dex2c.manager.domain.model.BuildProgress
import com.kerosdev.dex2c.manager.domain.model.BuildStep
import com.kerosdev.dex2c.manager.domain.model.ProtectionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File
import java.io.BufferedReader

class Dex2cExecutor(private val context: Context) {

    private val pythonPath = File(context.getExternalFilesDir(null), "python/bin/python3").absolutePath
    private val dex2cPath = File(context.getExternalFilesDir(null), "dex2c").absolutePath

    fun executeProtection(config: ProtectionConfig): Flow<BuildProgress> = flow {
        try {
            emit(BuildProgress(BuildStep.VALIDATING, 5, "Validating configuration..."))
            validatePaths()
            validateConfig(config)

            emit(BuildProgress(BuildStep.GENERATING_CPP, 20, "Preparing dex2c environment..."))
            
            // Write filter.txt
            val filterFile = File(dex2cPath, "filter.txt")
            filterFile.writeText(config.filterRules.joinToString("\n"))
            Timber.d("Filter file written: ${filterFile.absolutePath}")
            
            // Write dcc.cfg
            val cfgFile = File(dex2cPath, "dcc.cfg")
            writeDccConfig(cfgFile, config)
            Timber.d("Config file written: ${cfgFile.absolutePath}")

            emit(BuildProgress(BuildStep.BUILDING_NATIVE, 30, "Running dex2c compiler..."))
            
            // Build command
            val command = buildDccCommand(config)
            Timber.d("Executing: ${command.joinToString(" ")}")
            
            val process = ProcessBuilder(command)
                .directory(File(dex2cPath))
                .redirectErrorStream(true)
                .start()

            // Parse output and emit progress
            parseProcessOutput(process.inputStream.bufferedReader()).collect { progress ->
                emit(progress)
            }
            
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw Exception("dex2c failed with exit code: $exitCode")
            }

            emit(BuildProgress(BuildStep.REBUILDING_APK, 70, "Rebuilding APK with native libraries..."))
            rebuildApk(config)
            
            emit(BuildProgress(BuildStep.SIGNING_APK, 90, "Signing APK..."))
            signApk(config)
            
            emit(BuildProgress(BuildStep.COMPLETED, 100, "Protection completed successfully!"))
            Timber.d("APK protection completed: ${config.outputApkPath}")
            
        } catch (e: Exception) {
            Timber.e(e, "Error during protection")
            emit(BuildProgress(BuildStep.FAILED, 0, "Protection failed", e.message))
        }
    }

    private fun validatePaths() {
        if (!File(pythonPath).exists()) {
            throw Exception("Python runtime not found. Please install it first.")
        }
        if (!File(dex2cPath).exists()) {
            throw Exception("dex2c not found. Please install it first.")
        }
    }

    private fun validateConfig(config: ProtectionConfig) {
        if (!File(config.inputApkPath).exists()) {
            throw Exception("Input APK not found: ${config.inputApkPath}")
        }
        if (config.filterRules.isEmpty()) {
            throw Exception("No filter rules defined")
        }
    }

    private fun buildDccCommand(config: ProtectionConfig): List<String> {
        return listOf(
            pythonPath,
            "$dex2cPath/dcc.py",
            "-a", config.inputApkPath,
            "-o", config.outputApkPath,
            "--filter", "filter.txt",
            "--custom-loader", config.customLoader,
            if (config.obfuscate) "--obfuscate" else "",
            if (config.dynamicRegister) "--dynamic-register" else "",
            if (config.skipSynthetic) "--skip-synthetic" else "",
            if (config.forceKeepLibs) "--force-keep-libs" else "",
            if (config.disableSigning) "--disable-signing" else ""
        ).filter { it.isNotEmpty() }
    }

    private fun writeDccConfig(file: File, config: ProtectionConfig) {
        // TODO: Parse and update dcc.cfg JSON
        // For now, we assume dcc.cfg already exists in dex2c folder
    }

    private fun parseProcessOutput(reader: BufferedReader): Flow<BuildProgress> = flow {
        try {
            reader.useLines { lines ->
                lines.forEach { line ->
                    when {
                        line.contains("Analyzing") -> emit(BuildProgress(BuildStep.ANALYZING_APK, 25, line))
                        line.contains("Parsing DEX") -> emit(BuildProgress(BuildStep.PARSING_DEX, 35, line))
                        line.contains("Filtering") -> emit(BuildProgress(BuildStep.FILTERING_METHODS, 40, line))
                        line.contains("Generating") -> emit(BuildProgress(BuildStep.GENERATING_CPP, 50, line))
                        line.contains("Building") -> emit(BuildProgress(BuildStep.BUILDING_NATIVE, 60, line))
                        line.contains("Decompiling") -> emit(BuildProgress(BuildStep.DECOMPILING_APK, 70, line))
                        line.contains("Modifying") -> emit(BuildProgress(BuildStep.MODIFYING_SMALI, 75, line))
                        line.contains("Rebuilding") -> emit(BuildProgress(BuildStep.REBUILDING_APK, 80, line))
                        line.contains("Signing") -> emit(BuildProgress(BuildStep.SIGNING_APK, 90, line))
                        line.contains("ERROR") || line.contains("Error") -> emit(BuildProgress(BuildStep.FAILED, 0, line, line))
                        line.isNotEmpty() -> Timber.d(line)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing process output")
        }
    }

    private fun rebuildApk(config: ProtectionConfig) {
        // Apktool automatically handles this during dex2c execution
        // This is a placeholder for additional APK rebuild logic if needed
        Timber.d("APK rebuild handled by dex2c")
    }

    private fun signApk(config: ProtectionConfig) {
        if (config.disableSigning) {
            Timber.d("APK signing disabled")
            return
        }
        // Signing is handled by dex2c's apksigner
        Timber.d("APK signed by dex2c")
    }
}
