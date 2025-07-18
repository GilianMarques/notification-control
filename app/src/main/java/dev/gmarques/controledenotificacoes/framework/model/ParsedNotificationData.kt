package dev.gmarques.controledenotificacoes.framework.model

import android.app.Notification
import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.framework.NotificationParser

/**
 * Criado por Gilian Marques
 * Em quinta-feira, 17 de julho de 2025 as 22:32.
 *
 * Representa os dados extraídos de uma [StatusBarNotification] de forma estruturada,
 * isolando o parser do restante da aplicação e promovendo coesão e clareza.
 *
 * Essa estrutura serve como ponte entre o framework Android e os modelos do domínio/apresentação.
 *
 * @see NotificationParser
 */
data class ParsedNotificationData(
    val title: String,
    val content: String,
    val packageName: String,
    val timestamp: Long,
    val smallIcon: Icon?,
    val largeIcon: Icon?,
    val notification: Notification,
    val tag: String?,
)