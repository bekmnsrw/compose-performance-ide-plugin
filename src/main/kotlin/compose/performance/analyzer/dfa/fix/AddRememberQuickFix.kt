package compose.performance.analyzer.dfa.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import compose.performance.analyzer.dfa.addImport
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * @author i.bekmansurov
 */
internal class AddRememberQuickFix : LocalQuickFix {

    override fun getName() = "Обернуть в remember"

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val property = descriptor.psiElement as KtProperty
        val factory = KtPsiFactory(project)

        val initializer = property.initializer?.text ?: return
        val wrappedInitializer = "remember { $initializer }"
        val newInitializer = factory.createExpression(wrappedInitializer)

        property.initializer?.replace(newInitializer)
        (property.containingFile as? KtFile)?.addImport("androidx.compose.runtime.remember")
    }
}