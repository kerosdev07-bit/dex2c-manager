package com.kerosdev.dex2c.manager.domain.usecase

import com.kerosdev.dex2c.manager.data.dex.FilterRuleBuilder
import com.kerosdev.dex2c.manager.domain.model.DexClass
import timber.log.Timber
import javax.inject.Inject

class GenerateFilterRulesUseCase @Inject constructor(
    private val filterRuleBuilder: FilterRuleBuilder
) {
    operator fun invoke(selectedClasses: List<DexClass>): Result<String> {
        return try {
            Timber.d("Generating filter rules for ${selectedClasses.size} classes")
            val rules = filterRuleBuilder.buildFilterRules(selectedClasses)
            
            val validation = filterRuleBuilder.validateRules(rules)
            if (!validation.isValid) {
                return Result.failure(Exception(validation.message))
            }
            
            Timber.d("Filter rules generated: ${validation.message}")
            Result.success(rules)
        } catch (e: Exception) {
            Timber.e(e, "Error in GenerateFilterRulesUseCase")
            Result.failure(e)
        }
    }
}
