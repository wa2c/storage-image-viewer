package com.wa2c.android.storageimageviewer.common.values

/**
 * View type
 */
enum class TreeViewType(
    val isList: Boolean,
    val isLarge: Boolean,
) {
    /** List Large */
    ListLarge(true, true),
    /** List Small */
    ListSmall(true, false),
    /** Grid Large */
    GridLarge(false, true),
    /** Grid Small */
    GridSmall(false, false),
    ;
}
