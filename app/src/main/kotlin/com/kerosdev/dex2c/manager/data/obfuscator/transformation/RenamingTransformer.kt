package com.kerosdev.dex2c.manager.data.obfuscator.transformation

import timber.log.Timber
import java.io.File

/**
 * Method/Field Renaming Transformer
 * Renames methods and fields to meaningless names (a, b, c, etc.)
 */
class RenamingTransformer {

    private val nameCounter = mutableMapOf<String, Int>()
    private val mappings = mutableMapOf<String, String>()

    fun transform(smaliFile: File, preservePatterns: List<String> = emptyList()): Result<Unit> = try {
        Timber.d("Applying renaming transformation to: ${smaliFile.name}")
        
        if (!smaliFile.exists()) {
            return Result.failure(Exception("Smali file not found"))
        }

        val content = smaliFile.readText()
        var transformed = content

        // Extract methods and fields
        val methods = extractMethods(content)
        val fields = extractFields(content)

        // Generate new names and replace
        methods.forEach { method ->
            if (!shouldPreserve(method, preservePatterns)) {
                val newName = generateName(method)
                transformed = transformed.replace(method, newName)
                mappings[method] = newName
                Timber.d("Renamed method: $method -> $newName")
            }
        }

        fields.forEach { field ->
            if (!shouldPreserve(field, preservePatterns)) {
                val newName = generateName(field)
                transformed = transformed.replace(field, newName)
                mappings[field] = newName
                Timber.d("Renamed field: $field -> $newName")
            }
        }

        smaliFile.writeText(transformed)
        Timber.d("Renaming completed. Mappings: ${mappings.size}")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error in renaming transformation")
        Result.failure(e)
    }

    private fun extractMethods(content: String): List<String> {
        val methods = mutableListOf<String>()
        val regex = """\.method\s+\w+\s+(\w+)\(""".toRegex()
        regex.findAll(content).forEach { match ->
            methods.add(match.groupValues[1])
        }
        return methods.distinct()
    }

    private fun extractFields(content: String): List<String> {
        val fields = mutableListOf<String>()
        val regex = """\.field\s+\w+\s+(\w+):\w""".toRegex()
        regex.findAll(content).forEach { match ->
            fields.add(match.groupValues[1])
        }
        return fields.distinct()
    }

    private fun generateName(original: String): String {
        val counter = nameCounter.getOrDefault(original, 0)
        nameCounter[original] = counter + 1
        // Generate names: a, b, c, ..., aa, ab, etc.
        return when {
            counter < 26 -> ('a' + counter).toString()
            counter < 52 -> "a" + ('a' + (counter - 26))
            else -> "f_$counter"
        }
    }

    private fun shouldPreserve(name: String, patterns: List<String>): Boolean {
        return patterns.any { pattern ->
            name.matches(Regex(pattern))
        }
    }

    fun getMappings(): Map<String, String> = mappings.toMap()
}
