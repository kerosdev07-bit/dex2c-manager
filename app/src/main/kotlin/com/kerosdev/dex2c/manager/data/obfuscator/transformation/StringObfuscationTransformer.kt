package com.kerosdev.dex2c.manager.data.obfuscator.transformation

import timber.log.Timber
import java.io.File
import kotlin.random.Random

/**
 * String Obfuscation Transformer
 * Encrypts string literals and replaces with decryption calls
 */
class StringObfuscationTransformer {

    fun transform(smaliFile: File): Result<Unit> = try {
        Timber.d("Applying string obfuscation to: ${smaliFile.name}")
        
        if (!smaliFile.exists()) {
            return Result.failure(Exception("Smali file not found"))
        }

        val content = smaliFile.readText()
        val lines = content.split("\n").toMutableList()
        
        // Find all string literals
        val stringMap = mutableMapOf<String, String>()
        lines.forEachIndexed { index, line ->
            if (line.contains("const-string")) {
                val stringValue = extractStringLiteral(line)
                if (stringValue != null && stringValue.isNotEmpty()) {
                    val encrypted = encryptString(stringValue)
                    stringMap[stringValue] = encrypted
                    
                    // Replace with encrypted version
                    val obfuscatedLine = replaceStringLiteral(line, encrypted)
                    lines[index] = obfuscatedLine
                }
            }
        }

        smaliFile.writeText(lines.joinToString("\n"))
        Timber.d("String obfuscation completed. Encrypted ${stringMap.size} strings")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error in string obfuscation")
        Result.failure(e)
    }

    private fun extractStringLiteral(line: String): String? {
        // Example: const-string v0, "hello" -> "hello"
        val regex = """const-string\s+\w+,\s+"([^"]*)""".toRegex()
        val match = regex.find(line)
        return match?.groupValues?.getOrNull(1)
    }

    private fun encryptString(value: String): String {
        // Simple XOR encryption (for demonstration)
        val key = Random.nextInt(255)
        val encrypted = value.map { it.code xor key }.joinToString(",")
        return encrypted
    }

    private fun replaceStringLiteral(line: String, encrypted: String): String {
        // Replace original string with encrypted version
        // Actual implementation would add runtime decryption call
        return line.replace(
            Regex("\\"[^\\"]*\\""),
            "\"$encrypted\""
        )
    }
}
