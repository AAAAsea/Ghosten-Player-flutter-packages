package com.ghosten.videoplayer

import kotlin.test.Test
import kotlin.test.assertEquals

class SsaSubtitleParserFactoryTest {
    @Test
    fun replacesZeroPlayResolutionWithAssDefaults() {
        val header = """
            [Script Info]
            PlayResX: 0
            PlayResY: 0
            [V4+ Styles]
        """.trimIndent()

        assertEquals(
            """
                [Script Info]
                PlayResX: 384
                PlayResY: 288
                [V4+ Styles]
            """.trimIndent(),
            sanitizeSsaHeader(header),
        )
    }

    @Test
    fun preservesValidPlayResolution() {
        val header = "PlayResX: 1920\r\nPlayResY: 1080\r\n"

        assertEquals(header, sanitizeSsaHeader(header))
    }

    @Test
    fun replacesNegativeAndDecimalZeroValuesCaseInsensitively() {
        val header = "playresx : -1\nPLAYRESY:\t0.0\n"

        assertEquals("playresx : 384\nPLAYRESY:\t288\n", sanitizeSsaHeader(header))
    }

    @Test
    fun readsPersistedSubtitleScaleAndSupportsLegacySettings() {
        assertEquals(0.8f, subtitleScaleFromStyle(listOf(1, 2, 3, 4, 80)))
        assertEquals(1f, subtitleScaleFromStyle(listOf(1, 2, 3, 4)))
        assertEquals(1f, subtitleScaleFromStyle(listOf(1, 2, 3, 4, Int.MAX_VALUE)))
    }

    @Test
    fun readsPersistedSubtitlePositionAndSupportsLegacySettings() {
        assertEquals(0.2f, subtitleBottomPaddingFromStyle(listOf(1, 2, 3, 4, 100, 20)))
        assertEquals(null, subtitleBottomPaddingFromStyle(listOf(1, 2, 3, 4, 100)))
        assertEquals(null, subtitleBottomPaddingFromStyle(listOf(1, 2, 3, 4, 100, -1)))
        assertEquals(null, subtitleBottomPaddingFromStyle(listOf(1, 2, 3, 4, 100, 75)))
    }

    @Test
    fun mapsSubtitleStyleToMpvColorsAndPosition() {
        val options = mpvSubtitleStyleOptions(
            listOf(0xFFFFFFFF.toInt(), 0xB3000000.toInt(), 0, 0xFF000000.toInt(), 100, 20)
        )

        assertEquals("#FFFFFFFF", options["sub-color"])
        assertEquals("#B3000000", options["sub-back-color"])
        assertEquals("#FF000000", options["sub-border-color"])
        assertEquals("80.0", options["sub-pos"])
    }
}
