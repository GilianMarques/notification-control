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
import dev.gmarques.controledenotificacoes.framework.implementations.BackupNotificationAlarmSchedulerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/**
 * É executado mediante agendamento no sistema para  emitir notificações 'backup'
 * de notificações adiadas que foram perdidas pelo sistema.
 *
 * @see BackupNotificationAlarmSchedulerImpl
 */
class BackupNotificationAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_KEY = "notificationKey"
    }

    private val getSnoozedNotificationByKeyUseCase = HiltEntryPoints.getGetSnoozedNotificationByKeyUseCase()
    private val backupNotificationManager = HiltEntryPoints.backupNotificationManager()
    private val deleteSnoozedNotificationUseCase = HiltEntryPoints.getDeleteSnoozedNotificationUseCase()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        CoroutineScope(IO).launch {
            val key = intent.getStringExtra(NOTIFICATION_KEY) ?: return@launch

            getSnoozedNotificationByKeyUseCase(key)?.let {
                backupNotificationManager.showBackupNotification(it)
                deleteSnoozedNotificationUseCase(it.key)
            }
            removeScheduleData(key)
        }
    }


    /**
     * Remove os dados do agendamento da notificação de backup (referente a notificação adiada) que acabou de ser emitida pelo sistema
     * @param key O nome do pacote do aplicativo cujos dados de agendamento devem ser limpos.
     */
    private fun removeScheduleData(key: String) {

        val scheduleManager = HiltEntryPoints.backupNotificationAlarmSchedulerImpl()

        scheduleManager.deleteScheduleData(key)
    }
}