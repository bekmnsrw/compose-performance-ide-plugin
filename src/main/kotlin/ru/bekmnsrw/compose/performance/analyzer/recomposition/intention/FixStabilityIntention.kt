package ru.bekmnsrw.compose.performance.analyzer.recomposition.intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.idea.search.usagesSearch.constructor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.source.getPsi
import ru.bekmnsrw.compose.performance.analyzer.recomposition.analyzer.StabilityAnalyzer
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableNode
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableParameter
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.Stability.Stable
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.Stability.Unknown
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.Stability.Unstable.*
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.GREATER_THAN
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LEFT_CURLY_BRACE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LEFT_PARENTHESIS
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LESS_THAN
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LIST
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.MAP
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.METHOD_REFERENCE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.PERIOD
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.PERSISTENT_LIST_IMPORT
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.PERSISTENT_MAP_IMPORT
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.PERSISTENT_SET_IMPORT
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.RIGHT_ARROW
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.RIGHT_CURLY_BRACE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.RIGHT_PARENTHESIS
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.SET
import ru.bekmnsrw.compose.performance.analyzer.utils.Utils

/**
 * @author bekmnsrw
 */
internal class FixStabilityIntention(
    private val parameter: ComposableParameter,
    private val node: ComposableNode,
) : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "FAMILY NAME"

    override fun getText(): String = when (parameter.stability) {
        is AnonymousClass -> "Replace lambda with method invocation"
        is UnstableCollection -> "Replace with stable collection"
        is UnstableParam -> "Make parameter stable"
        is StateTransfer -> "StateTransfer"
        is Unknown -> "Unknown"
        is Stable -> ""
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return when (parameter.stability) {
            is AnonymousClass, is StateTransfer, is UnstableParam, is UnstableCollection -> true
            Stable, Unknown -> false
        }
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        when (parameter.stability) {
            is AnonymousClass -> processKtFiles(project) { ktFile -> replaceLambda(ktFile) }
            is UnstableCollection -> processKtFiles(project) { ktFile -> replaceUnstableCollection(ktFile) }
            is UnstableParam -> replaceUnstableParam(project)
            is StateTransfer -> Unit
            Stable, Unknown -> Unit
        }
    }

    private fun processKtFiles(
        project: Project,
        action: (KtFile) -> Unit,
    ) {
        FileTypeIndex.processFiles(
            /* fileType = */ KotlinFileType.INSTANCE,
            /* processor = */ { virtualFile ->
                val psiManager = PsiManager.getInstance(project)
                val ktFile = psiManager.findFile(virtualFile) as? KtFile ?: return@processFiles true
                action(ktFile)
                true
            },
            /* scope = */ GlobalSearchScope.projectScope(project)
        )
    }

    private fun replaceLambda(ktFile: KtFile) {
        val callExpressions = PsiTreeUtil.collectElementsOfType(ktFile, KtCallExpression::class.java)

        callExpressions.forEach { callExpression ->
            val calleeExpression = callExpression.calleeExpression?.text

            if (calleeExpression == node.name) {
                val arguments = callExpression.valueArguments

                arguments.forEach { argument ->
                    val argumentExpression = argument.getArgumentExpression()
                    val passedValue = parameter.passedValue

                    if (passedValue != null && argumentExpression?.text == passedValue) {
                        val formattedArgument = KtPsiFactory(ktFile.project).createExpression(formatLambdaExpression(passedValue))
                        argumentExpression.replace(formattedArgument)
                    }
                }
            }
        }
    }

    private fun formatLambdaExpression(passedValue: String): String {
        val containsRightArrow = if (passedValue.contains(RIGHT_ARROW)) {
            passedValue
                .removeRange(passedValue.indexOf(LEFT_CURLY_BRACE), passedValue.indexOf(RIGHT_ARROW) + 2)
                .trim()
        } else {
            passedValue
        }

        val formatted = containsRightArrow
            .removeRange(containsRightArrow.indexOf(LEFT_PARENTHESIS), containsRightArrow.indexOf(RIGHT_PARENTHESIS) + 1)
            .replace(LEFT_CURLY_BRACE, "")
            .replace(RIGHT_CURLY_BRACE, "")
            .trim()

        return if (formatted.contains(PERIOD)) {
            formatted.replace(PERIOD, METHOD_REFERENCE)
        } else {
            formatted.replaceFirstChar { "$METHOD_REFERENCE$it" }
        }
    }

    private fun replaceUnstableCollection(ktFile: KtFile) {
        val generic = parameter.typeValue.substring(
            startIndex = parameter.typeValue.indexOf(LESS_THAN),
            endIndex = parameter.typeValue.indexOf(GREATER_THAN) + 1,
        )
        val persistentCollection = when {
            parameter.typeValue.contains(LIST) -> PERSISTENT_LIST_IMPORT.split(PERIOD).last()
            parameter.typeValue.contains(MAP) -> PERSISTENT_MAP_IMPORT.split(PERIOD).last()
            parameter.typeValue.contains(SET) -> PERSISTENT_SET_IMPORT.split(PERIOD).last()
            else -> ""
        }
        replaceCollection(ktFile, "$persistentCollection$generic")
    }

    private fun replaceCollection(ktFile: KtFile, type: String) {
        val functions = PsiTreeUtil.collectElementsOfType(ktFile, KtNamedFunction::class.java)

        functions.forEach { function ->
            if (function.name == node.name) {
                val parameter = function.valueParameters.find { valueParameter ->
                    valueParameter.name == parameter.name
                }

                if (parameter != null) {
                    val project = ktFile.project
                    val psiFactory = KtPsiFactory(project)
                    val newTypeReference = psiFactory.createType(type)

                    parameter.typeReference?.replace(newTypeReference)

                    WriteCommandAction.runWriteCommandAction(project) {
                        val ktPsiFactory = KtPsiFactory(project)
                        val import = when {
                            type.contains(LIST) -> PERSISTENT_LIST_IMPORT
                            type.contains(MAP) -> PERSISTENT_MAP_IMPORT
                            type.contains(SET) -> PERSISTENT_SET_IMPORT
                            else -> ""
                        }
                        Utils.addImport(ktPsiFactory, ktFile, import)
                    }
                }
            }
        }
    }

    private fun replaceUnstableParam(project: Project) {
        val psiManager = PsiManager.getInstance(project)
        val virtualFiles = project.baseDir?.let { baseDir -> collectKotlinFiles(baseDir) } ?: return

        virtualFiles.forEach { virtualFile ->
            val ktFile = psiManager.findFile(virtualFile) as? KtFile ?: return@forEach
            val ktClasses = PsiTreeUtil.findChildrenOfType(ktFile, KtClass::class.java)

            ktClasses.forEach { ktClass ->
                if (ktClass.name == parameter.typeName) {
                    ktClass.primaryConstructor?.valueParameters?.forEach {
                        if (it.text.contains("var")) {
                            val psiFactory = KtPsiFactory(project)
                            val newParameter = psiFactory.createParameter(
                                "val ${it.name}: ${it.typeReference?.text} = false,"
                            )
                            it.replace(newParameter)
                        }
                    }

                    ktClass.constructor?.valueParameters?.forEach { valueParameter ->
                        val stability = StabilityAnalyzer.stabilityOf(valueParameter.type)
                        when (stability) {
                            is Stable, is Unknown, is AnonymousClass, is StateTransfer -> Unit
                            is UnstableCollection -> {
                                val type = when {
                                    valueParameter.type.fqName?.asString()?.contains(LIST) == true -> PERSISTENT_LIST_IMPORT.split(PERIOD).last()
                                    valueParameter.type.fqName?.asString()?.contains(MAP) == true -> PERSISTENT_MAP_IMPORT.split(PERIOD).last()
                                    valueParameter.type.fqName?.asString()?.contains(SET) == true -> PERSISTENT_SET_IMPORT.split(PERIOD).last()
                                    else -> ""
                                }

                                val parameter = valueParameter.source.getPsi() as? KtParameter ?: return

                                parameter.typeReference?.text?.let {
                                    val generic = it.substring(
                                        startIndex = it.indexOf(LESS_THAN),
                                        endIndex = it.indexOf(GREATER_THAN) + 1,
                                    )
                                    val newType = KtPsiFactory(project).createType("$type$generic")
                                    parameter.typeReference?.replace(newType)
                                }

                                WriteCommandAction.runWriteCommandAction(project) {
                                    val ktPsiFactory = KtPsiFactory(project)
                                    val import = when {
                                        type.contains(LIST) -> PERSISTENT_LIST_IMPORT
                                        type.contains(MAP) -> PERSISTENT_MAP_IMPORT
                                        type.contains(SET) -> PERSISTENT_SET_IMPORT
                                        else -> ""
                                    }
                                    Utils.addImport(ktPsiFactory, ktFile, import)
                                }
                            }
                            is UnstableParam -> {

                            }
                        }
                    }

                    // TODO: Add ability to check properties
                    ktClass.getProperties().forEach { ktProperty -> }
                }
            }
        }
    }

    private fun collectKotlinFiles(baseDir: VirtualFile): List<VirtualFile> {
        val kotlinFiles = mutableListOf<VirtualFile>()

        baseDir.children.forEach { file ->
            when {
                file.isDirectory -> kotlinFiles.addAll(collectKotlinFiles(file))
                file.extension == "kt" -> kotlinFiles.add(file)
            }
        }

        return kotlinFiles
    }
}
