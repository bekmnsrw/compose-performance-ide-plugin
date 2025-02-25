package compose.performance.analyzer.kmp.image

import com.intellij.openapi.application.runReadAction
import kotlinx.coroutines.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import compose.performance.analyzer.ast.ComposableNode
import compose.performance.analyzer.kmp.kmpSearch
import compose.performance.analyzer.kmp.image.ImageType.*
import compose.performance.analyzer.kmp.image.api.RetrofitClient
import compose.performance.analyzer.utils.Constants.ASYNC_IMAGE
import compose.performance.analyzer.utils.Constants.ASYNC_IMAGE_PAINTER
import compose.performance.analyzer.utils.Constants.CACHE_POLICY
import compose.performance.analyzer.utils.Constants.CACHE_STRATEGY
import compose.performance.analyzer.utils.Constants.DRAWABLE
import compose.performance.analyzer.utils.Constants.DRAWABLE_RES
import compose.performance.analyzer.utils.Constants.GLIDE_IMAGE
import compose.performance.analyzer.utils.Constants.IMAGE
import compose.performance.analyzer.utils.Constants.LEFT_PARENTHESIS
import compose.performance.analyzer.utils.Constants.MIPMAP
import compose.performance.analyzer.utils.Constants.MIPMAP_RES
import compose.performance.analyzer.utils.Constants.PERIOD
import compose.performance.analyzer.utils.Constants.PLACEHOLDER
import compose.performance.analyzer.utils.Constants.PROJECT_RES_PATH
import java.io.File
import javax.imageio.ImageIO

/**
 * @author i.bekmansurov
 */
internal class ImageSearcher(private val ktFile: KtFile) {

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

