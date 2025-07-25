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

package dev.gmarques.controledenotificacoes.domain.model

import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.framework.NotificationParser
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
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
            packageName = parsed.packageName,
            title = parsed.title,
            content = parsed.content,
            postTime = parsed.timestamp
        )
    }

    /**Cria uma [AppNotification] a partir de um [ActiveStatusBarNotification]*/
    fun create(act: ActiveStatusBarNotification): AppNotification {

        return AppNotification(
            packageName = act.packageName,
            title = act.title,
            content = act.content,
            postTime = act.postTime
        )
    }
}