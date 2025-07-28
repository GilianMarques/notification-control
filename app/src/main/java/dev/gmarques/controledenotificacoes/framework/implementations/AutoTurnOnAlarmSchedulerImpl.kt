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

package dev.gmarques.controledenotificacoes.framework.implementations

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.AutoTurnOnAlarmScheduler
import dev.gmarques.controledenotificacoes.framework.AutoTurnOnReceiver
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:07.
 *
 * Gerencia o agendamento e cancelamento de alarmes no sistema usados para emitir notificações
 */
class AutoTurnOnAlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AutoTurnOnAlarmScheduler {

    private val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    /**
     * Agenda o alarme responsavel por ligar o serviço de escuta de notificações de tempos em tempos
     */
    override fun scheduleAutoTurnOnAlarm(millis: Long) {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, createAutoTurnOnPendingIntent())
    }

    /**
     * Cria um [PendingIntent] para ser usado com o [AlarmManager] para ligar o serviço de escuta
     * de notificações.
     * Este [PendingIntent] será acionado quando o alarme disparar, enviando um broadcast para o [AutoTurnOnReceiver].
     *
     * @return Um [PendingIntent] configurado para enviar um broadcast.
     */
    private fun createAutoTurnOnPendingIntent(): PendingIntent {
        val intent = Intent(context, AutoTurnOnReceiver::class.java)

        return PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
