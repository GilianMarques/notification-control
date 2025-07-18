package dev.gmarques.controledenotificacoes.domain.model

import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.framework.NotificationParser
import dev.gmarques.controledenotificacoes.framework.model.ParsedNotificationData

/**
 * Criado por Gilian Marques
 * Em 17/07/2025 as 22:25
 *
 * Fábrica responsável por criar instâncias de [AppNotification] a partir de [StatusBarNotification].
 * Utiliza o [NotificationParser] para garantir consistência e segurança na extração dos dados.
 *
 * Essa separação mantém o domínio desacoplado do Android Framework, permitindo testes e reuso.
 *
 * @see AppNotification
 * @see NotificationParser
 * @see ParsedNotificationData
 */
object AppNotificationFactory {

    /**
     * @param sbn Notificação recebida via [android.service.notification.NotificationListenerService].
     * @return Uma instância de [AppNotification] representando dados relevantes da notificação.
     */
    fun create(sbn: StatusBarNotification): AppNotification {
        val parsed = NotificationParser.parse(sbn)

        return AppNotification(
            packageId = parsed.packageId,
            title = parsed.title,
            content = parsed.content,
            timestamp = parsed.timestamp
        )
    }
}