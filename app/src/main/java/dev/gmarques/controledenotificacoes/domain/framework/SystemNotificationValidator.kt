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

package dev.gmarques.controledenotificacoes.domain.framework

import android.app.Notification
import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Usado pra validar as notificações do sistema diretamente.
 * Valida se as notificações atendem às regras de negócio para saber se, e como processa-las
 */
object SystemNotificationValidator {

    fun isValidToProcess(notification: StatusBarNotification, acceptOnGoing: Boolean = false): Boolean {

        return notification.packageName != BuildConfig.APPLICATION_ID
                && if (!acceptOnGoing) !notification.isOngoing else true
    }


    fun isValidToEcho(notification: ActiveStatusBarNotification): Boolean {
        return !notification.isOngoing
                && !isMediaPlaybackNotification(notification)
    }

    private fun isMediaPlaybackNotification(notification: ActiveStatusBarNotification): Boolean {
        // Verifica se há estilo de media (MediaStyle)
        return notification.notification.extras.getString(Notification.EXTRA_TEMPLATE)?.contains("MediaStyle") == true
    }

}