package compose.performance.analyzer.dfa

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * @author i.bekmansurov
 */
sealed class ObjectCategory {
    object TopLevel : ObjectCategory()
    object Local : ObjectCategory()
}

fun classifyObject(element: PsiElement): ObjectCategory {
    return when (element.parent) {
        is KtClass -> ObjectCategory.TopLevel
        is KtNamedFunction -> ObjectCategory.Local
        else -> ObjectCategory.Local
    }
}