package com.wa2c.android.storageimageviewer.common.values

import java.util.Locale

/**
 * Language
 */
enum class Language(
    /** Language code */
    val code: String
) {
    /** English */
    ENGLISH("en"),
    /** Japanese */
    JAPANESE("ja"),
    ;

    /** Index */
    val index: Int = this.ordinal

    companion object {
        val default: Language
            get() =  findByCodeOrDefault(Locale.getDefault().language)

        /** Find value or default by code */
        fun findByCodeOrDefault(code: String?): Language {
            return entries.firstOrNull { it.code == code } ?: ENGLISH
        }

        /** Find value or default by index. */
        fun findByIndexOrDefault(index: Int?): Language {
            return entries.firstOrNull { it.index == index } ?: ENGLISH
        }
    }
}
