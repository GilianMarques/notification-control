package dev.gmarques.controledenotificacoes.framework.model

import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.framework.NotificationParser


/**
 * Criado por Gilian Marques
 * Em quinta-feira, 17 de julho de 2025 as 22:31.
 *
 * Fábrica responsável por construir [ActiveStatusBarNotification], um modelo
 * contendo informações completas sobre uma notificação em exibição.
 *
 * Essa factory garante que toda extração seja delegada ao [NotificationParser], centralizando a lógica e
 * evitando duplicações.
 *
 * @see NotificationParser
 * @see ParsedNotificationData
 * @see ActiveStatusBarNotification
 *
 */
object ActiveStatusBarNotificationFactory {

    fun create(sbn: StatusBarNotification): ActiveStatusBarNotification {
        val parsed = NotificationParser.parse(sbn)

        return ActiveStatusBarNotification(
            title = parsed.title,
            content = parsed.content,
            packageId = parsed.packageId,
            postTime = parsed.timestamp,
            smallIcon = parsed.smallIcon,
            largeIcon = parsed.largeIcon,
            id = sbn.id,
            key = sbn.key,
            isOngoing = sbn.isOngoing,
            notification = parsed.notification,
            tag = parsed.tag,
        )
    }
}
