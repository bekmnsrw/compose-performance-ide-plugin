package ru.bekmnsrw.compose.performance.analyzer.recomposition.model

import org.jetbrains.kotlin.psi.KtParameter

/**
 * @author bekmnsrw
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
