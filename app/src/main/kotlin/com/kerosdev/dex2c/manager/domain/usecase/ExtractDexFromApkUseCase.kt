package com.kerosdev.dex2c.manager.domain.usecase

import com.kerosdev.dex2c.manager.data.dex.DexExtractor
import com.kerosdev.dex2c.manager.domain.model.DexFile
import timber.log.Timber
import javax.inject.Inject

class ExtractDexFromApkUseCase @Inject constructor(
    private val dexExtractor: DexExtractor
) {
    suspend operator fun invoke(apkPath: String): Result<List<DexFile>> {
        Timber.d("Extracting DEX from APK: $apkPath")
        return try {
            dexExtractor.extractFromApk(apkPath)
        } catch (e: Exception) {
            Timber.e(e, "Error in ExtractDexFromApkUseCase")
            Result.failure(e)
        }
    }
}
