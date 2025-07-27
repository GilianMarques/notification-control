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
            else it.snoozeNot(notification, System.currentTimeMillis() + SnoozedNotification.defaultSnoozePeriod)
        }

    }
}