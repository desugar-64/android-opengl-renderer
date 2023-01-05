package com.desugar.glucose.util

private const val VERTEX_TAG = "vertex"
private const val FRAGMENT_TAG = "fragment"

internal fun parseVertexFragmentShaders(unifiedSource: String): Pair<String, String> {
    val vertex = StringBuilder()
    val fragment = StringBuilder()

    unifiedSource.split("#type ").filterNot { it.isEmpty() }.forEach { sourceCodeType ->
        when {
            sourceCodeType.startsWith(VERTEX_TAG) -> vertex.append(
                sourceCodeType.drop(VERTEX_TAG.length).dropWhile { it == '\n' || it == '\r' }
            )
            sourceCodeType.startsWith(FRAGMENT_TAG) -> fragment.append(
                sourceCodeType.drop(FRAGMENT_TAG.length).dropWhile { it == '\n' || it == '\r' }
            )
        }
    }

    return vertex.toString() to fragment.toString()
}