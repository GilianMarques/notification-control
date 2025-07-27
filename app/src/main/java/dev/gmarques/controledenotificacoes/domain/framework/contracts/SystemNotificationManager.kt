package dev.gmarques.controledenotificacoes.domain.framework.contracts

import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import kotlinx.coroutines.flow.Flow

/**
 * Criado por Gilian Marques
 * Em 23/07/2025 as 11:36
 *
 * É o repositorio usado para lidar com as notificações do sistema, as que estao na barra de estatus e adiadas.
 *
 * Deve ser implementada e exposta via função estatica  pelo listener de notificações [dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener]
 */
interface SystemNotificationManager {
    fun getActiveNotificationsFlow(): Flow<List<ActiveStatusBarNotification>>
    fun getActiveNotifications(): List<ActiveStatusBarNotification>
    fun getOngoingNotificationsFlow(): Flow<List<ActiveStatusBarNotification>>
    fun getOngoingNotifications(): List<ActiveStatusBarNotification>
    fun getSnoozedNotificationsFlow(): Flow<List<ActiveStatusBarNotification>>
    fun getSnoozedNotifications(): List<ActiveStatusBarNotification>
    fun processActiveNotifications()

    /**
     * Adia uma notificação até uma data específica no futuro
     * @param until uma data no futuro, até quando a notificação deve ser adiada
     */
    fun snoozeNotification(notification: ActiveStatusBarNotification, until: Long)

    /**Deve emitir imediatamente uma notificação adiada*/
    fun postSnoozedNotification(notification: ActiveStatusBarNotification)
}