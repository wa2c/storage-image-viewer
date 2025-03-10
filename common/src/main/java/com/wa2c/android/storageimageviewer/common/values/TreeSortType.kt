package com.wa2c.android.storageimageviewer.common.values

/**
 * Sort type
 */
enum class TreeSortType(
    val value: String,
) {
    /** Name */
    Name("NAME"),
    /** Size */
    Size("SIZE"),
    /** Date */
    Date("DATE"),
    ;

    companion object {
        fun findByValueOrDefault(value: String?): TreeSortType {
            return entries.firstOrNull { it.value == value } ?: Name
        }
    }
}
