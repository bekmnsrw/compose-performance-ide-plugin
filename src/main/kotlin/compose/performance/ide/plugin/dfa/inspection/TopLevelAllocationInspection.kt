package compose.performance.ide.plugin.dfa.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import compose.performance.ide.plugin.dfa.fix.AddLazyQuickFix
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtVisitorVoid

/**
 * @author i.bekmansurov
 */
internal class TopLevelAllocationInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : KtVisitorVoid() {
            override fun visitProperty(property: KtProperty) {
                super.visitProperty(property)

                if (property.isTopLevel && !property.isLazy()) {
                    holder.registerProblem(
                        property,
                        "Верхнеуровневый объект без отложенной инициализации",
                        ProblemHighlightType.WARNING,
                        AddLazyQuickFix()
                    )
                }
            }
        }
    }

    private fun KtProperty.isLazy(): Boolean {
        return delegateExpression?.text?.contains("lazy") == true
    }
}