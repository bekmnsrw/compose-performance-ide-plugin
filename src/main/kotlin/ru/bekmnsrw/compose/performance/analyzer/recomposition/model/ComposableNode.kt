package ru.bekmnsrw.compose.performance.analyzer.recomposition.model

/**
 * @author bekmnsrw
 */
data class ComposableNode(
    val name: String,
    val parameters: List<ComposableParameter>,
    val nestedNodes: List<ComposableNode>,
)
