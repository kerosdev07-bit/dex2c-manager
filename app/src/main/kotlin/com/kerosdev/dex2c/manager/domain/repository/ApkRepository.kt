package com.kerosdev.dex2c.manager.domain.repository

import com.kerosdev.dex2c.manager.domain.model.ApkAnalysis
import com.kerosdev.dex2c.manager.domain.model.BuildProgress
import com.kerosdev.dex2c.manager.domain.model.ProtectionConfig
import kotlinx.coroutines.flow.Flow

interface ApkRepository {
    suspend fun analyzeApk(apkPath: String): Result<ApkAnalysis>
    fun protectApk(config: ProtectionConfig): Flow<BuildProgress>
    suspend fun validateApk(apkPath: String): Result<Unit>
}
