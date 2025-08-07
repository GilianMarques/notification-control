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
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification.Origin
import dev.gmarques.controledenotificacoes.framework.backup_notification.BackupNotificationManager
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 06/08/2025 as 20:59
 *
 * Esse caso de uso foi criado com o objetivo de emitir todas as notificações adiadas de um aplicativo que foi removido
 *
 * Ele usa o [PostAppSnoozedNotificationsUseCase] para emitir todas as notificações adiadas disponíveis no sistema
 * Depois itera sobre os agendamentos de notificação que restaram no banco de dados e emite essas notificações
 * como notificações de backup.
 *
 * Depois de remover as notificações adiadas disponíveis no sistema os agendamentos que sobram são de notificações adiadas
 * que se perderam, por isso é necessário exibi-las como notificações de backup
 *
 * Manipula apenas notificações adiadas automaticamente via regras.
 * Processar notificações Manipuladas manualmente pelo usuário Aqui quebraria a funcionalidade de adiamento de notificações ativas do aplicativo
 *
 */ // TODO: testar usecase na pratica
class PostAppSnoozedAndBackupNotificationsUseCase @Inject constructor(
    private val deleteSnoozedNotificationUseCase: DeleteSnoozedNotificationUseCase,
    private val backupNotificationAlarmScheduler: BackupNotificationAlarmScheduler,
    private val getSnoozedNotificationByKeyUseCase: GetSnoozedNotificationByKeyUseCase,
    private val backupNotificationManager: BackupNotificationManager,
    private val postAppSnoozedNotificationsUseCase: PostAppSnoozedNotificationsUseCase,

    ) {

    suspend operator fun invoke(packageName: String) {

        // posta todas as notificações adiadas disponíveis no sistema android
        postAppSnoozedNotificationsUseCase(packageName)

        // posta as notificações que foram perdidas pelo sistema em forma de notificações de backup
        backupNotificationAlarmScheduler.getAllSchedules().onEach { key ->

            val snoozedNotification = getSnoozedNotificationByKeyUseCase(key) ?: run {
                //Isso não deve acontecer (ter agendamento mas notificação ser nula no db) mas se acontecer não vai ter o que mostrar na notificação Então pode retornar
                backupNotificationAlarmScheduler.cancelAlarm(key)
                return@onEach
            }

            if (snoozedNotification.origin != Origin.RULE) return@onEach

            backupNotificationManager.showBackupNotification(snoozedNotification)
            deleteSnoozedNotificationUseCase(snoozedNotification.key)
            backupNotificationAlarmScheduler.cancelAlarm(snoozedNotification.key)

        }
    }


}
