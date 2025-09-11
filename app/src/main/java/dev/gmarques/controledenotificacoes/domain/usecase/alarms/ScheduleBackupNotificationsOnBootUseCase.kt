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

package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.BackupNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.DeleteSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.GetSnoozedNotificationByKeyUseCase
import dev.gmarques.controledenotificacoes.framework.backup_notification.BackupNotificationManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 30/07/2025 as 14:05
 *
 * Agenda as notificações de backup, uma para cada notificação adiada que foi perdida após reiniciar o dispositivo.
 * Se a data de emissão da notificação adiada já passou, emite uma notificação de backup imediatamente.
 */
class ScheduleBackupNotificationsOnBootUseCase @Inject constructor(
    private val backupNotificationAlarmScheduler: BackupNotificationAlarmScheduler,
    private val getSnoozedNotificationByKeyUseCase: GetSnoozedNotificationByKeyUseCase,
    private val backupNotificationManager: BackupNotificationManager,
    private val deleteSnoozedNotificationUseCase: DeleteSnoozedNotificationUseCase,

    ) {


    suspend operator fun invoke() = withContext(IO) {
        backupNotificationAlarmScheduler.getAllSchedules()
            .onEach { key ->
                val snoozedNotification = getSnoozedNotificationByKeyUseCase(key)

                if (snoozedNotification == null) {
                    backupNotificationAlarmScheduler.cancelAlarm(key)
                    return@onEach
                }

                if (System.currentTimeMillis() >= snoozedNotification.snoozeUntil) {
                    backupNotificationManager.showBackupNotification(snoozedNotification)
                    deleteSnoozedNotificationUseCase(snoozedNotification.key)
                    backupNotificationAlarmScheduler.cancelAlarm(snoozedNotification.key)
                    return@onEach
                }

                backupNotificationAlarmScheduler.scheduleAlarm(key, snoozedNotification.snoozeUntil)
                AppLogger.d(
                    "backup not agendada ${snoozedNotification.title}",
                    snoozedNotification,
                    "snoozeUntil = ${LocalDateTime(snoozedNotification.snoozeUntil)}"
                )
            }
    }
}