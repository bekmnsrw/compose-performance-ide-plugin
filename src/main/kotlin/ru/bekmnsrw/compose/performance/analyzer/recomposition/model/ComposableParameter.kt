package ru.bekmnsrw.compose.performance.analyzer.recomposition.model

import org.jetbrains.kotlin.psi.KtParameter

/**
 * @author bekmnsrw
 */
internal data class ComposableParameter(
    val ktParameter: KtParameter,
    val stability: Stability,
    val passedValue: String? = null,
    val isState: Boolean = false,
    val stateUsages: List<String> = emptyList(),
)
