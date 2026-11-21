package com.zaurh.notefirelocal

import com.zaurh.notefirelocal.data.local.migrateLegacyBackground
import com.zaurh.notefirelocal.data.local.migrateLegacyTextColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyFormattingTest {

    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()
    private val green = 0xFF4CAF50.toInt()

    @Test
    fun `old default white background follows the theme`() {
        assertNull(migrateLegacyBackground(white))
    }

    @Test
    fun `picked backgrounds are kept exactly`() {
        assertEquals(green, migrateLegacyBackground(green))
        assertEquals(black, migrateLegacyBackground(black))
    }

    @Test
    fun `translucent backgrounds keep the look they had over white`() {
        // 50% black over white was drawn as mid grey.
        assertEquals(0xFF7F7F7F.toInt(), migrateLegacyBackground(0x80000000.toInt()))
        // Fully transparent looked white, i.e. the default.
        assertNull(migrateLegacyBackground(0x00FF0000))
    }

    @Test
    fun `default black text on default white note follows the theme`() {
        assertNull(migrateLegacyTextColor(black, white))
    }

    @Test
    fun `picked text colors and text on picked backgrounds are kept`() {
        assertEquals(0xFFD32F2F.toInt(), migrateLegacyTextColor(0xFFD32F2F.toInt(), white))
        assertEquals(black, migrateLegacyTextColor(black, green))
    }
}
