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
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.BackupNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.framework.backup_notification.BackupNotificationAlarmReceiver
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 28/07/2025 as 12:19
 *
 * Veja [BackupNotificationAlarmScheduler] para mais detalhes.
 */
class BackupNotificationAlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : BackupNotificationAlarmScheduler {

    private val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    /**
     * A notificação de backup será exibida nesse no horário marcado para a notificação adiada ser exibida pelo sistema + o
     * valor definido nessa variável. Isso serve pra dar tempo do sistema emitir a notificação e o app detectar e cancelar o agendamento
     * da notificação de backup.
     */
    private val defaultBackupNotificationDelay = 60_000L

    /**
     * Agenda um alarme para uma notificação específica em um determinado horário.
     * Se um agendamento ja existir, será cancelado e um novo será criado,
     * garantindo que apenas um alarme seja agendado para cada notificação.
     *
     * @param key A chave unica da notificação  para o qual o alarme será agendado.
     * @param snoozedUntil O horário em milissegundos em que a notificação foi adiada, esta função adicionará um delay a esse horario.
     *
     */
    override fun scheduleAlarm(key: String, snoozedUntil: Long) {

        if (LocalDateTime.now().isBefore(LocalDateTime(snoozedUntil))) error("Alarm cannot be scheduled in the past")

        cancelAlarm(key) // avoid multiple schedules for the same notification

        val pIntent = createPendingIntent(key)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozedUntil + defaultBackupNotificationDelay, pIntent)

        saveScheduleData(key)
    }

    /**
     * Cancela um alarme agendado para um pacote específico.
     *
     * @param key A ID da notificação para o qual o alarme será cancelado.
     */
    override fun cancelAlarm(key: String) {

        val pIntent = createPendingIntent(key)

        alarmManager.cancel(pIntent)

        deleteScheduleData(key)
    }

    /**
     * Verifica se existe algum alarme agendado para um pacote específico.
     * Lê a preferência que armazena a lista de pacotes com alarmes agendados e verifica se o `packageName` está presente.
     *
     * @param key A ID da notificação a ser verificado.
     * @return `true` se houver um alarme agendado para o pacote, `false` caso contrário.
     */
    override fun isThereAnyAlarmSetForKey(key: String): Boolean {

        val json = PreferencesImpl.scheduledSnoozedNotificationAlarms.value
        return (MoshiListConverter.fromJson(json) ?: mutableListOf()).contains(key)
    }

    /**
     * Recupera a lista de todos os pacotes que possuem alarmes agendados.
     * Lê a preferência que armazena a lista de pacotes com alarmes agendados e a retorna.
     *
     * @return Uma lista de strings contendo os IDs dos pacotes com alarmes agendados. Retorna uma lista vazia se nenhum alarme
     * estiver agendado ou se a preferência não existir.
     */
    override fun getAllSchedules(): List<String> {
        val json = PreferencesImpl.scheduledSnoozedNotificationAlarms.value
        return MoshiListConverter.fromJson(json) ?: mutableListOf()
    }

    /**
     * Cria um [PendingIntent] para ser usado com o [AlarmManager].
     * Este [PendingIntent] será acionado quando o alarme disparar, enviando um broadcast para o [BackupNotificationAlarmReceiver].
     *
     * @param key A ID da notificação a ser incluído como extra no [Intent] do [PendingIntent].
     * @return Um [PendingIntent] configurado para enviar um broadcast.
     */
    private fun createPendingIntent(key: String): PendingIntent {
        val intent = Intent(context, BackupNotificationAlarmReceiver::class.java).apply {
            putExtra(BackupNotificationAlarmReceiver.NOTIFICATION_KEY, key)
        }

        return PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Salva a chave  indicando que um alarme foi agendado para a notificação especificada.
     * Garante que as chaves na lista não se repitam.
     * Utiliza o [SavePreferenceUseCase] para persistir essa informação.
     *
     * @param key A ID da notificação para o qual o dado de agendamento será salvo.
     */
    private fun saveScheduleData(key: String) {

        val json = PreferencesImpl.scheduledSnoozedNotificationAlarms.value
        val list = (MoshiListConverter.fromJson(json) ?: mutableListOf())
            .apply { if (!this.contains(key)) add(key) }

        val updateJson = MoshiListConverter.toJson(list)

        PreferencesImpl.scheduledSnoozedNotificationAlarms.set(updateJson)
    }

    /**
     * Remove o dado que indica que um alarme foi agendado para o pacote especificado.
     *
     * @param key A ID da notificação para o qual o dado de agendamento será removido.
     */
    override fun deleteScheduleData(key: String) {

        val json = PreferencesImpl.scheduledSnoozedNotificationAlarms.value
        val list = (MoshiListConverter.fromJson(json) ?: mutableListOf())
            .apply { remove(key) }

        val updateJson = MoshiListConverter.toJson(list)

        PreferencesImpl.scheduledSnoozedNotificationAlarms.set(updateJson)

    }

    /**
     * Objeto utilitário para converter listas de strings de e para JSON usando a biblioteca Moshi.
     * Este objeto é usado para serializar e desserializar a lista de pacotes com alarmes agendados.
     */
    object MoshiListConverter {

        private val moshi = Moshi.Builder().build()
        private val type = Types.newParameterizedType(MutableList::class.java, String::class.java)
        private val adapter = moshi.adapter<MutableList<String>>(type)

        /**
         * Converte uma lista de strings em uma string JSON.
         *
         * @param list A lista de strings a ser convertida.
         * @return A representação JSON da lista.
         */
        fun toJson(list: MutableList<String>): String {
            return adapter.toJson(list)
        }

        /**
         * Converte uma string JSON em uma lista de strings.
         *
         * @param json A string JSON a ser convertida.
         * @return A lista de strings desserializada, uma lista vazia se a string estivee vazia ou `null` se a conversão falhar.
         */
        fun fromJson(json: String): MutableList<String>? {
            return if (json.isEmpty()) mutableListOf() else adapter.fromJson(json)!!
        }
    }
}
