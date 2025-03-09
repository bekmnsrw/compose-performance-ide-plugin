package compose.performance.ide.plugin.recomposition.ast.state

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.jetbrains.kotlin.psi.KtFile
import compose.performance.ide.plugin.ast.AbstractSyntaxTreeBuilder
import compose.performance.ide.plugin.ast.state.ScreenStateTransferAnalyzer

/**
 * @author i.bekmansurov
 */
internal class ScreenStateTransferAnalyzerTest : LightJavaCodeInsightFixtureTestCase() {

    fun testCheckScreenStateTransfer() {
        val psiFile = myFixture.configureByText(
            /* p0 = */ MOCKED_FILE_NAME,
            /* p1 = */ MOCKED_FILE_TEXT.trimIndent(),
        ) as KtFile

        val composableFunctions = AbstractSyntaxTreeBuilder.buildAST(psiFile)
        val functionsWithState = ScreenStateTransferAnalyzer.checkScreenStateTransfer(composableFunctions)
        composableFunctions.forEach { println(it.toString()) }
        functionsWithState.forEach { println(it) }
    }

    private companion object {

        const val MOCKED_FILE_NAME = "BuildAST.kt"

        const val MOCKED_FILE_TEXT = """
            data class ScreenState(
                val a: String = "",
                val b: Int = 0,
                val c: List<String> = emptyList(),
            )
                
            @Composable
            fun C1() {
                val state = ScreenState()
                state.a
                state.b
                state.c
                
                C2(state = state)
            }
                
            @Composable
            fun C2(state: ScreenState) {
                C3(state)
            }
                
            @Composable
            fun C3(state: ScreenState) {
                C4(state)
            }
                
            @Composable
            fun C4(state: ScreenState) {
                C5(state.c)
                C6(state.a)
            }
                
            @Composable
            fun C5(list: List<String>? = null) {}
            
            @Composable
            fun C6(a: String) {}
        """
    }
}
