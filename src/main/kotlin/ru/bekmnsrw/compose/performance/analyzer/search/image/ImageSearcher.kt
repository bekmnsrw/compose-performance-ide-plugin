package ru.bekmnsrw.compose.performance.analyzer.search.image

import com.intellij.openapi.application.runReadAction
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import ru.bekmnsrw.compose.performance.analyzer.recomposition.model.ComposableNode
import ru.bekmnsrw.compose.performance.analyzer.search.kmpSearch
import ru.bekmnsrw.compose.performance.analyzer.search.model.ImageType
import ru.bekmnsrw.compose.performance.analyzer.search.model.ImageType.*
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.ASYNC_IMAGE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.ASYNC_IMAGE_PAINTER
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.CACHE_POLICY
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.CACHE_STRATEGY
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.DRAWABLE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.DRAWABLE_RES
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.GLIDE_IMAGE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.IMAGE
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.LEFT_PARENTHESIS
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.MIPMAP
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.MIPMAP_RES
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.PLACEHOLDER
import ru.bekmnsrw.compose.performance.analyzer.utils.Constants.PROJECT_RES_PATH
import java.io.File
import javax.imageio.ImageIO

/**
 * @author bekmnsrw
 */
internal class ImageSearcher(
    private val ktFile: KtFile,
) {

    private val imageReferences: List<Pair<String, String>> by lazy { findImageReferences() }

    fun search(composables: List<ComposableNode>) {
        val images = mutableListOf<ComposableNode>()
        val asyncImages = mutableListOf<ComposableNode>()
        val glideImages = mutableListOf<ComposableNode>()

        composables.forEach { composable ->
            composable.nestedNodes.forEach { nested ->
                if (kmpSearch(nested.text, " $IMAGE$LEFT_PARENTHESIS").isNotEmpty()) {
                    images.add(nested)
                }
                if (kmpSearch(nested.text, " $ASYNC_IMAGE$LEFT_PARENTHESIS").isNotEmpty()) {
                    asyncImages.add(nested)
                }
                if (kmpSearch(nested.text, " $GLIDE_IMAGE$LEFT_PARENTHESIS").isNotEmpty()) {
                    glideImages.add(nested)
                }
            }
        }

        images.forEach { composable ->
            getImage(composable, IMAGE).forEach { image ->
                val imageRes = imageReferences.find { image.text.contains(it.first) } ?: ("" to "")
                println(analyzeImage(composable, image, imageRes))
            }
        }

        asyncImages.forEach { composable ->
            getImage(composable, ASYNC_IMAGE).forEach { asyncImage ->
                val imageRes = imageReferences.find { asyncImage.text.contains(it.first) } ?: ("" to "")
                println(analyzeAsyncImage(composable, asyncImage, imageRes))
            }
        }

        glideImages.forEach { composable ->
            getImage(composable, GLIDE_IMAGE).forEach { glideImage ->
                val imageRes = imageReferences.find { glideImage.text.contains(it.first) } ?: ("" to "")
                println(analyzeGlideImage(composable, glideImage, imageRes))
            }
        }
    }

    private fun analyzeImage(
        parent: ComposableNode,
        image: KtCallExpression,
        imageRef: Pair<String, String>,
        ): ImageType {
        val isImageRemote = image.isRemote()
        val imagePath = getImagePath(imageRef.second, imageRef.first)
        val (resolution, extension) = getImageResolutionAndExtension(imagePath)
        return Image(
            parent = parent,
            source = if (isImageRemote) Source.REMOTE else Source.LOCAL,
            isCacheEnabled = if (isImageRemote) image.isCacheEnabled() else null,
            hasPlaceholder = if (isImageRemote) image.hasPlaceholder() else null,
            extension = if (image.isImageVectorExtension()) Extension.IMAGE_VECTOR else extension,
            resolution = resolution,
        )
    }

    private fun analyzeAsyncImage(
        parent: ComposableNode,
        image: KtCallExpression,
        imageRef: Pair<String, String>,
        ): ImageType {
        val imagePath = getImagePath(imageRef.second, imageRef.first)
        val (resolution, extension) = getImageResolutionAndExtension(imagePath)
        return AsyncImage(
            parent = parent,
            isCacheEnabled = image.isCacheEnabled(),
            hasPlaceholder = image.hasPlaceholder(),
            extension = extension,
            resolution = resolution,
        )
    }

    private fun analyzeGlideImage(
        parent: ComposableNode,
        image: KtCallExpression,
        imageRef: Pair<String, String>,
    ): ImageType {
        val imagePath = getImagePath(imageRef.second, imageRef.first)
        val (resolution, extension) = getImageResolutionAndExtension(imagePath)
        return GlideImage(
            parent = parent,
            isCacheEnabled = image.isGlideImageCacheEnabled(),
            hasPlaceholder = image.hasPlaceholder(),
            extension = extension,
            resolution = resolution,
        )
    }

    private fun getImage(
        parent: ComposableNode,
        functionName: String,
    ): List<KtCallExpression> {
        return runReadAction {
            ktFile.collectDescendantsOfType<KtFunction>()
                .first { it.name == parent.name }
                .bodyExpression
                ?.collectDescendantsOfType<KtCallExpression>()
                ?.filter { it.text.contains(functionName + LEFT_PARENTHESIS) }
                ?: emptyList()
        }
    }

    private fun findImageReferences(): List<Pair<String, String>> {
        return ktFile.collectDescendantsOfType<KtQualifiedExpression>()
            .mapNotNull { expression ->
                val text = expression.text
                when {
                    text.startsWith(DRAWABLE_RES) -> text.removePrefix(DRAWABLE_RES) to DRAWABLE
                    text.startsWith(MIPMAP_RES) -> text.removePrefix(MIPMAP_RES) to MIPMAP
                    else -> null
                }
            }
    }

    private fun getImageResolutionAndExtension(filePath: String): Pair<Resolution, Extension> {
        // Don't handle `IMAGE_VECTOR` and `UNKNOWN`
        val extensions = Extension.entries.dropLast(2)
        
        for (extension in extensions) {
            val lowerCaseExtension = extension.name.lowercase()
            val fileFullName = "$filePath.$lowerCaseExtension"
            val imageFile = File(fileFullName)

            if (imageFile.exists()) {
                val image = ImageIO.read(imageFile)

                return if (image != null) {
                    Resolution(image.width, image.height) to extension
                } else {
                    Resolution(-1, -1) to extension
                }
            }
        }

        return Resolution(-1, -1) to Extension.UNKNOWN
    }

    private fun getImagePath(folder: String, name: String): String {
        val projectPath = ktFile.project.basePath
        return "$projectPath/$PROJECT_RES_PATH/$folder/$name"
    }

    private fun KtCallExpression.isRemote(): Boolean {
        return this.text.contains(
            other = ASYNC_IMAGE_PAINTER,
            ignoreCase = true,
        )
    }

    private fun KtCallExpression.hasPlaceholder(): Boolean {
        return this.text.contains(
            other = PLACEHOLDER,
            ignoreCase = true,
        )
    }

    private fun KtCallExpression.isCacheEnabled(): Boolean {
        return this.text.contains(
            other = CACHE_POLICY,
            ignoreCase = true,
        )
    }

    private fun KtCallExpression.isGlideImageCacheEnabled(): Boolean {
        return this.text.contains(
            other = CACHE_STRATEGY,
            ignoreCase = true,
        )
    }

    private fun KtCallExpression.isImageVectorExtension(): Boolean {
        return this.text.contains("ImageVector", true)
    }
}
