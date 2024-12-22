package ru.bekmnsrw.compose.performance.analyzer.recomposition.model

/**
 * @author bekmnsrw
 */
internal sealed interface Stability {

    object Stable : Stability

    sealed interface Unstable : Stability {

        val reason: String

        class UnstableParam(override val reason: String) : Unstable
        class AnonymousClass(override val reason: String) : Unstable
        class StateTransfer(override val reason: String) : Unstable
        class UnstableCollection(override val reason: String) : Unstable
    }

    object Unknown : Stability
}
