/*
 * MIT License
 *
 * Copyright (c) 2025 Gilian Marques Fernandes - linkedin.com/in/gilianmarques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dev.gmarques.controledenotificacoes.domain.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PreferencePropertyTest {

    private lateinit var storedValues: MutableMap<String, Any>
    private lateinit var preference: PreferenceProperty<String>

    @BeforeEach
    fun setup() {
        storedValues = mutableMapOf()

        preference = PreferenceProperty(
            key = "testKey",
            defaultValue = "default",
            preferenceReader = { key, default -> storedValues[key] as? String ?: default },
            preferenceSaver = { key, value -> storedValues[key] = value }
        )
    }

    @Test
    fun `ao acessar value deve retornar o valor salvo ou o padrao`() {
        assertEquals("default", preference.value)

        preference.set("novoValor")

        assertEquals("novoValor", preference.value)
    }

    @Test
    fun `ao chamar reset deve restaurar o valor padrao`() {
        preference.set("outroValor")
        assertEquals("outroValor", preference.value)

        preference.reset()
        assertEquals("default", preference.value)
    }

    @Test
    fun `isDefault deve retornar true se o valor for padrao`() {
        assertTrue(preference.isDefault())

        preference.set("modificado")
        assertFalse(preference.isDefault())

        preference.reset()
        assertTrue(preference.isDefault())
    }
}
