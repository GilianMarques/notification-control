package dev.gmarques.controledenotificacoes.domain.framework

import android.app.Notification
import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Usado pra validar as notificações do sistema diretamente.
 * Valida se as notificações atendem às regras de negócio para saber se, e como processa-las
 */
object SystemNotificationValidator {

    fun isValidToProcess(notification: StatusBarNotification): Boolean {
        return !notification.isOngoing
                && notification.packageName != BuildConfig.APPLICATION_ID
    }

    fun isValidToEcho(notification: ActiveStatusBarNotification): Boolean {
        return !notification.isOngoing
                && !isMediaPlaybackNotification(notification)
    }

    private fun isMediaPlaybackNotification(notification: ActiveStatusBarNotification): Boolean {
        // Verifica se há estilo de media (MediaStyle)
        return notification.notification.extras.getString(Notification.EXTRA_TEMPLATE)?.contains("MediaStyle") == true
    }

}