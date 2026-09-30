package com.kerosdev.dex2c.manager.domain.usecase

import com.kerosdev.dex2c.manager.domain.model.BuildProgress
import com.kerosdev.dex2c.manager.domain.model.ProtectionConfig
import com.kerosdev.dex2c.manager.domain.repository.ApkRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class ProtectApkUseCase @Inject constructor(
    private val apkRepository: ApkRepository
) {
    operator fun invoke(config: ProtectionConfig): Flow<BuildProgress> {
        Timber.d("Protecting APK: ${config.inputApkPath}")
        return apkRepository.protectApk(config)
    }
}
