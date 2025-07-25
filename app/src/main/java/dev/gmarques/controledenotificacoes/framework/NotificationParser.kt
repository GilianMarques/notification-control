/*
 * MIT License
 *
 * Copyright (c) 2025 Gilian Marques Fernandes - linkedin.com/in/gilianmarques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dev.gmarques.controledenotificacoes.framework

import android.app.Notification
import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationFactory
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotificationFactory
import dev.gmarques.controledenotificacoes.framework.model.ParsedNotificationData

/**
 * Criado por Gilian Marques
 * Em quinta-feira, 17 de julho de 2025 as 22:32.
 *
 * Converte uma [StatusBarNotification] em uma instância de [ParsedNotificationData] que por sua vez é usada em factories
 * como [AppNotificationFactory] ou [ActiveStatusBarNotificationFactory] para instanciar objetos que representam notificações dentro do app.
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
