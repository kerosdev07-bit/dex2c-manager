package com.kerosdev.dex2c.manager.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DexClass(
    val className: String,           // L com/example/MainActivity;
    val packageName: String,         // com.example
    val simpleName: String,          // MainActivity
    val methods: List<DexMethod>,
    val fields: List<DexField>,
    val isInterface: Boolean = false,
    val isSynthetic: Boolean = false,
    var isSelected: Boolean = false  // User selected for protection
)

@Serializable
data class DexMethod(
    val name: String,
    val descriptor: String,          // (Ljava/lang/String;)V
    val accessFlags: String,
    val isNative: Boolean = false,
    val isSynthetic: Boolean = false,
    val isStatic: Boolean = false,
    var isSelected: Boolean = false
)

@Serializable
data class DexField(
    val name: String,
    val type: String,
    val accessFlags: String
)

data class DexFile(
    val dexId: Int,                  // 0 for classes.dex, 1 for classes2.dex, etc
    val classes: List<DexClass>,
    val totalMethods: Int,
    val totalClasses: Int
)
