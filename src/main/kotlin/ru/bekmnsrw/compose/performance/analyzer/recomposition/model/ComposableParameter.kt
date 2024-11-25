package ru.bekmnsrw.compose.performance.analyzer.recomposition.model

/**
 * @author bekmnsrw
 */
data class ComposableParameter(
    val name: String,
    val type: String,
    val passedValue: String? = null,
)
