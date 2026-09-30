package com.kerosdev.dex2c.manager.domain.usecase

import com.kerosdev.dex2c.manager.domain.model.ApkAnalysis
import com.kerosdev.dex2c.manager.domain.repository.ApkRepository
import timber.log.Timber
import javax.inject.Inject

class AnalyzeApkUseCase @Inject constructor(
    private val apkRepository: ApkRepository
) {
    suspend operator fun invoke(apkPath: String): Result<ApkAnalysis> {
        Timber.d("Analyzing APK: $apkPath")
        return try {
            apkRepository.analyzeApk(apkPath)
        } catch (e: Exception) {
            Timber.e(e, "Error in AnalyzeApkUseCase")
            Result.failure(e)
        }
    }
}
