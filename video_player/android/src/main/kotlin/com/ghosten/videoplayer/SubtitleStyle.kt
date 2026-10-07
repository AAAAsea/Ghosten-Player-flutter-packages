package com.ghosten.videoplayer

import java.util.Locale

internal fun mpvSubtitleStyleOptions(style: List<Int>): Map<String, String> {
    if (style.size < 4) return emptyMap()
    val bottomPadding = subtitleBottomPaddingFromStyle(style)
    val position = bottomPadding?.let { 100f - (it * 100f) } ?: 100f

    return mapOf(
        "sub-color" to mpvColorFromArgb(style[0]),
        "sub-back-color" to mpvColorFromArgb(style[1]),
        "sub-border-color" to mpvColorFromArgb(style[3]),
        "sub-pos" to position.toString(),
    )
}

internal fun mpvColorFromArgb(color: Int): String = String.format(Locale.ROOT, "#%08X", color)
