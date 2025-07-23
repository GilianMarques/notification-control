package dev.gmarques.controledenotificacoes.domain.framework

import android.app.Notification
import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.BuildConfig

/**
 * Usado pra validar as notificações do sistema diretamente.
 * Valida se as notificações atendem às regras de negócio para saber se, e como processa-las
 */
object SystemNotificationValidator {

    fun isValidToProcess(notification: StatusBarNotification) =
        !notification.isOngoing
                && notification.packageName != BuildConfig.APPLICATION_ID
                && !isMediaPlaybackNotification(notification)


    private fun isMediaPlaybackNotification(sbn: StatusBarNotification): Boolean {
        // Verifica se há estilo de media (MediaStyle)
        return sbn.notification.extras.getString(Notification.EXTRA_TEMPLATE)?.contains("MediaStyle") == true
    }
}