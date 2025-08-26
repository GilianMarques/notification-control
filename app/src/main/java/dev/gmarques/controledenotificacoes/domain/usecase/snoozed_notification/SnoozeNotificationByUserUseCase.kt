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

import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.BackupNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotificationFactory
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 27/07/2025 as 19:59
 *
 * Classe responsável por executar todas as etapas necessárias para adiar ou ocultar uma notificação.
 * Isso inclui salvar a notificação no banco de dados, agendar um alarme para restaurá-la (se não for permanente)
 * e, finalmente, dispensá-la da barra de status.
 *
 * ATENÇÃO: Use apenas para adiamentos/ocultações gerados diretamente pelo usuário.
 *
 */
class SnoozeNotificationByUserUseCase @Inject constructor(
    private val insertSnoozedNotificationUseCase: InsertSnoozedNotificationUseCase,
    private val backupNotificationAlarmScheduler: BackupNotificationAlarmScheduler,
    private val systemNotificationManager: SystemNotificationManager,

    ) {

    suspend operator fun invoke(notification: ActiveStatusBarNotification, until: Long, permanently: Boolean) {
        AppLogger.d("")

        if (!systemNotificationManager.canOperate()) return

        val snoozedNotification = SnoozedNotificationFactory.create(notification)
            .copy(
                permaHidden = permanently,
                origin = SnoozedNotification.Origin.USER,
                snoozeUntil = until
            )

        insertSnoozedNotificationUseCase(snoozedNotification)

        if (permanently) systemNotificationManager.snoozeNotification(
            notification,
            System.currentTimeMillis() + SnoozedNotification.DEFAULT_SNOOZED_PERIOD
        )
        else {
            backupNotificationAlarmScheduler.scheduleAlarm(snoozedNotification.key, until)
            systemNotificationManager.snoozeNotification(notification, until)
        }

    }
}