package ru.bekmnsrw.compose.performance.analyzer.recomposition.model

/**
 * @author bekmnsrw
 */
internal data class ComposableNode(
    val name: String,
    val parameters: List<ComposableParameter>,
    val nestedNodes: List<ComposableNode>,
)
