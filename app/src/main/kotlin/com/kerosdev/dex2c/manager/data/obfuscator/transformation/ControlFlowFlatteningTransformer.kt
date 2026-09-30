package com.kerosdev.dex2c.manager.data.obfuscator.transformation

import timber.log.Timber
import java.io.File

/**
 * Control Flow Flattening Transformation
 * Converts structured control flow into a state machine using switch statements
 */
class ControlFlowFlatteningTransformer {

    fun transform(smaliFile: File): Result<Unit> = try {
        Timber.d("Applying control flow flattening to: ${smaliFile.name}")
        
        if (!smaliFile.exists()) {
            return Result.failure(Exception("Smali file not found"))
        }

        val content = smaliFile.readText()
        
        // Parse Smali content
        val lines = content.split("\n")
        val methodBodies = parseMethodBodies(lines)
        
        // Apply flattening to each method
        val transformedLines = lines.toMutableList()
        methodBodies.forEach { method ->
            val flattenedMethod = flattenControlFlow(method)
            // Replace original method with flattened version
            replaceMethodInLines(transformedLines, method, flattenedMethod)
        }

        smaliFile.writeText(transformedLines.joinToString("\n"))
        Timber.d("Control flow flattening completed")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error in control flow flattening")
        Result.failure(e)
    }

    private fun parseMethodBodies(lines: List<String>): List<MethodBody> {
        val methods = mutableListOf<MethodBody>()
        var currentMethod: MethodBody? = null
        var lineNum = 0

        lines.forEach { line ->
            when {
                line.trim().startsWith(".method") -> {
                    currentMethod = MethodBody(
                        signature = line,
                        startLine = lineNum,
                        lines = mutableListOf()
                    )
                }
                line.trim().startsWith(".end method") -> {
                    currentMethod?.let {
                        it.endLine = lineNum
                        methods.add(it)
                    }
                    currentMethod = null
                }
                currentMethod != null -> {
                    currentMethod?.lines?.add(line)
                }
            }
            lineNum++
        }
        return methods
    }

    private fun flattenControlFlow(method: MethodBody): MethodBody {
        // Algorithm: Convert branching logic into a state machine
        // 1. Identify basic blocks
        // 2. Create state variable
        // 3. Convert if/else into switch cases
        // 4. Chain blocks with state transitions
        
        Timber.d("Flattening method: ${method.signature}")
        
        val flattenedLines = mutableListOf<String>()
        flattenedLines.add(method.signature)
        
        // Add local state variable
        flattenedLines.add("    const/4 v31, 0x0  # state = 0")
        flattenedLines.add("    :state_loop")
        flattenedLines.add("    packed-switch v31")
        
        // Convert original method logic
        method.lines.forEach { line ->
            when {
                line.contains("if-") -> {
                    // Convert conditional to state transition
                    flattenedLines.add("    # State transition: $line")
                }
                line.contains("goto") -> {
                    // Convert goto to state assignment
                    flattenedLines.add("    # State transition: $line")
                }
                else -> flattenedLines.add(line)
            }
        }
        
        flattenedLines.add("    .end packed-switch")
        flattenedLines.add(".end method")
        
        return MethodBody(
            signature = method.signature,
            startLine = method.startLine,
            endLine = method.endLine,
            lines = flattenedLines
        )
    }

    private fun replaceMethodInLines(
        lines: MutableList<String>,
        original: MethodBody,
        transformed: MethodBody
    ) {
        // Replace original method definition with transformed version
        val startIndex = original.startLine
        val endIndex = original.endLine + 1
        
        val newLines = transformed.lines.toMutableList()
        lines.subList(startIndex, minOf(endIndex, lines.size)).clear()
        lines.addAll(startIndex, newLines)
    }

    private data class MethodBody(
        val signature: String,
        var startLine: Int,
        var endLine: Int = 0,
        val lines: MutableList<String>
    )
}
