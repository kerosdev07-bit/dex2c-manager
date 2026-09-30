package com.kerosdev.dex2c.manager.domain.model

data class ProtectionConfig(
    val projectId: String,
    val inputApkPath: String,
    val outputApkPath: String,
    val filterRules: List<FilterRule>,
    val customLoader: String = "amimo.dcc.DccApplication",
    val obfuscate: Boolean = false,
    val dynamicRegister: Boolean = false,
    val skipSynthetic: Boolean = false,
    val forceKeepLibs: Boolean = false,
    val disableSigning: Boolean = false,
    val allowGlobal: Boolean = false,
    val keystorePath: String? = null,
    val keystorePassword: String? = null,
    val keyAlias: String? = null,
    val keyPassword: String? = null
)

data class BuildProgress(
    val step: BuildStep,
    val progress: Int = 0, // 0-100
    val message: String = "",
    val error: String? = null
)

enum class BuildStep {
    IDLE,
    VALIDATING,
    ANALYZING_APK,
    PARSING_DEX,
    FILTERING_METHODS,
    GENERATING_CPP,
    BUILDING_NATIVE,
    DECOMPILING_APK,
    MODIFYING_SMALI,
    REBUILDING_APK,
    SIGNING_APK,
    COMPLETED,
    FAILED
}
