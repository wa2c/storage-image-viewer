package com.wa2c.android.storageimageviewer.common.values

/**
 * URI type
 */
enum class UriType {
    /** File URI (file://...) */
    File,
    /** Content URI (content://...) */
    Content,
    ;

    companion object {

        fun fromUriText(uriText: String): UriType? {
            return when {
                uriText.startsWith("content://") -> return Content
                uriText.startsWith("file://") -> return File
                else -> null
            }
        }

    }

}
