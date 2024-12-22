package ru.bekmnsrw.compose.performance.analyzer.utils

import org.jetbrains.kotlin.name.FqName

/**
 * @author bekmnsrw
 */
internal object Constants {

    /**
     * Packages
     */
    private const val COLLECTIONS_IMMUTABLE_ROOT = "kotlinx.collections.immutable"
    private const val COMPOSE_PACKAGE_NAME = "androidx.compose"
    private const val COMPOSE_ROOT = "$COMPOSE_PACKAGE_NAME.runtime"
    private const val GOOGLE_COLLECT_ROOT = "com.google.common.collect"

    /**
     * Annotations
     */
    const val COMPOSABLE_FQ_NAME = "androidx.compose.runtime.Composable"
    const val COMPOSABLE_FUNCTION = "$COMPOSE_ROOT.internal.ComposableFunction"
    const val COMPOSABLE_SHORT_NAME = "Composable"
    const val SUPPRESS = "Suppress"

    val EXPLICIT_GROUPS_COMPOSABLE_FQ_NAME = FqName("$COMPOSE_ROOT.ExplicitGroupsComposable")
    val IMMUTABLE_FQ_NAME = FqName("$COMPOSE_ROOT.Immutable")
    val NON_RESTARTABLE_COMPOSABLE_FQ_NAME = FqName("$COMPOSE_ROOT.NonRestartableComposable")
    val NON_SKIPPABLE_COMPOSABLE_FQ_NAME = FqName("$COMPOSE_ROOT.NonSkippableComposable")
    val STABLE_FQ_NAME = FqName("$COMPOSE_ROOT.Stable")

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
     * Other
     */
    const val FUN = "fun"
    const val NON_SKIPPABLE_COMPOSABLE = "NON_SKIPPABLE_COMPOSABLE"
    const val NON_SKIPPABLE_COMPOSABLE_CAMEL_CASE = "NonSkippableComposable"
    const val NON_SKIPPABLE_COMPOSABLE_ERROR_MESSAGE = "Non skippable function, that will lead to unnecessary recompositions. Consider all function arguments to be stable"
    const val NON_SKIPPABLE_COMPOSABLE_WARNING_MESSAGE = "Remove unused @Suppress"
    const val STATE = "state"
    const val COLON = ":"
    const val PERIOD = "."
    const val FUNCTION_TYPE = "Function"
    const val LEFT_CURLY_BRACE = "{"
    const val RIGHT_CURLY_BRACE = "}"
    const val METHOD_REFERENCE = "::"
    const val REMEMBER = "remember"
    const val LEFT_PARENTHESIS = "("
    const val RIGHT_PARENTHESIS = ")"
    const val RIGHT_ARROW = "->"
    const val LIST = "List"
    const val MAP = "Map"
    const val SET = "Set"
    const val GREATER_THAN = ">"
    const val LESS_THAN = "<"
}