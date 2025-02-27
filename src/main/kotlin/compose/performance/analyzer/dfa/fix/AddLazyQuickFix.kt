package compose.performance.analyzer.dfa.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * @author i.bekmansurov
 */
internal class AddLazyQuickFix : LocalQuickFix {
    override fun getName() = "Добавить by lazy"
    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val property = descriptor.psiElement as KtProperty
        val factory = KtPsiFactory(project)

        val initializer = property.initializer?.text ?: return
        val wrappedProperty = "val ${property.name} by lazy { $initializer }"
        val newProperty = factory.createProperty(wrappedProperty)

        property.replace(newProperty)
    }
}