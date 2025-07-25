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

package dev.gmarques.controledenotificacoes.data.local.room.mapper

import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import org.junit.Assert.assertEquals
import org.junit.Test

class ManagedAppMapperTest {

    @Test
    fun `deve mapear ManagedApp para ManagedAppEntity corretamente quando os dados forem validos`() {
        val model = ManagedApp(packageName = "com.exemplo.app", ruleId = "regra1", hasPendingNotifications = false)
        val entity = ManagedAppMapper.mapToEntity(model)
        assertEquals(model.packageName, entity.packageName)
        assertEquals(model.ruleId, entity.ruleId)
    }

    @Test
    fun `deve manter ruleId vazio ao mapear ManagedApp para ManagedAppEntity`() {
        val model = ManagedApp(packageName = "com.exemplo.app", ruleId = "", hasPendingNotifications = false)
        val entity = ManagedAppMapper.mapToEntity(model)
        assertEquals("", entity.ruleId)
    }

    @Test
    fun `deve manter packageName vazio ao mapear ManagedApp para ManagedAppEntity`() {
        val model = ManagedApp(packageName = "", ruleId = "regra1", hasPendingNotifications = false)
        val entity = ManagedAppMapper.mapToEntity(model)
        assertEquals("", entity.packageName)
    }

    @Test
    fun `deve mapear ManagedAppEntity para ManagedApp corretamente quando os dados forem validos`() {
        val entity = ManagedAppEntity(packageName = "com.exemplo.app", ruleId = "regra1", hasPendingNotifications = false)
        val model = ManagedAppMapper.mapToModel(entity)
        assertEquals(entity.packageName, model.packageName)
        assertEquals(entity.ruleId, model.ruleId)
    }

    @Test
    fun `deve manter ruleId vazio ao mapear ManagedAppEntity para ManagedApp`() {
        val entity = ManagedAppEntity(packageName = "com.exemplo.app", ruleId = "", hasPendingNotifications = false)
        val model = ManagedAppMapper.mapToModel(entity)
        assertEquals("", model.ruleId)
    }

    @Test
    fun `deve manter packageName vazio ao mapear ManagedAppEntity para ManagedApp`() {
        val entity = ManagedAppEntity(packageName = "", ruleId = "regra1", hasPendingNotifications = false)
        val model = ManagedAppMapper.mapToModel(entity)
        assertEquals("", model.packageName)
    }

    @Test
    fun `deve manter integridade dos dados ao mapear ManagedApp para ManagedAppEntity e de volta para ManagedApp`() {
        val original = ManagedApp(packageName = "com.exemplo.app", ruleId = "regra1", hasPendingNotifications = false)
        val entity = ManagedAppMapper.mapToEntity(original)
        val result = ManagedAppMapper.mapToModel(entity)
        assertEquals(original, result)
    }

    @Test
    fun `deve manter integridade dos dados ao mapear ManagedAppEntity para ManagedApp e de volta para ManagedAppEntity`() {
        val original = ManagedAppEntity(packageName = "com.exemplo.app", ruleId = "regra1", hasPendingNotifications = false)
        val model = ManagedAppMapper.mapToModel(original)
        val result = ManagedAppMapper.mapToEntity(model)
        assertEquals(original, result)
    }
}
