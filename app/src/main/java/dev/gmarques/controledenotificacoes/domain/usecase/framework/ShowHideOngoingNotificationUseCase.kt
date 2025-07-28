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

package dev.gmarques.controledenotificacoes.domain.usecase.framework

import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotificationFactory
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.DeleteHiddenNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.InsertSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 27/07/2025 as 14:35
 * Executa todas as etapas necessárias para ocultar ou reexibir uma notificação persistente no sistema
 */
class ShowHideOngoingNotificationUseCase @Inject constructor(
    private val deleteHiddenNotificationUseCase: DeleteHiddenNotificationUseCase,
    private val insertSnoozedNotificationUseCase: InsertSnoozedNotificationUseCase,
) {

    suspend operator fun invoke(notification: ActiveStatusBarNotification, show: Boolean) {

        val snoozed = SnoozedNotificationFactory.create(notification).copy(permaHidden = true)

        runBlocking {
            if (show) deleteHiddenNotificationUseCase(snoozed)
            else insertSnoozedNotificationUseCase(snoozed)
        }

        NotificationListener.getWhenReady {
            if (show) it.postSnoozedNotification(notification)
            else it.snoozeNotification(notification, System.currentTimeMillis() + SnoozedNotification.defaultSnoozePeriod)
        }

    }
}