package ru.bekmnsrw.compose.performance.analyzer.recomposition.model

import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * @author bekmnsrw
 */
internal data class ComposableNode(
    val ktNamedFunction: KtNamedFunction,
    val name: String,
    val parameters: List<ComposableParameter>,
    val nestedNodes: List<ComposableNode>,
    val isRoot: Boolean,
)
