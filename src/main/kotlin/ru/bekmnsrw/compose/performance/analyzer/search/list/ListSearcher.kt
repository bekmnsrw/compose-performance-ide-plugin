package ru.bekmnsrw.compose.performance.analyzer.search.list

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableNode
import ru.bekmnsrw.compose.performance.analyzer.search.kmpSearch
import ru.bekmnsrw.compose.performance.analyzer.search.list.ListType.Lazy.LazyColumn
import ru.bekmnsrw.compose.performance.analyzer.search.list.ListType.Lazy.LazyRow
import ru.bekmnsrw.compose.performance.analyzer.search.list.ListType.NotLazy.Column
import ru.bekmnsrw.compose.performance.analyzer.search.list.ListType.NotLazy.Row
import ru.bekmnsrw.compose.performance.analyzer.search.list.ListType.Type
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.COLUMN
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LAZY_COLUMN
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LAZY_ROW
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LEFT_PARENTHESIS
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.ROW

/**
 * @author bekmnsrw
 */
internal class ListSearcher(private val ktFile: KtFile) {

    private val allFunctions: List<KtNamedFunction> by lazy {
        PsiTreeUtil.collectElementsOfType(ktFile, KtNamedFunction::class.java).toList()
    }

    fun search(composables: List<ComposableNode>) {
        val columns = mutableListOf<ComposableNode>()
        val rows = mutableListOf<ComposableNode>()
        val lazyColumns = mutableListOf<ComposableNode>()
        val lazyRows = mutableListOf<ComposableNode>()

        composables.forEach { composable ->
            composable.nestedNodes.forEach { nested ->
                if (kmpSearch(nested.text, " $COLUMN$LEFT_PARENTHESIS").isNotEmpty()) {
                    if (nested.text.isScrollableColumn()) {
                        columns.add(nested)
                    }
                }
                if (kmpSearch(nested.text, " $ROW$LEFT_PARENTHESIS").isNotEmpty()) {
                    if (nested.text.isScrollableRow()) {
                        rows.add(nested)
                    }
                }
                if (kmpSearch(nested.text, " $LAZY_COLUMN$LEFT_PARENTHESIS").isNotEmpty()) {
                    lazyColumns.add(nested)
                }
                if (kmpSearch(nested.text, " $LAZY_ROW$LEFT_PARENTHESIS").isNotEmpty()) {
                    lazyRows.add(nested)
                }
            }
        }

        processLists(columns, Type.COLUMN)
        processLists(rows, Type.ROW)
        processLists(lazyColumns, Type.LAZY_COLUMN)
        processLists(lazyRows, Type.LAZY_ROW)
    }

    private fun getList(parent: ComposableNode, type: Type): List<KtCallExpression> {
        val functionName = when (type) {
            Type.COLUMN -> COLUMN
            Type.ROW -> ROW
            Type.LAZY_COLUMN -> LAZY_COLUMN
            Type.LAZY_ROW -> LAZY_ROW
        }

        return ktFile.collectDescendantsOfType<KtFunction>()
            .first { it.name == parent.name }
            .bodyExpression
            ?.collectDescendantsOfType<KtCallExpression>()
            ?.filter { it.text.contains(functionName + LEFT_PARENTHESIS) }
            ?: emptyList()
    }

    private fun processLists(composables: List<ComposableNode>, type: Type) {
        composables.forEach { composable ->
            getList(composable, type).forEach { list ->
                when (type) {
                    Type.COLUMN -> println(analyzeColumn(composable, list)) // проверка на vertical scroll + нет items
                    Type.ROW -> println(analyzeRow(composable, list)) // проверка на horizontal scroll + нет items, отдельная проверка на repeat или forEach
                    Type.LAZY_COLUMN -> println(analyzeLazyColumn(composable, list))
                    Type.LAZY_ROW -> println(analyzeLazyRow(composable, list))
                }
            }
        }
    }

    private fun analyzeLazyColumn(parent: ComposableNode, lazyColumn: KtCallExpression): LazyColumn {
        return LazyColumn(
            parent = parent,
            hasKey = lazyColumn.hasKey(),
            hasContentType = lazyColumn.hasContentType(),
            containsZeroPxSizeItem = lazyColumn.containsZeroPxSizeItem("items("),
            isObjectGeneratesForEachItem = lazyColumn.isObjectGeneratesForEachItem("items("),
        )
    }

    private fun analyzeLazyRow(parent: ComposableNode, lazyRow: KtCallExpression): LazyRow {
        return LazyRow(
            parent = parent,
            hasKey = lazyRow.hasKey(),
            hasContentType = lazyRow.hasContentType(),
            containsZeroPxSizeItem = lazyRow.containsZeroPxSizeItem("items("),
            isObjectGeneratesForEachItem = lazyRow.isObjectGeneratesForEachItem("items("),
        )
    }

    private fun analyzeColumn(parent: ComposableNode, column: KtCallExpression): Column {
        return Column(
            parent = parent,
            containsZeroPxSizeItem = column.containsZeroPxSizeItem("repeat(") ||
                    column.containsZeroPxSizeItem("forEach"),
            isObjectGeneratesForEachItem = column.isObjectGeneratesForEachItem("repeat(") ||
                    column.isObjectGeneratesForEachItem("forEach"),
        )
    }

    private fun analyzeRow(parent: ComposableNode, row: KtCallExpression): Row {
        return Row(
            parent = parent,
            containsZeroPxSizeItem = row.containsZeroPxSizeItem("repeat(") ||
                    row.containsZeroPxSizeItem("forEach"),
            isObjectGeneratesForEachItem = row.isObjectGeneratesForEachItem("repeat(") ||
                    row.isObjectGeneratesForEachItem("forEach"),
        )
    }

