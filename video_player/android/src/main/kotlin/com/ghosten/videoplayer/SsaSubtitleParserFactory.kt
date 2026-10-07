package com.ghosten.videoplayer

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.text.Cue
import androidx.media3.common.util.Consumer
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.text.CuesWithTiming
import androidx.media3.extractor.text.DefaultSubtitleParserFactory
import androidx.media3.extractor.text.SubtitleParser

@UnstableApi
internal class SsaSubtitleParserFactory(
    textScale: Float = 1f,
    private val delegate: SubtitleParser.Factory = DefaultSubtitleParserFactory(),
) : SubtitleParser.Factory {
    private val textScale = textScale.coerceIn(0.5f, 2f)

    override fun supportsFormat(format: Format): Boolean = delegate.supportsFormat(format)

    override fun getCueReplacementBehavior(format: Format): Int =
        delegate.getCueReplacementBehavior(format)

    override fun create(format: Format): SubtitleParser =
        ScalingSubtitleParser(delegate.create(sanitizeSsaFormat(format)), textScale)

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

@UnstableApi
private class ScalingSubtitleParser(
    private val delegate: SubtitleParser,
    private val textScale: Float,
) : SubtitleParser {
    override fun parse(
        data: ByteArray,
        offset: Int,
        length: Int,
        outputOptions: SubtitleParser.OutputOptions,
        output: Consumer<CuesWithTiming>,
    ) {
        delegate.parse(data, offset, length, outputOptions) { cuesWithTiming ->
            val scaledCues = cuesWithTiming.cues.map { cue -> scaleCueTextSize(cue, textScale) }
            output.accept(CuesWithTiming(scaledCues, cuesWithTiming.startTimeUs, cuesWithTiming.durationUs))
        }
    }

    override fun getCueReplacementBehavior(): Int = delegate.cueReplacementBehavior

    override fun reset() = delegate.reset()
}

@UnstableApi
private fun scaleCueTextSize(cue: Cue, scale: Float): Cue {
    if (cue.textSize == Cue.DIMEN_UNSET || scale == 1f) return cue
    return cue.buildUpon().setTextSize(cue.textSize * scale, cue.textSizeType).build()
}

internal fun subtitleScaleFromStyle(style: List<Int>?): Float {
    val percent = style?.getOrNull(4)?.takeIf { it in 50..200 } ?: 100
    return percent / 100f
}

internal fun subtitleBottomPaddingFromStyle(style: List<Int>?): Float? {
    val percent = style?.getOrNull(5)?.takeIf { it in 0..50 } ?: return null
    return percent / 100f
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
