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
            packageName = parsed.packageName,
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