        CoroutineScope(SupervisorJob() + CoroutineName("ImageSearcherScope")).launch {
            processImages(images, Type.IMAGE)
            processImages(asyncImages, Type.ASYNC)
            processImages(glideImages, Type.GLIDE)
        }
    }

    private suspend fun processImages(composables: List<ComposableNode>, type: Type) {
        composables.forEach { composable ->
            val images = runReadAction { getImage(composable, type) }
            images.forEach { image ->
                when (type) {
                    Type.IMAGE -> println(analyzeImage(composable, image))
                    Type.ASYNC -> println(analyzeAsyncImage(composable, image))
                    Type.GLIDE -> println(analyzeGlideImage(composable, image))
                }
            }
        }
    }

    private suspend fun analyzeImage(parent: ComposableNode, image: KtCallExpression): ImageType {
        val isImageRemote = image.isRemote()
        val source = if (isImageRemote) {
            val url = runReadAction { getRemoteImageUrl(image, Type.IMAGE) }
            val (resolution, extension) = fetchRemoteImage(url)

            runReadAction {
                Source.Remote(
                    url = url,
                    extension = extension,
                    resolution = resolution,
                )
            }
        } else {
            runReadAction {
                val isImageVector = image.isImageVectorExtension()
                val imageRes = imageReferences.find { image.text.contains(it.first) } ?: ("" to "")
                val localImagePath = getImagePath(imageRes.second, imageRes.first)
                val (resolution, extension) = getImageResolutionAndExtension(localImagePath)

                Source.Local(
                    path = if (isImageVector) "" else localImagePath,
                    extension = if (isImageVector) Extension.IMAGE_VECTOR else extension,
                    resolution = resolution,
                )
            }
        }

        return Image(
            parent = parent,
            source = source,
            isCacheEnabled = if (isImageRemote) image.isCacheEnabled() else null,
            hasPlaceholder = if (isImageRemote) image.hasPlaceholder() else null,
        )
    }

    private suspend fun analyzeAsyncImage(parent: ComposableNode, image: KtCallExpression): ImageType {
        val url = runReadAction { getRemoteImageUrl(image, Type.ASYNC) }
        val (resolution, extension) = fetchRemoteImage(url)

        return AsyncImage(
            parent = parent,
            source = Source.Remote(
                url = url,
                extension = extension,
                resolution = resolution,
            ),
            isCacheEnabled = image.isCacheEnabled(),
            hasPlaceholder = image.hasPlaceholder(),
        )
    }

    private suspend fun analyzeGlideImage(parent: ComposableNode, image: KtCallExpression): ImageType {
        val url = runReadAction { getRemoteImageUrl(image, Type.GLIDE) }
        val (resolution, extension) = fetchRemoteImage(url)

        return GlideImage(
            parent = parent,
            source = Source.Remote(
                url = url,
                extension = extension,
                resolution = resolution,
            ),
            isCacheEnabled = image.isGlideImageCacheEnabled(),
            hasPlaceholder = image.hasPlaceholder(),
        )
    }

    private fun getImage(parent: ComposableNode, type: Type): List<KtCallExpression> {
        val functionName = when (type) {
            Type.IMAGE -> IMAGE
            Type.ASYNC -> ASYNC_IMAGE
            Type.GLIDE -> GLIDE_IMAGE
        }

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
        return runReadAction {
            ktFile.collectDescendantsOfType<KtQualifiedExpression>()
                .mapNotNull { expression ->
                    val text = expression.text
                    when {
                        text.startsWith(DRAWABLE_RES) -> text.removePrefix(DRAWABLE_RES) to DRAWABLE
                        text.startsWith(MIPMAP_RES) -> text.removePrefix(MIPMAP_RES) to MIPMAP
                        else -> null
                    }
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
        return runReadAction {
            val projectPath = ktFile.project.basePath
            "$projectPath/$PROJECT_RES_PATH/$folder/$name"
        }
    }

    private suspend fun fetchRemoteImage(imageUrl: String): Pair<Resolution, Extension> {
        val response = RetrofitClient.imageSearcherApi.fetchImage(imageUrl)

        if (response.isSuccessful) {
            response.body()?.let { body ->
                body.byteStream().use { stream ->
                    val image = ImageIO.read(stream)
                    val extension = imageUrl.substringAfterLast(
                        delimiter = PERIOD,
                        missingDelimiterValue = "",
                    )

                    return Resolution(image.width, image.height) to mapExtension(extension)
                }
            }
        }

        return Resolution(-1, -1) to Extension.UNKNOWN
    }

    private fun mapExtension(extension: String): Extension {
        return Extension.entries
            .dropLast(2)
            .find { it.name.lowercase() == extension } ?: Extension.UNKNOWN
    }

    private fun getRemoteImageUrl(image: KtCallExpression, imageType: Type): String {
        return runReadAction {
            when (imageType) {
                Type.ASYNC -> getAsyncImageUrl(image)
                Type.IMAGE -> getImageUrl(image)
                Type.GLIDE -> getGlideImageUrl(image)
            }
        }
    }

    private fun getGlideImageUrl(glideImage: KtCallExpression): String {
        return runReadAction {
            val modelValue = glideImage.getModelParamValue()

            if (!modelValue.contains("null")) {
                val urlStartIndex = modelValue.indexOfFirst { it == '"' } + 1
                val urlEndIndex = modelValue.indexOfLast { it == '"' }
                modelValue.substring(urlStartIndex, urlEndIndex)
            } else {
                val requestBuilderParamValue = glideImage.getRequestBuilderParamValue()
                val loadStartIndex = requestBuilderParamValue.indexOf("load") + "load".length + 2
                val loadSubstring = requestBuilderParamValue.substring(loadStartIndex)
                val urlEndIndex = loadSubstring.indexOfFirst { it == '"' }
                loadSubstring.substring(0, urlEndIndex)
            }
        }
    }

    private fun getImageUrl(image: KtCallExpression): String {
        return runReadAction {
            val modelValue = image.getAsyncImagePainterModelParamValue()

            if (modelValue.contains("data")) {
                val dataStartIndex = modelValue.indexOf("data") + "data".length + 2
                val dataSubstring = modelValue.substring(dataStartIndex)
                val urlEndIndex = dataSubstring.indexOfFirst { it == '"' }
                dataSubstring.substring(0, urlEndIndex)
            } else {
                val urlStartIndex = modelValue.indexOfFirst { it == '"' } + 1
                val urlEndIndex = modelValue.indexOfLast { it == '"' }
                modelValue.substring(urlStartIndex, urlEndIndex)
            }
        }
    }

    private fun getAsyncImageUrl(image: KtCallExpression): String {
        return runReadAction {
            val modelValue = image.getModelParamValue()

            if (modelValue.contains("data")) {
                val dataStartIndex = modelValue.indexOf("data") + "data".length + 2
                val dataSubstring = modelValue.substring(dataStartIndex)
                val urlEndIndex = dataSubstring.indexOfFirst { it == '"' }
                dataSubstring.substring(0, urlEndIndex)
            } else {
                val urlStartIndex = modelValue.indexOfFirst { it == '"' } + 1
                val urlEndIndex = modelValue.indexOfLast { it == '"' }
                modelValue.substring(urlStartIndex, urlEndIndex)
            }
        }
    }

    private fun KtCallExpression.getModelParamValue(): String {
        return runReadAction {
            this.valueArguments.firstOrNull { argument ->
                argument.text.contains("model")
            }?.getArgumentExpression()?.text.orEmpty()
        }
    }

    private fun KtCallExpression.getRequestBuilderParamValue(): String {
        return runReadAction {
            val startIndex = this.text.indexOfFirst { it == '{' }
            val endIndex = this.text.indexOfLast { it == '}' }
            this.text.substring(startIndex, endIndex)
        }
    }

    private fun KtCallExpression.getAsyncImagePainterModelParamValue(): String {
        return runReadAction {
            val painter = this.valueArguments.firstOrNull { argument ->
                argument.text.contains("painter")
            }?.getArgumentExpression()

            val valueArgumentList = painter?.children?.find { child ->
                child is KtValueArgumentList
            } as? KtValueArgumentList

            valueArgumentList?.arguments?.firstOrNull { argument ->
                argument.text.contains("model")
            }?.getArgumentExpression()?.text.orEmpty()
        }
    }

    private fun KtCallExpression.isRemote(): Boolean {
        return runReadAction {
            this.text.contains(
                other = ASYNC_IMAGE_PAINTER,
                ignoreCase = true,
            )
        }
    }

    private fun KtCallExpression.hasPlaceholder(): Boolean {
        return runReadAction {
            this.text.contains(
                other = PLACEHOLDER,
                ignoreCase = true,
            )
        }
    }

    private fun KtCallExpression.isCacheEnabled(): Boolean {
        return runReadAction {
            this.text.contains(
                other = CACHE_POLICY,
                ignoreCase = true,
            )
        }
    }

    private fun KtCallExpression.isGlideImageCacheEnabled(): Boolean {
        return runReadAction {
            this.text.contains(
                other = CACHE_STRATEGY,
                ignoreCase = true,
            )
        }
    }

    private fun KtCallExpression.isImageVectorExtension(): Boolean {
        return runReadAction {
            this.text.contains("ImageVector", true)
        }
    }
}
