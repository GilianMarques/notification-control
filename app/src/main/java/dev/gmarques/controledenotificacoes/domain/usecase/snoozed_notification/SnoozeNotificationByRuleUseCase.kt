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
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationFactory
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotificationFactory
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 29/07/2025 as 11:17
 *
 * Adia uma notificação ativa com base em uma regra.
 * Isso envolve criar um registro de notificação adiada, agendar um alarme de backup e, em seguida, adiar a notificação no sistema.
 *
 * ATENÇÃO: Use apenas para adiamentos gerados pela execução de regras.
 *
 */
class SnoozeNotificationByRuleUseCase @Inject constructor(
    private val insertSnoozedNotificationUseCase: InsertSnoozedNotificationUseCase,
    private val backupNotificationAlarmScheduler: BackupNotificationAlarmScheduler,
    private val systemNotificationManager: SystemNotificationManager,
) {

    suspend operator fun invoke(notification: ActiveStatusBarNotification, until: Long) {
        AppLogger.d("", AppNotificationFactory.create(notification))

        if (!systemNotificationManager.canOperate()) return

        val snoozedNotification = SnoozedNotificationFactory.create(notification)
            .copy(
                permaHidden = false,
                origin = SnoozedNotification.Origin.RULE,
                snoozeUntil = until
            )

        insertSnoozedNotificationUseCase(snoozedNotification)
        backupNotificationAlarmScheduler.scheduleAlarm(snoozedNotification.key, until)
        systemNotificationManager.snoozeNotification(notification, until)
    }
}