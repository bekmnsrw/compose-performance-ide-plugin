package compose.performance.ide.plugin.dfa

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

/**
 * @author i.bekmansurov
 */
internal fun KtFile.addImport(fqName: String) {
    val importList = importList
    val existingImports = importList?.imports?.mapNotNull { it.importPath?.toString() } ?: emptyList()

    if (!existingImports.contains(fqName)) {
        val psiFactory = KtPsiFactory(this)
        val importDirective = psiFactory.createImportDirective(ImportPath.fromString(fqName))
        importList?.addAfter(importDirective, importList.lastChild)
    }
}
