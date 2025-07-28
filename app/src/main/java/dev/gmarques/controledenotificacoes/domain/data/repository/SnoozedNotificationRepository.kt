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

package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import kotlinx.coroutines.flow.Flow

/**
 * Criado por Gilian Marques
 * Em 26/07/2025 as 18:01
 */
interface SnoozedNotificationRepository {
    suspend fun insert(notification: SnoozedNotification)
    suspend fun deleteAll(packageName: String)

    suspend fun delete(key: String)
    suspend fun getByKey(key: String): SnoozedNotification?
    suspend fun getByPackageName(packageName: String): List<SnoozedNotification>
    suspend fun getAll(): List<SnoozedNotification>
    fun observeNotificationsByPkgId(pkg: String): Flow<List<SnoozedNotification>>
}