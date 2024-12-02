package ru.bekmnsrw.compose.performance.analyzer.utils

import org.jetbrains.kotlin.name.FqName

/**
 * @author bekmnsrw
 */
internal object Constants {

    private const val COMPOSE_PACKAGE_NAME = "androidx.compose"
    private const val COMPOSE_ROOT = "$COMPOSE_PACKAGE_NAME.runtime"
    private const val COLLECTIONS_IMMUTABLE_ROOT = "kotlinx.collections.immutable"
    private const val GOOGLE_COLLECT_ROOT = "com.google.common.collect"

    const val COMPOSABLE_FUNCTION = "$COMPOSE_ROOT.internal.ComposableFunction"
    const val COMPOSABLE_SHORT_NAME = "Composable"
    const val STATE = "state"

    val STABLE_FQ_NAME = FqName("$COMPOSE_ROOT.Stable")
    val IMMUTABLE_FQ_NAME = FqName("$COMPOSE_ROOT.Immutable")

    val STABLE_COLLECTIONS = listOf(
        "$GOOGLE_COLLECT_ROOT.ImmutableList",
        "$GOOGLE_COLLECT_ROOT.ImmutableEnumMap",
        "$GOOGLE_COLLECT_ROOT.ImmutableMap",
        "$GOOGLE_COLLECT_ROOT.ImmutableEnumSet",
        "$GOOGLE_COLLECT_ROOT.ImmutableSet",

        "$COLLECTIONS_IMMUTABLE_ROOT.immutableListOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.immutableSetOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.immutableMapOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.persistentListOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.persistentSetOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.persistentMapOf",
        "$COLLECTIONS_IMMUTABLE_ROOT.ImmutableCollection",
        "$COLLECTIONS_IMMUTABLE_ROOT.ImmutableList",
        "$COLLECTIONS_IMMUTABLE_ROOT.ImmutableSet",
        "$COLLECTIONS_IMMUTABLE_ROOT.ImmutableMap",
        "$COLLECTIONS_IMMUTABLE_ROOT.PersistentCollection",
        "$COLLECTIONS_IMMUTABLE_ROOT.PersistentList",
        "$COLLECTIONS_IMMUTABLE_ROOT.PersistentSet",
        "$COLLECTIONS_IMMUTABLE_ROOT.PersistentMap",
    )
}