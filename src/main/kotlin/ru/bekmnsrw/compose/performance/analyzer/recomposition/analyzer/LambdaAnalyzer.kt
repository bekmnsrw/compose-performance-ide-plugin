package ru.bekmnsrw.compose.performance.analyzer.recomposition.analyzer

import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableNode
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableParameter
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.Stability
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.EMPTY_STRING
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LEFT_CURLY_BRACE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.METHOD_REFERENCE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.RIGHT_CURLY_BRACE

/**
 * @author bekmnsrw
 */
internal object LambdaAnalyzer {

    fun checkLambdaInvocation(composables: List<ComposableNode>): List<ComposableNode> {
        return composables.map { composable ->
            val updatedNestedNodes = composable.nestedNodes.map { nestedComposable ->
                val updatedParameters = nestedComposable.parameters.map { parameter ->
                    if (parameter.isLambda() && parameter.passedValue != null && parameter.checkAnonymousClass()) {
                        parameter.copy(stability = Stability.Unstable.AnonymousClass(ANONYMOUS_CLASS_REASON))
                    } else {
                        parameter
                    }
                }
                nestedComposable.copy(parameters = updatedParameters)
            }
            composable.copy(nestedNodes = updatedNestedNodes)
        }
    }

    private fun ComposableParameter.isLambda(): Boolean {
        return typeName.contains(FUNCTION_TYPE)
    }

    /**
     * Don't do like that:
     *   onClick = { viewModel.onClick(it) }
     *
     * Instead, use Method Reference:
     *   onClick = viewModel::onClick
     *
     * Or wrap lambda with remember:
     *   val onClick: (Int) -> Unit = remember(viewModel) {
     *       { viewModel.onClick(it) }
     *   }
     */
    private fun ComposableParameter.checkAnonymousClass(): Boolean {
        return passedValue != null &&
                passedValue.lambdaIsNotEmpty() &&
                passedValue.contains(LEFT_CURLY_BRACE) &&
                passedValue.contains(RIGHT_CURLY_BRACE) &&
                !passedValue.contains(METHOD_REFERENCE) &&
                !passedValue.contains(REMEMBER)
    }

    private fun String.lambdaIsNotEmpty(): Boolean {
        return this
            .trim()
            .replace(LEFT_CURLY_BRACE, EMPTY_STRING)
            .replace(RIGHT_CURLY_BRACE, EMPTY_STRING)
            .trim()
            .isNotBlank()
    }

    /**
     * Constants
     */
    private const val ANONYMOUS_CLASS_REASON = "Causes creating of anonymous class"
    private const val FUNCTION_TYPE = "Function"
    private const val REMEMBER = "remember"
}
