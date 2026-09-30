package com.kerosdev.dex2c.manager.data.obfuscator.transformation

import timber.log.Timber
import java.io.File

/**
 * Dead Code Insertion Transformer
 * Injects unreachable code to confuse static analysis
 */
class DeadCodeInsertionTransformer {

    fun transform(smaliFile: File): Result<Unit> = try {
        Timber.d("Injecting dead code into: ${smaliFile.name}")
        
        if (!smaliFile.exists()) {
            return Result.failure(Exception("Smali file not found"))
        }

        val content = smaliFile.readText()
        val lines = content.split("\n").toMutableList()
        
        // Find method bodies and insert dead code
        var i = 0
        while (i < lines.size) {
            if (lines[i].trim().startsWith(".method")) {
                // Find the return statement
                for (j in i until lines.size) {
                    if (lines[j].contains("return")) {
                        // Insert dead code before return
                        val deadCode = generateDeadCode()
                        lines.addAll(j, deadCode)
                        i = j + deadCode.size
                        break
                    }
                }
            }
            i++
        }

        smaliFile.writeText(lines.joinToString("\n"))
        Timber.d("Dead code injection completed")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error in dead code insertion")
        Result.failure(e)
    }

    private fun generateDeadCode(): List<String> {
        return listOf(
            "    # Dead code block - unreachable",
            "    const/4 v30, 0x0",
            "    if-nez v30, :skip_dead_code",
            "    const-string v31, \"dead_code\"",
            "    invoke-static {v31}, Ljava/lang/System;->out(Ljava/io/PrintStream;)V",
            "    :skip_dead_code",
            "    # End dead code block"
        )
    }
}
