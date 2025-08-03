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
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification.Origin
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 23 de julho de 2025 às 13:14.
 *
 * Responsável por cancelar o adiamento e emitir imediatamente todas as notificações adiadas
 * de um aplicativo específico.
 *
 * Criado originalmente para ser utilizado após a edição de uma regra, uma vez que a reemissão
 * das notificações faz com que elas sejam reprocessadas de acordo com a nova regra.
 *
 * Manipula apenas notificações adiadas automaticamente via regras.
 *
 */
class PostAppSnoozedNotificationsUseCase @Inject constructor(
    private val deleteSnoozedNotificationUseCase: DeleteSnoozedNotificationUseCase,
    private val getSnoozedNotificationsByPackageNameUseCase: GetSnoozedNotificationsByPackageNameUseCase,
    private val backupNotificationAlarmScheduler: BackupNotificationAlarmScheduler,
) {

    suspend operator fun invoke(app: ManagedApp) {
        /**
         * É necessário impor um limite de tempo porque esse UseCase pode ser executado antes que o usuário tenha dado permissão para o
         *  aplicativo ler as notificações fazendo com que o serviço nunca seja retornado e que o aplicativo fique travado.
         */
        val notificationManager = NotificationListener.getWhenReady(500L) ?: return
        val snoozedNotificationsOnDB = getSnoozedNotificationsForAppOnDB(app)

        notificationManager.getSnoozedNotifications()
            .filter { activeNot ->
                activeNot.packageName == app.packageName
                        && snoozedNotificationsOnDB.any { snoozedNot -> activeNot.key == snoozedNot.key }
            }.onEach {

                deleteSnoozedNotificationUseCase(it.key)
                backupNotificationAlarmScheduler.cancelAlarm(it.key)
                notificationManager.postSnoozedNotification(it.key)
            }
    }

    private suspend fun getSnoozedNotificationsForAppOnDB(app: ManagedApp): List<SnoozedNotification> {
        return getSnoozedNotificationsByPackageNameUseCase(app.packageName).filter { it.origin == Origin.RULE }
    }
}
