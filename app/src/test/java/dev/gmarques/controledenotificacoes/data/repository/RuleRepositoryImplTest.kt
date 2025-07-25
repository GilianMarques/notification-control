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

import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.mapper.RuleMapper
import dev.gmarques.controledenotificacoes.data.repository.RuleRepositoryImpl
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.fail
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class RuleRepositoryImplTest {

    @Mock
    private lateinit var ruleDao: RuleDao
    private lateinit var repository: RuleRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = RuleRepositoryImpl(ruleDao)
    }

    @Test
    fun `addRule deve chamar insertRule no dao`() = runBlocking {
        val rule = Rule(
            "1",
            "Teste",
            listOf(Rule.WeekDay.MONDAY),
            listOf(
                TimeRange(
                    8,
                    0,
                    12,
                    0
                )
            ),
            null,
            type = Rule.typeDefault,
            keepFullHistory = Rule.keepFullHistoryDefault,
            action = Rule.actionDefault
        )

        repository.addRuleOrThrow(rule)

        verify(ruleDao).insertRule(RuleMapper.mapToEntity(rule))
    }

    @Test
    fun `addRuleOrThrow deve lancar excecao ao passar um regra invalida`() = runBlocking {
        val rules = listOf(
            Rule(
                "1",
                "Teste1",
                listOf(),
                listOf(
                    TimeRange(
                        8,
                        0,
                        12,
                        0
                    )
                ),
                null,
                type = Rule.typeDefault,
                keepFullHistory = Rule.keepFullHistoryDefault,
                action = Rule.actionDefault
            ),
            Rule(
                "1",
                "Teste2",
                listOf(Rule.WeekDay.TUESDAY),
                listOf(
                    TimeRange(
                        8,
                        0,
                        8,
                        0
                    )
                ),
                null,
                type = Rule.typeDefault,
                keepFullHistory = Rule.keepFullHistoryDefault,
                action = Rule.actionDefault
            ),
            Rule(
                "",
                "Teste3",
                listOf(Rule.WeekDay.TUESDAY),
                listOf(
                    TimeRange(
                        8,
                        0,
                        9,
                        0
                    )
                ),
                null,
                type = Rule.typeDefault,
                keepFullHistory = Rule.keepFullHistoryDefault,
                action = Rule.actionDefault
            ),
        )

        rules.forEach { rule ->
            try {
                repository.addRuleOrThrow(rule)
                fail { "Objeto invalido entrou no DB. Chamada deveria ter lançado uma exceção. ${rule.name}" }
            } catch (_: Exception) {
                true
            }
        }

    }

    @Test
    fun `updateRuleOrThrow deve lancar excecao ao passar um regra invalida`() = runBlocking {
        val rules = listOf(
            Rule(
                "1",
                "Teste1",
                listOf(),
                listOf(
                    TimeRange(
                        8,
                        0,
                        12,
                        0
                    )
                ),
                null,
                type = Rule.typeDefault,
                keepFullHistory = Rule.keepFullHistoryDefault,
                action = Rule.actionDefault
            ),
            Rule(
                "1",
                "Teste2",
                listOf(Rule.WeekDay.TUESDAY),
                listOf(
                    TimeRange(
                        8,
                        0,
                        8,
                        0
                    )
                ),
                null,
                type = Rule.typeDefault,
                keepFullHistory = Rule.keepFullHistoryDefault,
                action = Rule.actionDefault
            ),
            Rule(
                "",
                "Teste3",
                listOf(Rule.WeekDay.TUESDAY),
                listOf(
                    TimeRange(
                        8,
                        0,
                        9,
                        0
                    )
                ),
                null,
                type = Rule.typeDefault,
                keepFullHistory = Rule.keepFullHistoryDefault,
                action = Rule.actionDefault
            ),
        )

        rules.forEach { rule ->
            try {
                repository.updateRuleOrThrow(rule)
                fail { "Objeto invalido entrou no DB. Chamada deveria ter lançado uma exceção. ${rule.name}" }
            } catch (_: Exception) {
                true
            }
        }

    }

    @Test
    fun `updateRule deve chamar updateRule no dao`() = runBlocking {
        val rule = Rule(
            "1",
            "Teste",
            listOf(Rule.WeekDay.MONDAY),
            listOf(
                TimeRange(
                    8,
                    0,
                    12,
                    0
                )
            ),
            null,
            type = Rule.typeDefault,
            keepFullHistory = Rule.keepFullHistoryDefault,
            action = Rule.actionDefault
        )

        repository.updateRuleOrThrow(rule)

        verify(ruleDao).updateRule(RuleMapper.mapToEntity(rule))
    }

    @Test
    fun `removeRule deve chamar deleteRule no dao`() = runBlocking {
        val rule = Rule(
            "1",
            "Teste",
            listOf(Rule.WeekDay.MONDAY),
            listOf(
                TimeRange(
                    8,
                    0,
                    12,
                    0
                )
            ),
            null,
            type = Rule.typeDefault,
            keepFullHistory = Rule.keepFullHistoryDefault,
            action = Rule.actionDefault
        )

        repository.deleteRule(rule)

        verify(ruleDao).deleteRule(RuleMapper.mapToEntity(rule))
    }

    @Test
    fun `getRuleById deve retornar regra convertida`() = runBlocking {
        val ruleId = "1"
        val ruleEntity =
            RuleMapper.mapToEntity(
                Rule(
                    ruleId,
                    "Teste",
                    listOf(Rule.WeekDay.MONDAY),
                    listOf(
                        TimeRange(
                            8,
                            0,
                            12,
                            0
                        )
                    ),
                    null,
                    type = Rule.typeDefault,
                    keepFullHistory = Rule.keepFullHistoryDefault,
                    action = Rule.actionDefault
                )
            )
        `when`(ruleDao.getRuleById(ruleId)).thenReturn(ruleEntity)

        val result = repository.getRuleById(ruleId)

        assert(result != null)
        assert(result!!.id == ruleId)
    }

    @Test
    fun `getAllRules deve retornar lista de regras convertidas`() = runBlocking {
        val ruleEntityList = listOf(
            RuleMapper.mapToEntity(
                Rule(
                    "1",
                    "Teste 1",
                    listOf(Rule.WeekDay.MONDAY),
                    listOf(
                        TimeRange(
                            8,
                            0,
                            12,
                            0
                        )
                    ),
                    null,
                    type = Rule.typeDefault,
                    keepFullHistory = Rule.keepFullHistoryDefault,
                    action = Rule.actionDefault
                )
            ),
            RuleMapper.mapToEntity(
                Rule(
                    "2",
                    "Teste 2",
                    listOf(Rule.WeekDay.TUESDAY),
                    listOf(
                        TimeRange(
                            10,
                            0,
                            14,
                            0
                        )
                    ),
                    null,
                    type = Rule.typeDefault,
                    keepFullHistory = Rule.keepFullHistoryDefault,
                    action = Rule.actionDefault
                )
            )
        )
        `when`(ruleDao.getAllRules()).thenReturn(ruleEntityList)

        val result = repository.getAllRules()

        assert(result.size == ruleEntityList.size)
    }
}