package com.kerosdev.dex2c.manager.domain.usecase

import com.kerosdev.dex2c.manager.data.processor.Dex2cExecutor
import com.kerosdev.dex2c.manager.domain.model.BuildProgress
import com.kerosdev.dex2c.manager.domain.model.ProtectionConfig
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class ExecuteProtectionUseCase @Inject constructor(
    private val dex2cExecutor: Dex2cExecutor
) {
    operator fun invoke(config: ProtectionConfig): Flow<BuildProgress> {
        Timber.d("Starting protection for: ${config.inputApkPath}")
        return dex2cExecutor.executeProtection(config)
    }
}
