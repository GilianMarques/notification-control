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

package dev.gmarques.controledenotificacoes.domain.model.validators

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ManagedAppValidatorTest {

    @Test
    fun `ao validar packageName valido deve retornar sucesso`() {
        val result = ManagedAppValidator.validatePackageId("com.exemplo.app")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `ao validar packageName vazio deve retornar falha com BlankStringException`() {
        val result = ManagedAppValidator.validatePackageId("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ManagedAppValidator.ManagedAppValidatorException.BlankPackageIdException)
    }

    @Test
    fun `ao validar ruleId valido deve retornar sucesso`() {
        val result = ManagedAppValidator.validateRuleId("regra123")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `ao validar ruleId vazio deve retornar falha com BlankStringException`() {
        val result = ManagedAppValidator.validateRuleId("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ManagedAppValidator.ManagedAppValidatorException.BlankRuleIdException)
    }

    @Test
    fun `ao validar managedApp com dados validos deve passar sem excecao`() {
        val managedApp = ManagedApp(packageName = "com.exemplo.app", ruleId = "regra123", hasPendingNotifications = false)
        try {
            ManagedAppValidator.validate(managedApp)
        } catch (e: Exception) {
            fail("Não era esperada nenhuma exceção, mas foi lançada: ${e.message}")
        }
    }

    @Test
    fun `ao validar managedApp com packageName invalido deve lancar BlankStringException`() {
        val managedApp = ManagedApp(packageName = "", ruleId = "regra123", hasPendingNotifications = false)
        try {
            ManagedAppValidator.validate(managedApp)
            fail("Era esperada uma BlankStringException")
        } catch (e: Exception) {
            assertTrue(e is ManagedAppValidator.ManagedAppValidatorException.BlankPackageIdException)
        }
    }

    @Test
    fun `ao validar managedApp com ruleId invalido deve lancar BlankStringException`() {
        val managedApp = ManagedApp(packageName = "com.exemplo.app", ruleId = "", hasPendingNotifications = false)
        try {
            ManagedAppValidator.validate(managedApp)
            fail("Era esperada uma BlankStringException")
        } catch (e: Exception) {
            assertTrue(e is ManagedAppValidator.ManagedAppValidatorException.BlankRuleIdException)
        }
    }
}
