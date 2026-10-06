package com.ghosten.videoplayer

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.text.DefaultSubtitleParserFactory
import androidx.media3.extractor.text.SubtitleParser

@UnstableApi
internal class SsaSubtitleParserFactory(
    private val delegate: SubtitleParser.Factory = DefaultSubtitleParserFactory(),
) : SubtitleParser.Factory {
    override fun supportsFormat(format: Format): Boolean = delegate.supportsFormat(format)

    override fun getCueReplacementBehavior(format: Format): Int =
        delegate.getCueReplacementBehavior(format)

    override fun create(format: Format): SubtitleParser = delegate.create(sanitizeSsaFormat(format))

    private fun sanitizeSsaFormat(format: Format): Format {
        if (format.sampleMimeType != MimeTypes.TEXT_SSA || format.initializationData.size < 2) {
            return format
        }

        val originalHeader = format.initializationData[1].toString(Charsets.UTF_8)
        val sanitizedHeader = sanitizeSsaHeader(originalHeader)
        if (sanitizedHeader == originalHeader) return format

        val initializationData = format.initializationData.toMutableList()
        initializationData[1] = sanitizedHeader.toByteArray(Charsets.UTF_8)
        return format.buildUpon().setInitializationData(initializationData).build()
    }
}

internal fun sanitizeSsaHeader(header: String): String =
    replaceInvalidPlayResolution(
        replaceInvalidPlayResolution(header, "PlayResX", 384),
        "PlayResY",
        288,
    )

private fun replaceInvalidPlayResolution(header: String, key: String, fallback: Int): String {
    val pattern = Regex(
        pattern = "(?im)^([\\t ]*$key[\\t ]*:[\\t ]*)([-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))([\\t ]*\\r?)$",
    )
    return pattern.replace(header) { match ->
        val value = match.groupValues[2].toFloatOrNull()
        if (value != null && value > 0f) {
            match.value
        } else {
            "${match.groupValues[1]}$fallback${match.groupValues[3]}"
        }
    }
}
