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

/**
 * Criado por Gilian Marques
 * Em 01/07/2025 as 17:03
 *
 * Representa uma [android.service.notification.StatusBarNotification] apenas com os dados que o app precisa.
 * Serve para evitar dependência do Domínio com o Framework Android.
 *
 * Use [ActiveStatusBarNotificationFactory] para instanciar o objeto com segurança
 */
data class ActiveStatusBarNotification(
    val title: String,
    val content: String,
    val packageName: String,
    val smallIcon: Icon?,
    val largeIcon: Icon?,
    val postTime: Long,
    val id: Int,
    val key: String,
    val isOngoing: Boolean,
    val notification: Notification,
    val tag: String?,
    )
