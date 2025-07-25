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

package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener

/**
 * Criado por Gilian Marques
 * Em 23/07/2025 as 11:36
 *
 * É o repositorio usado para lidar com as notificações do sistema, as que estao na barra de estatus e adiadas.
 *
 * Deve ser implementada e exposta via função estatica  pelo listener de notificações [NotificationListener]
 */
interface SystemNotificationRepository {
    fun getActiveNots(): List<ActiveStatusBarNotification>
    fun getSnoozedNots(): List<ActiveStatusBarNotification>
    fun processActiveNotifications()

    /**
     * Adia uma notificação até uma data específica no futuro
     * @param until uma data no futuro, até quando a notificação deve ser adiada
     */
    fun snoozeNot(notification: ActiveStatusBarNotification, until: Long)

    /**Deve emitir imediatamente uma notificação adiada*/
    fun postSnoozedNotification(notification: ActiveStatusBarNotification)
}