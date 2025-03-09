package compose.performance.ide.plugin.ast

import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * @author i.bekmansurov
 */
internal data class ComposableNode(
    val ktNamedFunction: KtNamedFunction,
    val name: String,
    val parameters: List<ComposableParameter>,
    val nestedNodes: List<ComposableNode>,
    val isRoot: Boolean,
    val text: String,
)
