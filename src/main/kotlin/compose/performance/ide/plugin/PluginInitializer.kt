package compose.performance.ide.plugin

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClass

/**
 * @author i.bekmansurov
 */
internal class PluginInitializer : Annotator {

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        if (psiElement is KtClass) {
            RecompositionAnnotator().annotate(psiElement, annotationHolder)
        }
    }
}
