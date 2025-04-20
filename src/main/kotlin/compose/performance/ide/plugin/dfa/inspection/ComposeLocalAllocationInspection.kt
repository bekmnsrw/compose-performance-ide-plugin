package compose.performance.ide.plugin.dfa.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import compose.performance.ide.plugin.dfa.fix.AddRememberQuickFix
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.debugger.core.breakpoints.isComposable
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtVisitorVoid

/**
 * @author i.bekmansurov
 */
internal class ComposeLocalAllocationInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : KtVisitorVoid() {
            override fun visitProperty(property: KtProperty) {
                super.visitProperty(property)

                val parentFunction = PsiTreeUtil.getParentOfType(property, KtNamedFunction::class.java)

                if (parentFunction?.isComposable() == true) {
                    if (!property.isRemembered()) {
                        holder.registerProblem(
                            property,
                            "Локальный объект в Composable-функции",
                            ProblemHighlightType.WARNING,
                            AddRememberQuickFix()
                        )
                    }
                }
            }
        }
    }

    private fun KtProperty.isRemembered(): Boolean {
        return initializer?.text?.startsWith("remember") == true ||
                initializer?.text?.contains("viewModel", ignoreCase = true) == true ||
                initializer?.text == null
    }
}