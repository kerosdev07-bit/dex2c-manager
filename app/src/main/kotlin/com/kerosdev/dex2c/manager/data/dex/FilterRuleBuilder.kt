package com.kerosdev.dex2c.manager.data.dex

import com.kerosdev.dex2c.manager.domain.model.DexClass
import timber.log.Timber

class FilterRuleBuilder {

    /**
     * Generate filter.txt rules from selected classes
     * Format:
     * com/example/MainActivity;.*        <- Protect all methods
     * com/example/Security;checkAuth.*   <- Protect specific methods
     */
    fun buildFilterRules(selectedClasses: List<DexClass>): String {
        val rules = mutableListOf<String>()
        
        selectedClasses.forEach { dexClass ->
            // Skip interface and synthetic classes
            if (dexClass.isInterface || dexClass.isSynthetic) {
                return@forEach
            }
            
            // Get selected methods in this class
            val selectedMethods = dexClass.methods.filter { it.isSelected && !it.isNative && !it.isSynthetic }
            
            if (selectedMethods.isEmpty()) {
                // If no methods selected in class, protect entire class
                if (dexClass.isSelected) {
                    rules.add(buildClassRule(dexClass.className))
                }
            } else {
                // Add individual method rules
                selectedMethods.forEach { method ->
                    rules.add(buildMethodRule(dexClass.className, method.name))
                }
            }
        }
        
        if (rules.isEmpty()) {
            Timber.w("No rules generated - no classes/methods selected")
            return ""
        }
        
        Timber.d("Generated ${rules.size} filter rules")
        return rules.joinToString("\n")
    }

    private fun buildClassRule(className: String): String {
        // Lcom/example/MainActivity; -> com/example/MainActivity;.*
        val classPath = className.substring(1, className.length - 1)  // Remove L and ;
        return "$classPath;.*"
    }

    private fun buildMethodRule(className: String, methodName: String): String {
        // Lcom/example/MainActivity; + onCreate -> com/example/MainActivity;onCreate\(.*
        val classPath = className.substring(1, className.length - 1)
        return "$classPath;$methodName\\(.*"
    }

    /**
     * Validate filter rules before sending to dex2c
     */
    fun validateRules(rules: String): ValidationResult {
        if (rules.isBlank()) {
            return ValidationResult(false, "No rules defined")
        }
        
        val lineCount = rules.lines().count { it.isNotBlank() }
        if (lineCount == 0) {
            return ValidationResult(false, "No valid rule lines")
        }
        
        // Check for global rule (dangerous)
        if (rules.contains(".*") && !rules.contains(";")) {
            return ValidationResult(
                false,
                "Global rule (.*) detected. This can break the app. Use package-specific rules instead."
            )
        }
        
        // Validate regex patterns
        rules.lines().forEach { line ->
            if (line.isBlank() || line.startsWith("#")) return@forEach
            
            val rule = line.removePrefix("!").removePrefix("=").trim()
            try {
                Regex(rule)
            } catch (e: Exception) {
                return ValidationResult(false, "Invalid regex in rule: $rule - ${e.message}")
            }
        }
        
        return ValidationResult(
            true,
            "Valid rules: $lineCount rules"
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )
}
