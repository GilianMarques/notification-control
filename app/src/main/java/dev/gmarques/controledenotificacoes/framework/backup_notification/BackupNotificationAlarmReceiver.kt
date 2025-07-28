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

package dev.gmarques.controledenotificacoes.framework.backup_notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import kotlinx.coroutines.runBlocking
import dev.gmarques.controledenotificacoes.framework.implementations.BackupNotificationAlarmSchedulerImpl

/**
 * É executado mediante agendamento no sistema para  emitir notificações 'backup' de notificações adiadas que foram perdidas pelo sistema.
 *
 * @see BackupNotificationAlarmSchedulerImpl
 *
 */
class BackupNotificationAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_KEY = "notificationKey"
    }

    // TODO: terminar de editar
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val key = intent.getStringExtra(NOTIFICATION_KEY) ?: return

        val snoozedNotification = runBlocking { HiltEntryPoints.getGetSnoozedNotificationByKeyUseCase().invoke(key) }
        if (snoozedNotification != null) {
            HiltEntryPoints.backupNotificationManager().showBackupNotification(snoozedNotification)
        }

        clearPreferenceForKey(key)
    }


    /**
     * Cancela o agendamento da notificação de backup da notificação adiada que acabou de ser emitida pelo sistema
     * @param key O nome do pacote do aplicativo cujos dados de agendamento devem ser limpos.
     */
    private fun clearPreferenceForKey(key: String) {

        val scheduleManager = HiltEntryPoints.snoozedNotificationScheduleManager()

        scheduleManager.deleteScheduleData(key)
    }
}