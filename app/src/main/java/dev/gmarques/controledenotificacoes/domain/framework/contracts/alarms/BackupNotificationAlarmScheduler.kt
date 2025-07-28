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

package dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms

/**
 * Criado por Gilian Marques
 * Em 28/07/2025 as 12:14
 *
 * Gerencia o agendamento e cancelamento de alarmes no sistema usados como backup para as notificações adiadas.
 *
 * Sempre que uma notificação é adiada uma copia dela é feita no DB assim como um agendamento, caso a notificação
 * não seja emitida na data correta pelo sistema, este app emite uma notificação 'backup' com o mesmo conteudo da original
 * para que o usuario nao seja prejudicado.
 *
 * Se a notificação for emitida corretamente, o registro do DB é removido e o agendamento é cancelado.
 */
interface BackupNotificationAlarmScheduler {

    /**
     * Agenda uma alarme para disparar em determinado horario e
     * escreve o dados do agendamento nas preferencias.
     */
    fun scheduleAlarm(key: String, snoozedUntil: Long)

    /**
     * Cancela o agendamento de um alarme alarme  e
     * remove o dados do agendamento das preferencias.
     */
    fun cancelAlarm(key: String)

    /**
     * Remove das preferências a chave referente ao alarme.
     */
    fun deleteScheduleData(key: String)

    /**
     * Verifica se existe algum alarme agendado para a notificação especificada.
     */
    fun isThereAnyAlarmSetForKey(key: String): Boolean

    /**
     * Retorna todos os agendamentos ativos no momento
     */
    fun getAllSchedules(): List<String>
}
