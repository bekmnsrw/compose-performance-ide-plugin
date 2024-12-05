package ru.bekmnsrw.compose.performance.analyzer.recomposition.ast

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.jetbrains.kotlin.psi.KtFile

/**
 * @author bekmnsrw
 */
internal class AbstractSyntaxTreeBuilderTest : LightJavaCodeInsightFixtureTestCase() {

    fun testBuildAST() {
        val psiFile = myFixture.configureByText(
            /* p0 = */ MOCKED_FILE_NAME,
            /* p1 = */ MOCKED_FILE_TEXT.trimIndent(),
        ) as KtFile

        AbstractSyntaxTreeBuilder.buildAST(psiFile).forEach { node ->
            println(node.toString())
            println()
        }
    }

    private companion object {

        const val MOCKED_FILE_NAME = "BuildAST.kt"

        const val MOCKED_FILE_TEXT = """
            @Composable
            fun C1(p1: String, p2: Int, p3: List<String>) {
                C2(p1 = {})
                R1()
            }
                
            @Composable
            fun C2(p1: () -> Unit) {
                C3()
            }
                
            @Composable
            fun C3() {
                C4(listOf<Int>(1, 2, 3))
            }
                
            @Composable
            fun C4(p1: List<Int>) {}
                
            fun R1() {}
                
            @Composable
            fun C5() {}
        """
    }
}