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
            get() = findByCodeOrDefault(Locale.getDefault().language)

        /** Find value or default by code */
        fun findByCodeOrDefault(code: String?): Language {
            val locale = Locale.getDefault()
            return entries.firstOrNull { it.code == code }
                ?: entries.firstOrNull { it.code == locale.toLanguageTag() }
                ?: entries.firstOrNull { it.code == locale.language }
                ?: ENGLISH
        }
    }
}
