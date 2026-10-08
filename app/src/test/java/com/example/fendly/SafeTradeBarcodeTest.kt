package com.example.fendly

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SafeTradeBarcodeTest {
    @Test
    fun extractsPlainImeiFromBarcode() {
        assertEquals("490154203237518", imeiFromBarcodePayload("490154203237518"))
    }

    @Test
    fun extractsImeiWhenDigitsAreGrouped() {
        assertEquals("490154203237518", imeiFromBarcodePayload("IMEI: 490 154 203 237 518"))
    }

    @Test
    fun extractsFirstImeiWhenBarcodeContainsTwoImeiValues() {
        assertEquals("490154203237518", imeiFromBarcodePayload("490154203237518490154203237526"))
    }

    @Test
    fun doesNotTreatOtherBarcodeLengthsAsImei() {
        assertNull(imeiFromBarcodePayload("49015420323751"))
    }

    @Test
    fun extractsImeiFromPayloadWithLabelDigits() {
        assertEquals("490154203237518", imeiFromBarcodePayload("IMEI 1: 490154203237518"))
    }
}
