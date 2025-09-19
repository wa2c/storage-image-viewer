package com.wa2c.android.storageimageviewer.common.values

/**
 * Folder Order
 */
enum class TreeFolderOrder(
    val value: String,
) {
    /** Name */
    First("FIRST"),
    /** Size */
    Last("LAST"),
    /** Date */
    Mix("MIX"),
    ;

    companion object {
        val DEFAULT = First

        fun findByValueOrDefault(value: String?): TreeFolderOrder {
            return entries.firstOrNull { it.value == value } ?: DEFAULT
        }
    }
}
