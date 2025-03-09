package compose.performance.ide.plugin.ast

import org.jetbrains.kotlin.psi.KtParameter

/**
 * @author i.bekmansurov
 */
internal data class ComposableParameter(
    val name: String,
    val typeName: String,
    val typeValue: String,
    val ktParameter: KtParameter,
    val stability: Stability,
    val passedValue: String? = null,
    val isState: Boolean = false,
    val stateUsages: List<String> = emptyList(),
)
