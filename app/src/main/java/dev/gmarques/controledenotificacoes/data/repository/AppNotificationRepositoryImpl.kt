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

package dev.gmarques.controledenotificacoes.data.repository

import dev.gmarques.controledenotificacoes.data.local.room.dao.AppNotificationDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.AppNotificationMapper
import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppNotificationRepositoryImpl @Inject constructor(
    private val dao: AppNotificationDao,
) : AppNotificationRepository {

    override suspend fun insert(notification: AppNotification) {
        AppNotificationMapper.toEntity(notification).let { dao.insert(it) }
    }

    override suspend fun deleteAll(packageName: String) {
        dao.deleteAll(packageName)
    }

    override suspend fun getByPkg(pkg: String): AppNotification? {
        return dao.getByPkg(pkg)?.let { AppNotificationMapper.toModel(it) }
    }

    override suspend fun getAll(): List<AppNotification> {
        return dao.getAll().map { AppNotificationMapper.toModel(it) }
    }

    override fun observeNotificationsByPkgId(pkg: String): Flow<List<AppNotification>> {
        return dao.observeNotificationsByPkgId(pkg).map { list ->
            list.map { AppNotificationMapper.toModel(it) }
        }
    }
}