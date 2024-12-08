package ru.bekmnsrw.compose.performance.analyzer.recomposition

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClass

/**
 * @author bekmnsrw
 */
internal class PluginInitializer : Annotator {

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        if (psiElement is KtClass) {
            RecompositionAnnotator().annotate(psiElement, annotationHolder)
        }
    }
}
