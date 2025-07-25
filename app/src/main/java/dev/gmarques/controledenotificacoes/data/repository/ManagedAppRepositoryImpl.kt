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

import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.ManagedAppMapper
import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.ManagedAppValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:57.
 */
class ManagedAppRepositoryImpl @Inject constructor(private val managedAppDao: ManagedAppDao) : ManagedAppRepository {

    /**
     * Adiciona ou atualiza um aplicativo gerenciado no repositório.
     * Se o objeto já existir no DB ele será atualizado.
     * Lança exceção se o objeto falhar na validação, impedidno que um objeto invalido seja salvo.
     *
     * @param managedApp O aplicativo gerenciado a ser adicionado ou atualizado.
     */
    override suspend fun addOrUpdateManagedAppOrThrow(managedApp: ManagedApp) {
        ManagedAppValidator.validate(managedApp)
        managedAppDao.insertOrUpdateManagedApp(ManagedAppMapper.mapToEntity(managedApp))
    }

    override suspend fun updateManagedAppOrThrow(managedApp: ManagedApp) {
        ManagedAppValidator.validate(managedApp)
        managedAppDao.updateManagedApp(ManagedAppMapper.mapToEntity(managedApp))
    }

    override suspend fun deleteManagedAppByPackageId(packageName: String) {
        managedAppDao.deleteById(packageName)
    }

    override suspend fun getManagedAppByPackageId(id: String): ManagedApp? {
        return managedAppDao.getManagedAppByPackageId(id)?.let {
            ManagedAppMapper.mapToModel(it)
        }
    }

    override suspend fun getManagedAppsByRuleId(ruleId: String): List<ManagedApp?> {
        return managedAppDao.getManagedAppsByRuleId(ruleId).let { managedAppEntity ->
            managedAppEntity.map { it?.let { ManagedAppMapper.mapToModel(it) } }
        }
    }

    override suspend fun deleteManagedAppsByRuleId(ruleId: String): Int {
        return managedAppDao.deleteManagedAppsByRuleId(ruleId)
    }

    override fun observeAllManagedApps(): Flow<List<ManagedApp>> {
        return managedAppDao.observeAllManagedApps().map { apps ->
            apps.map { ManagedAppMapper.mapToModel(it) }
        }
    }

    override fun observeManagedApp(pkg: String): Flow<ManagedApp?> {
        return managedAppDao.observeManagedApp(pkg).map { it?.let { ManagedAppMapper.mapToModel(it) } }
    }
}
