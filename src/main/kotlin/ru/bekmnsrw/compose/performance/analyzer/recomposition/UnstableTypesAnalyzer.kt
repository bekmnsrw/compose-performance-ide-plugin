package ru.bekmnsrw.compose.performance.analyzer.recomposition

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile

/**
 * @author bekmnsrw
 */
internal class UnstableTypesAnalyzer : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val composables = AbstractSyntaxTreeBuilder.buildAST(element as KtFile)
        val composablesWithStability = StabilityAnalyzer.checkParamsStability(composables)
        composablesWithStability.forEach { composable ->
            println(composable.name)
            composable.parameters.forEach { param -> println("${param.ktParameter.name}: ${param.stability}") }
            println()
        }
    }
}