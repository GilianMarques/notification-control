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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.manage_notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.domain.data.repository.SystemNotificationRepository
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 25/07/2025 as 16:54
 */
@HiltViewModel
class ManageNotificationsViewModel @Inject constructor(
) : ViewModel() {

    private val _notificationsFlow: MutableStateFlow<List<ActiveStatusBarNotification>> = MutableStateFlow(emptyList())
    val notificationsFlow: Flow<List<ActiveStatusBarNotification>> get() = _notificationsFlow

    private var observerJob: Job = Job()

    fun loadActiveNotifications() {

        getServiceWhenReady() { notificationService ->
            observeNotifications(notificationService.getActiveNotsFlow())
        }
    }


    fun loadSnoozedNotifications() {
        getServiceWhenReady() { notificationService ->
            observeNotifications(notificationService.getSnoozedNotsFlow())
        }
    }

    fun loadOngoingNotifications() {
        getServiceWhenReady() { notificationService ->
            observeNotifications(notificationService.getOngoingNotsFlow())
        }
    }

    /**
     * Aguarda o servico de notificacoes estar pronto e entao executa [callback]
     * passando o servico como parametro
     * @param callback Funcao a ser executada com o servico como parametro
     */
    private fun getServiceWhenReady(callback: (notificationService: SystemNotificationRepository) -> Unit) {
        viewModelScope.launch {
            NotificationListener.getWhenReady()
                .filterNotNull()
                .first()
                .let { notificationService ->
                    callback(notificationService)
                }
        }
    }

    /**
     * Observa o fluxo de notificacoes [flow] e atualiza o estado interno [_notificationsFlow]
     * @param flow Fluxo de notificacoes a ser observado
     */
    private fun observeNotifications(flow: Flow<List<ActiveStatusBarNotification>>) {

        observerJob.cancel()

        observerJob = viewModelScope.launch {
            flow.collect { nots ->
                _notificationsFlow.tryEmit(
                    nots
                        .filter { it.content.isNotBlank() || it.title.isNotBlank() }
                        .distinctBy { it.title to it.content })
            }
        }

    }

    fun snoozeNotification(notification: ActiveStatusBarNotification) {
        NotificationListener.get()?.snoozeNot(notification, System.currentTimeMillis() + 10000)
    }
}
