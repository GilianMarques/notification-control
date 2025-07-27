package dev.gmarques.controledenotificacoes.domain.usecase.framework

import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.ReportNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotificationFactory
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.InsertSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 27/07/2025 as 19:59
 *
 * Classe responsável por Executar todas as etapas necessárias para adiar uma notificação
 */
class SnoozeNotificationUseCase @Inject constructor(
    private val reportNotificationAlarmScheduler: ReportNotificationAlarmScheduler,
    private val insertSnoozedNotificationUseCase: InsertSnoozedNotificationUseCase,
) {

    suspend operator fun invoke(notification: ActiveStatusBarNotification, until: Long, permanently: Boolean) {

        val snoozedNotification = SnoozedNotificationFactory.create(notification).copy(permaHidden = permanently)

        runBlocking {
            insertSnoozedNotificationUseCase(snoozedNotification)
        }


        NotificationListener.getWhenReady {

            if (!permanently) {
                it.snoozeNotification(notification, until)
                // TODO: fazer copia de reportNotificationAlarmScheduler e usar aqui
                // TODO: agendar notificaçao reportNotificationAlarmScheduler.scheduleAlarm(snoozedNotification)
                return@getWhenReady
            }

            it.snoozeNotification(
                notification,
                System.currentTimeMillis() + SnoozedNotification.defaultSnoozePeriod
            )
        }
    }
}