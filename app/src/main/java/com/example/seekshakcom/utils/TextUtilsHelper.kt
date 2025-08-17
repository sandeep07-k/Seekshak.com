package com.example.seekshakcom.utils

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan

object TextUtilsHelper {

    /**
     * Builds a spannable string with:
     *  - Bold label (e.g. "FROM: ")
     *  - Bold or normal value (configurable)
     *  - Optional color styling for value
     */
    fun setBoldLabel(
        label: String?,
        value: String?,
        valueColor: Int? = null,
        isValueBold: Boolean = false // <-- new option
    ): SpannableString {
        val safeLabel = label ?: ""
        val safeValue = value ?: ""
        val fullText = safeLabel + safeValue
        val spannable = SpannableString(fullText)

        // Bold for label
        if (safeLabel.isNotEmpty()) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                safeLabel.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Bold for value if required
        if (isValueBold && safeValue.isNotEmpty()) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                safeLabel.length,
                fullText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Apply color to value if provided
        valueColor?.let {
            if (safeValue.isNotEmpty()) {
                spannable.setSpan(
                    ForegroundColorSpan(it),
                    safeLabel.length,
                    fullText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return spannable
    }
}
