package dev.gmarques.controledenotificacoes.framework

import android.app.Notification
import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.framework.model.ParsedNotificationData

/**
 * Criado por Gilian Marques
 * Em quinta-feira, 17 de julho de 2025 as 22:32.
 *
 * Responsável por isolar e centralizar a lógica de extração de dados relevantes de uma [StatusBarNotification],
 * evitando repetição de código e facilitando a manutenção.
 *
 * Essa classe fornece uma interface consistente para extrair informações padronizadas de qualquer notificação.
 * É utilizada pelas fábricas de modelos de domínio e apresentação, garantindo uniformidade na interpretação.
 *
 * @see ParsedNotificationData
 */
object NotificationParser {

    /**
     * Converte uma [StatusBarNotification] em uma instância de [ParsedNotificationData],
     * extraindo título, conteúdo, ícones, intent e demais dados relevantes com fallback seguro.
     *
     * A lógica considera diferentes estilos de notificação como InboxStyle e BigTextStyle.
     *
     * @param sbn Notificação do sistema a ser interpretada.
     * @return Dados extraídos da notificação.
     */
    fun parse(sbn: StatusBarNotification): ParsedNotificationData {
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: ""

        val content = when {
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.isNotEmpty() == true -> {
                extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)!!
                    .joinToString("\n") { it.toString() }
            }

            extras.getCharSequence(Notification.EXTRA_BIG_TEXT) != null -> {
                extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString()
            }

            else -> extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        }

        return ParsedNotificationData(
            title = title,
            content = content,
            packageName = sbn.packageName,
            timestamp = sbn.postTime,
            smallIcon = notification.smallIcon,
            largeIcon = notification.getLargeIcon(),
            notification = notification,
            tag = sbn.tag
        )
    }
}
