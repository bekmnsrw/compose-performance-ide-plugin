package ru.bekmnsrw.compose.performance.analyzer.recomposition

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableNode
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableParameter

/**
 * @author bekmnsrw
 */
object AbstractSyntaxTreeBuilder {

    private const val COMPOSABLE_ANNOTATION_SHORT_NAME = "Composable"

    fun buildAST(file: KtFile): List<ComposableNode> {
        val composableFunctions = mutableListOf<ComposableNode>()

        PsiTreeUtil.findChildrenOfType(
            /* element = */ file,
            /* aClass = */ KtNamedFunction::class.java,
        ).forEach { function ->
            if (function.isComposable()) {
                composableFunctions.add(traverseComposable(function))
            }
        }

        return composableFunctions
    }

    private fun traverseComposable(function: KtNamedFunction): ComposableNode {
        val name = function.name ?: ""
        val parameters = function.valueParameters.map { valueParameter ->
            ComposableParameter(
                name = valueParameter.nameAsName?.asString() ?: "",
                type = valueParameter.typeReference?.text ?: "",
            )
        }

        val nestedComposables = mutableListOf<ComposableNode>()

        PsiTreeUtil.findChildrenOfType(
            /* element = */ function.bodyBlockExpression,
            /* aClass = */ KtCallExpression::class.java,
        )
            .forEach { callExpression ->
                val callee = callExpression.calleeExpression

                if (callee is KtNameReferenceExpression) {
                    val resolvedFunction = callee.resolve() as? KtNamedFunction

                    if (resolvedFunction?.isComposable() == true) {
                        val argumentMap = matchArgumentsWithParameters(callExpression, resolvedFunction)
                        val nestedNode = traverseComposable(resolvedFunction).copy(
                            parameters = resolvedFunction.valueParameters.map { param ->
                                ComposableParameter(
                                    name = param.nameAsName?.asString() ?: "",
                                    type = param.typeReference?.text ?: "",
                                    passedValue = argumentMap[param.nameAsName?.asString()]
                                )
                            }
                        )
                        nestedComposables.add(nestedNode)
                    }
                }
            }

        return ComposableNode(
            name = name,
            parameters = parameters,
            nestedNodes = nestedComposables,
        )
    }

    private fun matchArgumentsWithParameters(
        callExpression: KtCallExpression,
        resolvedFunction: KtNamedFunction,
    ): Map<String, String?> {
        val argumentMap = mutableMapOf<String, String?>()

        val arguments = callExpression.valueArguments

        resolvedFunction.valueParameters.forEachIndexed { index, parameter ->
            val argument = arguments.getOrNull(index)
            val passedValue = argument?.getArgumentExpression()?.text
            argumentMap[parameter.nameAsName?.asString() ?: ""] = passedValue
        }

        return argumentMap
    }

    private fun KtNamedFunction.isComposable(): Boolean {
        return annotationEntries.any { annotation ->
            annotation.shortName?.asString() == COMPOSABLE_ANNOTATION_SHORT_NAME
        }
    }
}
