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

package dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification

import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.BackupNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotificationFactory
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 27/07/2025 as 14:35
 *
 * Executa todas as etapas necessárias para  reexibir uma notificação adiada no sistema
 *
 * Criada originalmente  para reexibir (desocultar) uma notificação persistente ocultada pelo usuario. Tambem funciona com
 * notificações normais.
 */
class PostSnoozedNotificationUseCase @Inject constructor(
    private val deleteSnoozedNotificationUseCase: DeleteSnoozedNotificationUseCase,
    private val backupNotificationAlarmScheduler: BackupNotificationAlarmScheduler
) {

    suspend operator fun invoke(notification: ActiveStatusBarNotification) =
        NotificationListener.getWhenReady { notificationManager ->
            {
                val snoozed = SnoozedNotificationFactory.create(notification)

                runBlocking {
                    deleteSnoozedNotificationUseCase(snoozed)
                }

                backupNotificationAlarmScheduler.cancelAlarm(snoozed.key)

                notificationManager.postSnoozedNotification(notification)
            }

        }
}