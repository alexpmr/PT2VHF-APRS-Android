package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AsciiFoldTest {
    @Test fun removesPortugueseAccents() {
        assertEquals("Ola, tudo bem? Cambio!", stripAccents("Olá, tudo bem? Câmbio!"))
        assertEquals("acao CORACAO", stripAccents("ação CORAÇÃO"))
        assertEquals("1o lugar, 2a vez", stripAccents("1º lugar, 2ª vez"))
    }

    @Test fun mapsSpecialLettersAndPunctuation() {
        assertEquals("Strasse aeroe \"ok\" - ...", stripAccents("Straße æroe “ok” – …"))
        assertEquals("Nino senor", stripAccents("Niño señor"))
    }

    @Test fun keepsAsciiAndReplacesTheRest() {
        assertEquals("abc ABC 123 :{}|~", stripAccents("abc ABC 123 :{}|~"))
        assertEquals("ok ?", stripAccents("ok 😀"))
        assertEquals("? ?", stripAccents("北 京"))
        assertTrue(stripAccents("Olá ação ñ ü ß € ☺").all { it.code in 0x20..0x7E })
    }

    @Test fun strippedTextFitsAprsLimit() {
        val text = "é".repeat(80)
        val parts = splitMessage(stripAccents(text))
        assertEquals(listOf("e".repeat(67), "e".repeat(13)), parts)
    }
}
