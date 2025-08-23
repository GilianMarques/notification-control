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

package dev.gmarques.controledenotificacoes.domain.implementations

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor.PerformAction
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.usecase.rules.IsRuleInBlockPeriodUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.joda.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class IncomingNotificationProcessorImplTest {

    private lateinit var processor: IncomingNotificationProcessorImpl
    private lateinit var isRuleInBlockPeriodUseCase: IsRuleInBlockPeriodUseCase

    private val baseDate = LocalDateTime.now()

    // Notificação que contém a palavra "teste" no título
    private val appNotificationWithKeyword = AppNotification(
        packageName = "com.example.test",
        title = "Este é um teste",
        content = "Conteúdo da notificação",
        postTime = System.currentTimeMillis()
    )

    // Notificação que NÃO contém a palavra "teste"
    private val appNotificationWithoutKeyword = AppNotification(
        packageName = "com.example.test",
        title = "Título normal",
        content = "Conteúdo normal",
        postTime = System.currentTimeMillis()
    )

    private val managedApp = ManagedApp(
        packageName = "com.example.test",
        ruleId = "rule-123",
        hasPendingNotifications = false
    )

    @Before
    fun setup() {
        isRuleInBlockPeriodUseCase = mockk()
        processor = IncomingNotificationProcessorImpl(isRuleInBlockPeriodUseCase)

        // Mock do AppLog do Android
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
    }

    // ===== TESTES PARA REGRAS SEM CONDIÇÕES =====

    @Test
    fun processNotification_deve_permitir_regra_restritiva_sem_condicao_fora_do_periodo_de_bloqueio() {
        val rule = createRestrictiveRule(condition = null, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultado)
    }

    @Test
    fun processNotification_deve_adiar_regra_restritiva_sem_condicao_dentro_do_periodo_de_bloqueio_com_acao_snooze() {
        val rule = createRestrictiveRule(condition = null, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultado)
    }

    @Test
    fun processNotification_deve_cancelar_regra_restritiva_sem_condicao_dentro_do_periodo_de_bloqueio_com_acao_cancel() {
        val rule = createRestrictiveRule(condition = null, action = Rule.Action.CANCEL)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Cancel, resultado)
    }

    @Test
    fun processNotification_deve_permitir_regra_permissiva_sem_condicao_fora_do_periodo_de_bloqueio() {
        val rule = createPermissiveRule(condition = null, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultado)
    }

    @Test
    fun processNotification_deve_adiar_regra_permissiva_sem_condicao_dentro_do_periodo_de_bloqueio_com_acao_snooze() {
        val rule = createPermissiveRule(condition = null, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultado)
    }

    @Test
    fun processNotification_deve_cancelar_regra_permissiva_sem_condicao_dentro_do_periodo_de_bloqueio_com_acao_cancel() {
        val rule = createPermissiveRule(condition = null, action = Rule.Action.CANCEL)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Cancel, resultado)
    }

    // ===== TESTES PARA REGRAS RESTRITIVAS COM CONDIÇÕES =====

    @Test
    fun processNotification_deve_permitir_regra_restritiva_only_if_dentro_do_periodo_quando_condicao_nao_satisfeita() {
        val condition = createConditionOnlyIf()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        // Usa notificação SEM palavra-chave, condição NÃO satisfeita
        val resultado = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultado)
    }

    @Test
    fun processNotification_deve_adiar_regra_restritiva_only_if_dentro_do_periodo_quando_condicao_satisfeita_com_acao_snooze() {
        val condition = createConditionOnlyIf()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        // Usa notificação COM palavra-chave, condição satisfeita
        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultado)
    }

    @Test
    fun processNotification_deve_cancelar_regra_restritiva_only_if_dentro_do_periodo_quando_condicao_satisfeita_com_acao_cancel() {
        val condition = createConditionOnlyIf()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.CANCEL)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        // Usa notificação COM palavra-chave, condição satisfeita
        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Cancel, resultado)
    }

    @Test
    fun processNotification_deve_adiar_regra_restritiva_except_dentro_do_periodo_quando_condicao_nao_satisfeita_com_acao_snooze() {
        val condition = createConditionExcept()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        // Usa notificação SEM palavra-chave, condição NÃO satisfeita
        val resultado = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultado)
    }

    @Test
    fun processNotification_deve_cancelar_regra_restritiva_except_dentro_do_periodo_quando_condicao_nao_satisfeita_com_acao_cancel() {
        val condition = createConditionExcept()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.CANCEL)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        // Usa notificação SEM palavra-chave, condição NÃO satisfeita
        val resultado = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Cancel, resultado)
    }

    @Test
    fun processNotification_deve_permitir_regra_restritiva_except_dentro_do_periodo_quando_condicao_satisfeita() {
        val condition = createConditionExcept()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        // Usa notificação COM palavra-chave, condição satisfeita
        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultado)
    }

    @Test
    fun processNotification_deve_permitir_regra_restritiva_com_condicao_fora_do_periodo_de_bloqueio() {
        val condition = createConditionOnlyIf()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultado)
    }

    // ===== TESTES PARA REGRAS PERMISSIVAS COM CONDIÇÕES =====

    @Test
    fun processNotification_deve_permitir_regra_permissiva_only_if_fora_do_periodo_quando_condicao_satisfeita() {
        val condition = createConditionOnlyIf()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        // Usa notificação COM palavra-chave, condição satisfeita
        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultado)
    }

    @Test
    fun processNotification_deve_adiar_regra_permissiva_only_if_fora_do_periodo_quando_condicao_nao_satisfeita_com_acao_snooze() {
        val condition = createConditionOnlyIf()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        // Usa notificação SEM palavra-chave, condição NÃO satisfeita
        val resultado = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultado)
    }

    @Test
    fun processNotification_deve_cancelar_regra_permissiva_only_if_fora_do_periodo_quando_condicao_nao_satisfeita_com_acao_cancel() {
        val condition = createConditionOnlyIf()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.CANCEL)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        // Usa notificação SEM palavra-chave, condição NÃO satisfeita
        val resultado = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Cancel, resultado)
    }

    @Test
    fun processNotification_deve_permitir_regra_permissiva_except_fora_do_periodo_quando_condicao_nao_satisfeita() {
        val condition = createConditionExcept()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        // Usa notificação SEM palavra-chave, condição NÃO satisfeita
        val resultado = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultado)
    }

    @Test
    fun processNotification_deve_adiar_regra_permissiva_except_fora_do_periodo_quando_condicao_satisfeita_com_acao_snooze() {
        val condition = createConditionExcept()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        // Usa notificação COM palavra-chave, condição satisfeita
        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultado)
    }

    @Test
    fun processNotification_deve_cancelar_regra_permissiva_except_fora_do_periodo_quando_condicao_satisfeita_com_acao_cancel() {
        val condition = createConditionExcept()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.CANCEL)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        // Usa notificação COM palavra-chave, condição satisfeita
        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Cancel, resultado)
    }

    @Test
    fun processNotification_deve_permitir_regra_permissiva_com_condicao_dentro_do_periodo_de_bloqueio() {
        val condition = createConditionOnlyIf()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)
        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        val resultado = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultado)
    }

    // ===== TESTES PARA CASOS EXTREMOS =====

    @Test
    fun processNotification_deve_permitir_regra_restritiva_com_condicao_only_if_fora_do_periodo_independente_da_satisfacao() {
        val condition = createConditionOnlyIf()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.SNOOZE)

        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        val resultadoComKeyword = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)
        val resultadoSemKeyword = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultadoComKeyword)
        assertEquals(PerformAction.Allow, resultadoSemKeyword)
    }

    @Test
    fun processNotification_deve_permitir_regra_restritiva_com_condicao_except_fora_do_periodo_independente_da_satisfacao() {
        val condition = createConditionExcept()
        val rule = createRestrictiveRule(condition = condition, action = Rule.Action.CANCEL)

        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns false

        val resultadoComKeyword = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)
        val resultadoSemKeyword = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultadoComKeyword)
        assertEquals(PerformAction.Allow, resultadoSemKeyword)
    }

    @Test
    fun processNotification_deve_permitir_regra_permissiva_com_condicao_only_if_dentro_do_periodo_independente_da_satisfacao() {
        val condition = createConditionOnlyIf()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)

        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        val resultadoComKeyword = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)
        val resultadoSemKeyword = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultadoComKeyword)
        assertEquals(PerformAction.Allow, resultadoSemKeyword)
    }

    @Test
    fun processNotification_deve_permitir_regra_permissiva_com_condicao_except_dentro_do_periodo_independente_da_satisfacao() {
        val condition = createConditionExcept()
        val rule = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)

        every { isRuleInBlockPeriodUseCase(rule, baseDate) } returns true

        val resultadoComKeyword = processor.processNotification(appNotificationWithKeyword, rule, managedApp, baseDate)
        val resultadoSemKeyword = processor.processNotification(appNotificationWithoutKeyword, rule, managedApp, baseDate)

        assertEquals(PerformAction.Allow, resultadoComKeyword)
        assertEquals(PerformAction.Allow, resultadoSemKeyword)
    }

    // ===== TESTES ADICIONAIS PARA COBERTURA COMPLETA =====

    @Test
    fun processNotification_deve_bloquear_regra_restritiva_only_if_dentro_periodo_com_diferentes_acoes() {
        val condition = createConditionOnlyIf()
        val ruleSnooze = createRestrictiveRule(condition = condition, action = Rule.Action.SNOOZE)
        val ruleCancel = createRestrictiveRule(condition = condition, action = Rule.Action.CANCEL)

        every { isRuleInBlockPeriodUseCase(ruleSnooze, baseDate) } returns true
        every { isRuleInBlockPeriodUseCase(ruleCancel, baseDate) } returns true

        val resultadoSnooze = processor.processNotification(appNotificationWithKeyword, ruleSnooze, managedApp, baseDate)
        val resultadoCancel = processor.processNotification(appNotificationWithKeyword, ruleCancel, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultadoSnooze)
        assertEquals(PerformAction.Cancel, resultadoCancel)
    }

    @Test
    fun processNotification_deve_bloquear_regra_restritiva_except_dentro_periodo_com_diferentes_acoes() {
        val condition = createConditionExcept()
        val ruleSnooze = createRestrictiveRule(condition = condition, action = Rule.Action.SNOOZE)
        val ruleCancel = createRestrictiveRule(condition = condition, action = Rule.Action.CANCEL)

        every { isRuleInBlockPeriodUseCase(ruleSnooze, baseDate) } returns true
        every { isRuleInBlockPeriodUseCase(ruleCancel, baseDate) } returns true

        val resultadoSnooze = processor.processNotification(appNotificationWithoutKeyword, ruleSnooze, managedApp, baseDate)
        val resultadoCancel = processor.processNotification(appNotificationWithoutKeyword, ruleCancel, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultadoSnooze)
        assertEquals(PerformAction.Cancel, resultadoCancel)
    }

    @Test
    fun processNotification_deve_bloquear_regra_permissiva_only_if_fora_periodo_com_diferentes_acoes() {
        val condition = createConditionOnlyIf()
        val ruleSnooze = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)
        val ruleCancel = createPermissiveRule(condition = condition, action = Rule.Action.CANCEL)

        every { isRuleInBlockPeriodUseCase(ruleSnooze, baseDate) } returns false
        every { isRuleInBlockPeriodUseCase(ruleCancel, baseDate) } returns false

        val resultadoSnooze = processor.processNotification(appNotificationWithoutKeyword, ruleSnooze, managedApp, baseDate)
        val resultadoCancel = processor.processNotification(appNotificationWithoutKeyword, ruleCancel, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultadoSnooze)
        assertEquals(PerformAction.Cancel, resultadoCancel)
    }

    @Test
    fun processNotification_deve_bloquear_regra_permissiva_except_fora_periodo_com_diferentes_acoes() {
        val condition = createConditionExcept()
        val ruleSnooze = createPermissiveRule(condition = condition, action = Rule.Action.SNOOZE)
        val ruleCancel = createPermissiveRule(condition = condition, action = Rule.Action.CANCEL)

        every { isRuleInBlockPeriodUseCase(ruleSnooze, baseDate) } returns false
        every { isRuleInBlockPeriodUseCase(ruleCancel, baseDate) } returns false

        val resultadoSnooze = processor.processNotification(appNotificationWithKeyword, ruleSnooze, managedApp, baseDate)
        val resultadoCancel = processor.processNotification(appNotificationWithKeyword, ruleCancel, managedApp, baseDate)

        assertEquals(PerformAction.Snooze, resultadoSnooze)
        assertEquals(PerformAction.Cancel, resultadoCancel)
    }

    // ===== MÉTODOS AUXILIARES =====

    private fun createRestrictiveRule(condition: Condition?, action: Rule.Action): Rule {
        return Rule(
            id = "test-rule-restrictive",
            name = "Test Restrictive Rule",
            days = listOf(Rule.WeekDay.MONDAY, Rule.WeekDay.TUESDAY),
            timeRanges = listOf(TimeRange(startHour = 9, startMinute = 0, endHour = 17, endMinute = 0)),
            condition = condition,
            keepFullHistory = false,
            type = Rule.Type.RESTRICTIVE,
            action = action
        )
    }

    private fun createPermissiveRule(condition: Condition?, action: Rule.Action): Rule {
        return Rule(
            id = "test-rule-permissive",
            name = "Test Permissive Rule",
            days = listOf(Rule.WeekDay.MONDAY, Rule.WeekDay.TUESDAY),
            timeRanges = listOf(TimeRange(startHour = 9, startMinute = 0, endHour = 17, endMinute = 0)),
            condition = condition,
            keepFullHistory = false,
            type = Rule.Type.PERMISSIVE,
            action = action
        )
    }

    private fun createConditionOnlyIf(): Condition {
        // Condição que procura pela palavra "teste" no título
        return Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("teste"),
            caseSensitive = false
        )
    }

    private fun createConditionExcept(): Condition {
        // Condição que procura pela palavra "teste" no título
        return Condition(
            type = Condition.Type.EXCEPT,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("teste"),
            caseSensitive = false
        )
    }
}