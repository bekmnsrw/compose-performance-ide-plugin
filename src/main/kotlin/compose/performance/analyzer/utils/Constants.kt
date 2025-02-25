package compose.performance.analyzer.utils

import org.jetbrains.kotlin.name.FqName

/**
 * @author i.bekmansurov
 */
internal object Constants {

    /**
     * Packages
     */
    private const val COLLECTIONS_IMMUTABLE_ROOT = "kotlinx.collections.immutable"
    private const val COMPOSE_PACKAGE_NAME = "androidx.compose"
    const val COMPOSE_ROOT = "$COMPOSE_PACKAGE_NAME.runtime"
    private const val GOOGLE_COLLECT_ROOT = "com.google.common.collect"

    /**
     * Annotations
     */
    const val SUPPRESS = "Suppress"

    val EXPLICIT_GROUPS_COMPOSABLE_FQ_NAME = FqName("$COMPOSE_ROOT.ExplicitGroupsComposable")
    val NON_RESTARTABLE_COMPOSABLE_FQ_NAME = FqName("$COMPOSE_ROOT.NonRestartableComposable")
    val NON_SKIPPABLE_COMPOSABLE_FQ_NAME = FqName("$COMPOSE_ROOT.NonSkippableComposable")


    /**
     * Stable Collections
     */
    const val PERSISTENT_LIST_IMPORT = "$COLLECTIONS_IMMUTABLE_ROOT.PersistentList"
    const val PERSISTENT_MAP_IMPORT = "$COLLECTIONS_IMMUTABLE_ROOT.PersistentMap"
    const val PERSISTENT_SET_IMPORT = "$COLLECTIONS_IMMUTABLE_ROOT.PersistentSet"

    val STABLE_COLLECTIONS = listOf(
        "$GOOGLE_COLLECT_ROOT.ImmutableEnumMap",
        "$GOOGLE_COLLECT_ROOT.ImmutableEnumSet",
        "$GOOGLE_COLLECT_ROOT.ImmutableList",
        "$GOOGLE_COLLECT_ROOT.ImmutableMap",
        "$GOOGLE_COLLECT_ROOT.ImmutableSet",

        "$COLLECTIONS_IMMUTABLE_ROOT.ImmutableCollection",
        "$COLLECTIONS_IMMUTABLE_ROOT.ImmutableList",
        "$COLLECTIONS_IMMUTABLE_ROOT.ImmutableMap",
        "$COLLECTIONS_IMMUTABLE_ROOT.ImmutableSet",
        "$COLLECTIONS_IMMUTABLE_ROOT.PersistentCollection",
        "$COLLECTIONS_IMMUTABLE_ROOT.immutableListOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.immutableMapOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.immutableSetOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.persistentListOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.persistentMapOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.persistentSetOf",
        PERSISTENT_LIST_IMPORT,
        PERSISTENT_MAP_IMPORT,
        PERSISTENT_SET_IMPORT,
    )

    /**
     * Images
     */
    const val IMAGE = "Image"
    const val ASYNC_IMAGE = "AsyncImage"
    const val GLIDE_IMAGE = "GlideImage"

    const val ASYNC_IMAGE_PAINTER = "rememberAsyncImagePainter"
    const val PLACEHOLDER = "placeholder"
    const val CACHE_POLICY = "cachePolicy"
    const val CACHE_STRATEGY = "cacheStrategy"

    const val DRAWABLE_RES = "R.drawable."
    const val DRAWABLE = "drawable"
    const val MIPMAP_RES = "R.mipmap."
    const val MIPMAP = "mipmap"

    const val PROJECT_RES_PATH = "app/src/main/res"

    /**
     * Lists
     */
    const val COLUMN = "Column"
    const val ROW = "Row"
    const val LAZY_COLUMN = "LazyColumn"
    const val LAZY_ROW = "LazyRow"

    /**
     * Other
     */
    const val FUN = "fun"
    const val NON_SKIPPABLE_COMPOSABLE = "NON_SKIPPABLE_COMPOSABLE"
    const val NON_SKIPPABLE_COMPOSABLE_CAMEL_CASE = "NonSkippableComposable"
    const val NON_SKIPPABLE_COMPOSABLE_ERROR_MESSAGE = "Non skippable function, that will lead to unnecessary recompositions. Consider all function arguments to be stable"
    const val NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE = "Remove unused @Suppress"
    const val COLON = ":"
    const val PERIOD = "."
    const val LEFT_CURLY_BRACE = "{"
    const val RIGHT_CURLY_BRACE = "}"
    const val METHOD_REFERENCE = "::"

    /**
     * Common
     */
    const val STATE = "state"
    const val LEFT_PARENTHESIS = "("
    const val RIGHT_PARENTHESIS = ")"
    const val RIGHT_ARROW = "->"
    const val EMPTY_STRING = ""
}
