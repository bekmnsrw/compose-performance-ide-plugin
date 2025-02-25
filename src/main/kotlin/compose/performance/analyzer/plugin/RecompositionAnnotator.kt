package compose.performance.analyzer.plugin

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.highlighter.AnnotationHostKind
import org.jetbrains.kotlin.idea.inspections.RemoveAnnotationFix
import org.jetbrains.kotlin.idea.quickfix.KotlinSuppressIntentionAction
import org.jetbrains.kotlin.idea.quickfix.RemoveArgumentFix
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.types.typeUtil.isUnit
import compose.performance.analyzer.ast.lambda.LambdaAnalyzer
import compose.performance.analyzer.ast.stability.StabilityAnalyzer
import compose.performance.analyzer.ast.AbstractSyntaxTreeBuilder
import compose.performance.analyzer.ast.ComposableNode
import compose.performance.analyzer.ast.Stability.Unstable
import compose.performance.analyzer.utils.Constants.EXPLICIT_GROUPS_COMPOSABLE_FQ_NAME
import compose.performance.analyzer.utils.Constants.FUN
import compose.performance.analyzer.utils.Constants.NON_RESTARTABLE_COMPOSABLE_FQ_NAME
import compose.performance.analyzer.utils.Constants.NON_SKIPPABLE_COMPOSABLE
import compose.performance.analyzer.utils.Constants.NON_SKIPPABLE_COMPOSABLE_CAMEL_CASE
import compose.performance.analyzer.utils.Constants.NON_SKIPPABLE_COMPOSABLE_ERROR_MESSAGE
import compose.performance.analyzer.utils.Constants.NON_SKIPPABLE_COMPOSABLE_FQ_NAME
import compose.performance.analyzer.utils.Constants.NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE
import compose.performance.analyzer.utils.Constants.SUPPRESS

/**
 * @author i.bekmansurov
 */
internal class RecompositionAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val ast = AbstractSyntaxTreeBuilder.buildAST(element as KtFile)
        val composablesWithStability = StabilityAnalyzer.checkParamsStability(ast)
        val fullyAnalyzedComposables = LambdaAnalyzer.checkLambdaInvocation(composablesWithStability)

//        val a = ScreenStateTransferAnalyzer.checkScreenStateTransfer(fullyAnalyzedComposables)
//        a.forEach {
//            println(it)
//        }
//        fullyAnalyzedComposables.forEach {
//            it.parameters.forEach { a ->
//                println(a.stateUsages)
//                println("-----")
//            }
//        }
//
//        StateParameterReducer.reduceStateParameters(element as KtFile)

        fullyAnalyzedComposables.forEach { composable ->
            showMessage(holder, composable)

            composable.nestedNodes.forEach { nestedNode ->
                showMessage(holder, nestedNode)
            }
        }

//        ImageSearcher(element).search(ast)
//        ListSearcher(element).search(ast)
    }

    private fun showMessage(
        holder: AnnotationHolder,
        node: ComposableNode,
    ) {
        val function = node.ktNamedFunction

        if (function.hasAnnotation(NON_SKIPPABLE_COMPOSABLE_FQ_NAME) || !function.isRestartable()) {
            return
        }

        val suppressAnnotation = function.annotationEntries.firstOrNull { annotation ->
            val isSuppress = annotation.getQualifiedName()?.contains(SUPPRESS) == true
            isSuppress && annotation.valueArgumentList?.arguments.orEmpty().any { argument ->
                argument.getArgumentExpression()?.text?.contains(NON_SKIPPABLE_COMPOSABLE) == true
            }
        }

        val hasUnstableParams = node.parameters.any { parameter -> parameter.stability is Unstable }
        val isUnstable = suppressAnnotation == null && hasUnstableParams
        val isNowStable = suppressAnnotation != null && !hasUnstableParams

        when {
            isUnstable -> {
                val kind = AnnotationHostKind(FUN, function.name.orEmpty(), newLineNeeded = true)
                val intentionAction = KotlinSuppressIntentionAction(function, NON_SKIPPABLE_COMPOSABLE, kind)

                holder.newAnnotation(HighlightSeverity.ERROR, NON_SKIPPABLE_COMPOSABLE_ERROR_MESSAGE)
                    .range(function.nameIdentifier?.originalElement ?: function.originalElement)
                    .withFix(intentionAction)
                    .create()

                node.parameters
                    .filter { parameter -> parameter.stability is Unstable }
                    .forEach { parameter ->
                        holder.newAnnotation(HighlightSeverity.ERROR, "Parameter '${parameter.name} : ${parameter.typeValue}' is unstable. Reason: ${(parameter.stability as Unstable).reason}")
                            .range(parameter.ktParameter.originalElement)
                            .withFix(FixStabilityIntention(parameter, node))
                            .create()
                    }
            }

            isNowStable -> {
                if (suppressAnnotation?.valueArguments?.size == 1) {
                    holder.newAnnotation(HighlightSeverity.WARNING, NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE)
                        .highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
                        .range(suppressAnnotation.originalElement)
                        .newFix(RemoveAnnotationFix(NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE, suppressAnnotation))
                        .registerFix()
                        .create()
                    return
                }
                suppressAnnotation?.valueArgumentList?.arguments.orEmpty()
                    .filter { it.getArgumentExpression()?.text?.contains(NON_SKIPPABLE_COMPOSABLE) == true }
                    .forEach { annotationValue ->
                        holder.newAnnotation(
                            HighlightSeverity.WARNING,
                            "Remove unused $NON_SKIPPABLE_COMPOSABLE_CAMEL_CASE"
                        )
                            .highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL)
                            .range(annotationValue.getArgumentExpression()!!.originalElement)
                            .newFix(RemoveArgumentFix(annotationValue))
                            .registerFix()
                            .create()
                    }
            }
        }
    }

    private fun KtAnnotationEntry.getQualifiedName(): String? {
        return analyze(BodyResolveMode.PARTIAL).get(BindingContext.ANNOTATION, this)?.fqName?.asString()
    }

    private fun KtNamedFunction.hasAnnotation(fqString: String): Boolean {
        return annotationEntries.any { annotation -> annotation.fqNameMatches(fqString) }
    }

    private fun KtNamedFunction.hasAnnotation(fqName: FqName): Boolean {
        return hasAnnotation(fqName.asString())
    }

    private fun KtAnnotationEntry.fqNameMatches(fqName: String): Boolean {
        val shortName = shortName?.asString() ?: return false
        return fqName.endsWith(shortName) && fqName == getQualifiedName()
    }

    private fun KtNamedFunction.isRestartable(): Boolean {
        return when {
            isLocal -> false
            hasModifier(KtTokens.INLINE_KEYWORD) -> false
            hasAnnotation(NON_RESTARTABLE_COMPOSABLE_FQ_NAME) -> false
            hasAnnotation(EXPLICIT_GROUPS_COMPOSABLE_FQ_NAME) -> false
            resolveToDescriptorIfAny()?.returnType?.isUnit() == false -> false
            else -> true
        }
    }
}
