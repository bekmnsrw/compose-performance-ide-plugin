package compose.performance.analyzer.kmp.image

import compose.performance.analyzer.ast.ComposableNode

/**
 * @author i.bekmansurov
 */
internal sealed interface ImageType {

    val parent: ComposableNode
    val source: Source
    val isCacheEnabled: Boolean?
    val hasPlaceholder: Boolean?

    enum class Type {

        IMAGE, ASYNC, GLIDE
    }

    sealed interface Source {

        val extension: Extension
        val resolution: Resolution

        data class Remote(
            val url: String,
            override val extension: Extension,
            override val resolution: Resolution,
        ) : Source

        data class Local(
            val path: String,
            override val extension: Extension,
            override val resolution: Resolution,
        ) : Source
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
    ) : ImageType {

        override fun toString(): String {
            return """Image(
                source=$source, 
                isCacheEnabled=$isCacheEnabled, 
                hasPlaceholder=$hasPlaceholder, 
            )""".trimMargin()
        }
    }

    data class AsyncImage(
        override val parent: ComposableNode,
        override val source: Source,
        override val isCacheEnabled: Boolean,
        override val hasPlaceholder: Boolean,
    ) : ImageType {

        override fun toString(): String {
            return """AsyncImage(
                source=$source, 
                isCacheEnabled=$isCacheEnabled, 
                hasPlaceholder=$hasPlaceholder,
            )""".trimMargin()
        }
    }

    data class GlideImage(
        override val parent: ComposableNode,
        override val source: Source,
        override val isCacheEnabled: Boolean,
        override val hasPlaceholder: Boolean,
    ) : ImageType {

        override fun toString(): String {
            return """GlideImage(
                source=$source, 
                isCacheEnabled=$isCacheEnabled, 
                hasPlaceholder=$hasPlaceholder, 
            )""".trimMargin()
        }
    }
}