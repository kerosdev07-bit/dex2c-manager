package com.kerosdev.dex2c.manager.domain.model

data class ApkAnalysis(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val minSdk: Int,
    val targetSdk: Int,
    val classes: List<ClassInfo>,
    val dexFileCount: Int
)

data class ClassInfo(
    val className: String,
    val packagePath: String,
    val methods: List<MethodInfo>,
    val isSynthetic: Boolean = false
)

data class MethodInfo(
    val name: String,
    val signature: String,
    val returnType: String,
    val accessFlags: String,
    val isNative: Boolean = false,
    val isSynthetic: Boolean = false,
    val annotations: List<String> = emptyList()
) {
    fun getFullName(): String = "$name$signature"
}

data class FilterRule(
    val pattern: String,
    val isBlacklist: Boolean = false,
    val isExact: Boolean = false
) {
    fun toFilterString(): String {
        val prefix = if (isBlacklist) "!" else if (isExact) "=" else ""
        return prefix + pattern
    }
}
