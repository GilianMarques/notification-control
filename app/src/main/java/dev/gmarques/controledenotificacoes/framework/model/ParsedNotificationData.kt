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