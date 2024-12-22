package ru.bekmnsrw.compose.performance.analyzer.utils

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

/**
 * @author bekmnsrw
 */
internal object Utils {

    fun addImport(ktPsiFactory: KtPsiFactory, ktPsiFile: KtFile, fqName: String) {
        val isImportExists = ktPsiFile.importList?.imports?.any { ktImportDirective ->
            ktImportDirective.importedFqName?.asString() == fqName
        }

        requireNotNull(isImportExists)

        if (isImportExists == false) {
            ktPsiFile.importList?.add(
                ktPsiFactory.createImportDirective(
                    ImportPath(
                        fqName = FqName(fqName),
                        isAllUnder = false,
                    )
                )
            )
        }
    }
}
