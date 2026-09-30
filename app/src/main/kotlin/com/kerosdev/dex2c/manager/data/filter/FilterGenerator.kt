package com.kerosdev.dex2c.manager.data.filter

import com.kerosdev.dex2c.manager.domain.model.FilterRule
import com.kerosdev.dex2c.manager.domain.model.MethodInfo
import timber.log.Timber

class FilterGenerator {

    fun generateFilterRules(selectedMethods: List<MethodInfo>): String {
        return selectedMethods.map { method ->
            generateMethodPattern(method)
        }.joinToString("\n")
    }

    fun generateClassPattern(className: String, includeAllMethods: Boolean = true): String {
        return if (includeAllMethods) {
            "${className.replace('.', '/')};.*"
        } else {
            "${className.replace('.', '/')}"
        }
    }

    fun generatePackagePattern(packagePath: String): String {
        return "${packagePath.replace('.', '/')}/.*;.*"
    }

    fun generateMethodPattern(method: MethodInfo): String {
        return ".*;${method.name}\\(.*"
    }

    fun validateFilterRules(rules: List<FilterRule>): ValidationResult {
        val errors = mutableListOf<String>()
        var hasGlobalRule = false

        for (rule in rules) {
            if (rule.pattern == ".*") {
                hasGlobalRule = true
            }
            
            // Validate regex pattern
            try {
                Regex(rule.pattern)
            } catch (e: Exception) {
                errors.add("Invalid regex pattern: ${rule.pattern}")
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            hasGlobalRule = hasGlobalRule
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>,
        val hasGlobalRule: Boolean
    )
}
