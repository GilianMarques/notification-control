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

import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.RuleMapper
import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
class RuleRepositoryImpl @Inject constructor(private val ruleDao: RuleDao) : RuleRepository {

    override suspend fun addRuleOrThrow(rule: Rule) {
        RuleValidator.validate(rule)
        ruleDao.insertRule(RuleMapper.mapToEntity(rule))
    }

    override suspend fun updateRuleOrThrow(rule: Rule) {
        RuleValidator.validate(rule)
        ruleDao.updateRule(RuleMapper.mapToEntity(rule))
    }

    override suspend fun deleteRule(rule: Rule) {
        ruleDao.deleteRule(RuleMapper.mapToEntity(rule))
    }

    override suspend fun getRuleById(id: String): Rule? {
        return ruleDao.getRuleById(id)?.let {
            RuleMapper.mapToModel(it)
        }
    }

    override suspend fun getAllRules(): List<Rule> {

        return ruleDao.getAllRules().map {
            RuleMapper.mapToModel(it)
        }
    }

    override fun observeAllRules(): Flow<List<Rule>> {

        return ruleDao.observeAllRules().map { entities ->
            entities.map {
                RuleMapper.mapToModel(it)
            }
        }
    }

    override fun observeRule(id: String): Flow<Rule?> {
        return ruleDao.observeRule(id).map {
            if (it == null) null
            else RuleMapper.mapToModel(it)
        }
    }
}
