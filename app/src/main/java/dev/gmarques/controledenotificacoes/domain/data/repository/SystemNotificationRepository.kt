package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener

/**
 * Criado por Gilian Marques
 * Em 23/07/2025 as 11:36
 *
 * É o repositorio usado para lidar com as notificações do sistema, as que estao na barra de estatus e adiadas.
 *
 * Deve ser implementada e exposta via função estatica  pelo listener de notificações [NotificationListener]
 */
interface SystemNotificationRepository {
    fun getActiveNots(): List<ActiveStatusBarNotification>
    fun getSnoozedNots(): List<ActiveStatusBarNotification>
    fun processActiveNotifications()

    /**
     * Adia uma notificação até uma data específica no futuro
     * @param until uma data no futuro, até quando a notificação deve ser adiada
     */
    fun snoozeNot(notification: ActiveStatusBarNotification, until: Long)

    /**Deve emitir imediatamente uma notificação adiada*/
    fun postSnoozedNotification(notification: ActiveStatusBarNotification)
}