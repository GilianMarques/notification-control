package dev.gmarques.controledenotificacoes.framework.implementations


import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.domain.data.repository.ActiveNotificationRepository
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotificationFactory
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:34.
 */
class ActiveNotificationRepositoryImpl @Inject constructor() :
    ActiveNotificationRepository {

    override fun getActiveNotifications(): List<ActiveStatusBarNotification> {

        val nots = NotificationListener.instance?.getFilteredActiveNotifications() ?: emptyList()
        return mapStatusBarNotificationsToActiveStatusBarNotifications(nots)
    }

    /**
     * Mapeia uma lista de [StatusBarNotification] para uma lista de [ActiveStatusBarNotification].
     *
     * Esta função transforma a representação de notificações do sistema em um modelo
     * mais adequado para a camada de apresentação da aplicação.
     */
    private fun mapStatusBarNotificationsToActiveStatusBarNotifications(
        statusBarNotifications: List<StatusBarNotification>,
    ): List<ActiveStatusBarNotification> {
        return statusBarNotifications.map { sbn ->
            sbn.notification.extras
            ActiveStatusBarNotificationFactory.create(sbn)
        }
    }

}

