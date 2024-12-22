package ru.bekmnsrw.compose.performance.analyzer.recomposition.intention

import org.jetbrains.kotlin.psi.*

/**
 * @author bekmnsrw
 */

object StateParameterReducer {

    fun reduceStateParameters(file: KtFile) {
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)

                val stateParameter = function.valueParameters.firstOrNull { it.isState() }
                if (stateParameter != null) {
                    val usedProperties = function.collectStateUsages()
                    if (usedProperties.isNotEmpty()) {
                        updateFunctionSignature(function, stateParameter, usedProperties)
                        updateFunctionCalls(function, stateParameter, usedProperties)
                    }
                }
            }
        })
    }

    private fun updateFunctionSignature(
        function: KtNamedFunction,
        stateParameter: KtParameter,
        usedProperties: List<String>
    ) {
        val factory = KtPsiFactory(function.project)
        val newParameters = usedProperties.joinToString { property ->
            "${property}: ${stateParameter.typeReference?.text?.removeSuffix("State")}"
        }
        val newParameterList = factory.createParameterList("($newParameters)")

        function.valueParameterList?.replace(newParameterList)
    }

    private fun updateFunctionCalls(
        function: KtNamedFunction,
        stateParameter: KtParameter,
        usedProperties: List<String>
    ) {
        function.containingFile.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                super.visitCallExpression(expression)

                if (expression.calleeExpression?.text == function.name) {
                    val arguments = usedProperties.joinToString { property ->
                        "${stateParameter.name}.$property"
                    }
                    val factory = KtPsiFactory(expression.project)
                    val newArgumentList = factory.createCallArguments("($arguments)")
                    expression.valueArgumentList?.replace(newArgumentList)
                }
            }
        })
    }

    private fun KtParameter.isState(): Boolean {
        return typeReference?.text?.contains("State", ignoreCase = true) == true
    }

    private fun KtNamedFunction.collectStateUsages(): List<String> {
        val stateUsages = mutableListOf<String>()

        bodyExpression?.accept(object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(expression)

                if (expression.receiverExpression.text.contains("state")) {
                    expression.selectorExpression?.text?.let { stateUsages.add(it) }
                }
            }
        })

        return stateUsages.distinct()
    }
}
