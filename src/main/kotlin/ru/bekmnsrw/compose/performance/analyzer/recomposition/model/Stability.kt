package ru.bekmnsrw.compose.performance.analyzer.recomposition.model

/**
 * @author bekmnsrw
 */
internal sealed interface Stability {

    data object Stable : Stability
    data class Unstable(val reason: String) : Stability
    data object Unknown : Stability
}
