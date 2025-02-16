package com.wa2c.android.storageimageviewer.common.values

/**
 * View type
 */
enum class TreeViewType(
    val value: String,
    val isList: Boolean,
    val isLarge: Boolean?,
) {
    /** List None */
    ListNone("LIST_NONE", true, null),
    /** List Small */
    ListSmall("LIST_SMALL", true, false),
    /** List Large */
    ListLarge("LIST_LARGE", true, true),
    /** Grid Small */
    GridSmall("GRID_SMALL", false, false),
    /** Grid Large */
    GridLarge("GRID_LARGE", false, true),
    ;

    companion object {
        fun findByValueOrDefault(value: String?): TreeViewType {
            return entries.firstOrNull { it.value == value } ?: ListLarge
        }
    }
}
