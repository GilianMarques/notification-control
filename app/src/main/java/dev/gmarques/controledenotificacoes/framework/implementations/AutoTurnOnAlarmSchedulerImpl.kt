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
