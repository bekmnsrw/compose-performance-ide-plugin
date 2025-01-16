package ru.bekmnsrw.compose.performance.analyzer.search.list

import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableNode

/**
 * @author bekmnsrw
 */
internal sealed interface ListType {

    val parent: ComposableNode
    val containsZeroPxSizeItem: Boolean
    val isObjectGeneratesForEachItem: Boolean

    enum class Type {

        COLUMN, ROW, LAZY_COLUMN, LAZY_ROW
    }

    sealed interface Lazy : ListType {

        val hasKey: Boolean
        val hasContentType: Boolean

        data class LazyColumn(
            override val parent: ComposableNode,
            override val hasKey: Boolean,
            override val hasContentType: Boolean,
            override val containsZeroPxSizeItem: Boolean,
            override val isObjectGeneratesForEachItem: Boolean,
        ) : Lazy {

            override fun toString(): String {
                return """LazyColumn(
                    parent=${parent.name},
                    hasKey=$hasKey,
                    hasContentType=$hasContentType,
                    containsZeroPxSizeItem=$containsZeroPxSizeItem,
                    isObjectGeneratesForEachItem=$isObjectGeneratesForEachItem,
                """.trimMargin()
            }
        }

        data class LazyRow(
            override val parent: ComposableNode,
            override val hasKey: Boolean,
            override val hasContentType: Boolean,
            override val containsZeroPxSizeItem: Boolean,
            override val isObjectGeneratesForEachItem: Boolean,
        ) : Lazy {

            override fun toString(): String {
                return """LazyRow(
                    parent=${parent.name},
                    hasKey=$hasKey,
                    hasContentType=$hasContentType,
                    containsZeroPxSizeItem=$containsZeroPxSizeItem,
                    isObjectGeneratesForEachItem=$isObjectGeneratesForEachItem,
                """.trimMargin()
            }
        }
    }

    sealed interface NotLazy : ListType {

        data class Column(
            override val parent: ComposableNode,
            override val containsZeroPxSizeItem: Boolean,
            override val isObjectGeneratesForEachItem: Boolean,
        ) : NotLazy {

            override fun toString(): String {
                return """Column(
                    parent=${parent.name},
                    containsZeroPxSizeItem=$containsZeroPxSizeItem,
                    isObjectGeneratesForEachItem=$isObjectGeneratesForEachItem,
                """.trimMargin()
            }
        }

        data class Row(
            override val parent: ComposableNode,
            override val containsZeroPxSizeItem: Boolean,
            override val isObjectGeneratesForEachItem: Boolean,
        ) : NotLazy {

            override fun toString(): String {
                return """Row(
                    parent=${parent.name},
                    containsZeroPxSizeItem=$containsZeroPxSizeItem,
                    isObjectGeneratesForEachItem=$isObjectGeneratesForEachItem,
                """.trimMargin()
            }
        }
    }
}