    private fun KtCallExpression.getItemsParams(): List<String> {
        val lambdaArgument = this.valueArguments.find { argument ->
            argument is KtLambdaArgument
        } ?: return emptyList()

        val content = (lambdaArgument as KtLambdaArgument).text
        val startIndex = content.indexOf("items") + "items".length + 1

        return content
            .substring(startIndex)
            .substringBefore(")")
            .split(",")
            .map { it.trim() }
    }

    private fun KtCallExpression.hasKey(): Boolean {
        val itemsParams = this.getItemsParams()
        return when {
            itemsParams.any { param -> param.contains("key") } -> true
            itemsParams.size == 2 && !itemsParams[1].contains("contentType") -> true
            itemsParams.size == 3 -> true
            else -> false
        }
    }

    private fun KtCallExpression.hasContentType(): Boolean {
        val itemsParams = this.getItemsParams()
        return when {
            itemsParams.any { param -> param.contains("contentType") } -> true
            itemsParams.size == 2 && itemsParams[1].contains("contentType") -> true
            itemsParams.size == 3 -> true
            else -> false
        }
    }

    private fun KtCallExpression.getParentItemName(): String {
        val parentContent = this.text.trim()
        val endIndex = parentContent.indexOf("->")

        return if (endIndex == -1) {
            "it"
        } else {
            val patentContentSubstring = parentContent.substring(0, endIndex)
            val startIndex = patentContentSubstring.lastIndexOf("{") + 1
            patentContentSubstring.substring(startIndex, patentContentSubstring.length).trim()
        }
    }

    private fun KtCallExpression.containsZeroPxSizeItem(pattern: String): Boolean {
        val lambdaArgument = this.valueArguments.find { argument ->
            argument is KtLambdaArgument
        } as? KtLambdaArgument ?: return false

        lambdaArgument.findFunctionCallHierarchyInLambda(pattern).forEach { (parent, children) ->
            val parentItemName = parent.getParentItemName()

            children.forEach { child ->
                if (child.text.contains(parentItemName)) {
                    val childListItem = child.text.trim()
                    val childListItemName = childListItem.substring(0, childListItem.indexOf("("))
                    val listItemFunction = allFunctions.find { function -> function.name == childListItemName }

                    if (listItemFunction != null) {
                        val content = listItemFunction.bodyExpression?.text.orEmpty()

                        val containsZeroPxSizeItem = with(content) {
                            contains(".size(0.dp)") ||
                                    contains(".height(0.dp)") ||
                                    contains(".width(0.dp)")
                        }

                        if (containsZeroPxSizeItem) return true
                    }
                }
            }
        }

        return false
    }

    private fun KtCallExpression.isObjectGeneratesForEachItem(pattern: String): Boolean {
        var isObjectGeneratesForEachItem = false

        val lambdaArgument = this.valueArguments.find { argument ->
            argument is KtLambdaArgument
        } as? KtLambdaArgument ?: return false

        lambdaArgument.findVariableDeclarationHierarchyInLambda(pattern).forEach { (parent, children) ->
            val parentItemName = parent.getParentItemName()

            children.forEach { child ->
                val propertyName = child.text.trim().split(" ")[1].trim()

                parent.forEachDescendantOfType<KtCallExpression> { callExpression ->
                    val listItemContent = callExpression
                        .collectDescendantsOfType<KtCallExpression> { it != callExpression }
                        .find { innerCall -> innerCall.text.contains(parentItemName) }

                    if (listItemContent != null) {
                        if (listItemContent.text.contains(propertyName)) {
                            isObjectGeneratesForEachItem = true
                        }
                    }
                }
            }
        }

        return isObjectGeneratesForEachItem
    }

    private fun KtLambdaArgument.findFunctionCallHierarchyInLambda(pattern: String): Map<KtCallExpression, List<KtCallExpression>> {
        val lambdaExpression = this.getLambdaExpression() ?: return emptyMap()
        val bodyExpression = lambdaExpression.bodyExpression ?: return emptyMap()

        val callHierarchy = mutableMapOf<KtCallExpression, MutableList<KtCallExpression>>()

        bodyExpression.forEachDescendantOfType<KtCallExpression> { callExpression ->
            if (callExpression.text.contains(pattern)) {
                val innerCalls = callExpression.collectDescendantsOfType<KtCallExpression> { it != callExpression }

                if (innerCalls.isNotEmpty()) {
                    callHierarchy[callExpression] = innerCalls.toMutableList()
                }
            }
        }

        return callHierarchy
    }

    private fun KtLambdaArgument.findVariableDeclarationHierarchyInLambda(pattern: String): Map<KtCallExpression, List<KtProperty>> {
        val lambdaExpression = this.getLambdaExpression() ?: return emptyMap()
        val bodyExpression = lambdaExpression.bodyExpression ?: return emptyMap()

        val variableHierarchy = mutableMapOf<KtCallExpression, MutableList<KtProperty>>()

        bodyExpression.forEachDescendantOfType<KtCallExpression> { callExpression ->
            if (callExpression.text.contains(pattern)) {
                val innerCalls = callExpression.collectDescendantsOfType<KtProperty>()

                if (innerCalls.isNotEmpty()) {
                    variableHierarchy[callExpression] = innerCalls.toMutableList()
                }
            }
        }

        return variableHierarchy
    }

    private fun String.isScrollableColumn(): Boolean {
        return this.contains(".verticalScroll")
    }

    private fun String.isScrollableRow(): Boolean {
        return this.contains(".horizontalScroll")
    }
}