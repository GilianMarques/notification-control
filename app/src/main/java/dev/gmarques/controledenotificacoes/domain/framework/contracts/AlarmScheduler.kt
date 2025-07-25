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

package dev.gmarques.controledenotificacoes.domain.framework.contracts

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:00.
 *
 * gerencia o agendamento e cancelamento de alarmes no sistema, é usada para agendar as notificações de relatório
 */
interface AlarmScheduler {

    /**
     * Agenda uma alarme para disparar em determinado horario e
     * escreve o dados do agendamento nas preferencias.
     */
    fun scheduleAlarm(packageName: String, millis: Long)

    fun scheduleAutoTurnOnAlarm(millis: Long)

    /**
     * Cancela o agendamento de um alarme alarme  e
     * remove o dados do agendamento das preferencias.
     */
    fun cancelAlarm(packageName: String)

    /**
     * Remove das preferências do pacote referente ao alarme.
     * Use essa função para remover o registro do agendamento depois que o alarme for disparado ou cancelado.
     */
    fun deleteScheduleData(packageName: String)

    /**
     * Verifica se existe algum alarme agendado para o aplicativo especificado.
     */
    fun isThereAnyAlarmSetForPackage(packageName: String): Boolean

    /**
     * Retorna todos os agendamentos ativos no momento
     */
    fun getAllSchedules(): List<String>
}