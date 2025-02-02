package com.wa2c.android.storageimageviewer.common.values

/**
 * View type
 */
enum class TreeViewType(
    val value: String,
    val isList: Boolean,
    val isLarge: Boolean,
) {
    /** List Large */
    ListLarge("LIST_LARGE", true, true),
    /** List Small */
    ListSmall("LIST_SMALL", true, false),
    /** Grid Large */
    GridLarge("GRID_LARGE", false, true),
    /** Grid Small */
    GridSmall("GRID_SMALL", false, false),
    ;

    companion object {
        fun findByValueOrDefault(value: String?): TreeViewType {
            return entries.firstOrNull { it.value == value } ?: ListLarge
        }
    }
}
