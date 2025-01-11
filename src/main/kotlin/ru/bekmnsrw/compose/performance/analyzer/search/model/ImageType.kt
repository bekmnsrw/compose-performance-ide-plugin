package ru.bekmnsrw.compose.performance.analyzer.search.model

import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableNode

/**
 * @author bekmnsrw
 */
internal sealed interface ImageType {

    val parent: ComposableNode
    val source: Source
    val isCacheEnabled: Boolean?
    val hasPlaceholder: Boolean?
    val extension: Extension
    val resolution: Resolution

    enum class Source {

        LOCAL, REMOTE
    }

    enum class Extension {

        XML, JPG, JPEG, WEBP, PNG, SVG, IMAGE_VECTOR, UNKNOWN
    }

    data class Resolution(
        val width: Int,
        val height: Int,
    )

    data class Image(
        override val parent: ComposableNode,
        override val source: Source,
        override val isCacheEnabled: Boolean?,
        override val hasPlaceholder: Boolean?,
        override val extension: Extension,
        override val resolution: Resolution,
    ) : ImageType {

        override fun toString(): String {
            return """Image(
                source=$source, 
                isCacheEnabled=$isCacheEnabled, 
                hasPlaceholder=$hasPlaceholder, 
                extension=$extension, 
                resolution=$resolution,
            )""".trimMargin()
        }
    }

    data class AsyncImage(
        override val parent: ComposableNode,
        override val source: Source = Source.REMOTE,
        override val isCacheEnabled: Boolean,
        override val hasPlaceholder: Boolean,
        override val extension: Extension,
        override val resolution: Resolution,
    ) : ImageType {

        override fun toString(): String {
            return """AsyncImage(
                source=$source, 
                isCacheEnabled=$isCacheEnabled, 
                hasPlaceholder=$hasPlaceholder,
                extension=$extension,
                resolution=$resolution,
            )""".trimMargin()
        }
    }

    data class GlideImage(
        override val parent: ComposableNode,
        override val source: Source = Source.REMOTE,
        override val isCacheEnabled: Boolean,
        override val hasPlaceholder: Boolean,
        override val extension: Extension,
        override val resolution: Resolution,
    ) : ImageType {

        override fun toString(): String {
            return """GlideImage(
                source=$source, 
                isCacheEnabled=$isCacheEnabled, 
                hasPlaceholder=$hasPlaceholder, 
                extension=$extension,
                resolution=$resolution,
            )""".trimMargin()
        }
    }
}