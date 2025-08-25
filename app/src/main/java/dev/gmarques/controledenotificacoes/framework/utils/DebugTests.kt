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

package dev.gmarques.controledenotificacoes.framework.utils

import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Esta classe encapsula funcionalidades de teste e depuração destinadas a serem usadas
 * exclusivamente durante o desenvolvimento (builds de debug).
 * Ela fornece métodos para simular cenários de falha e verificar o comportamento
 * esperado do aplicativo em relação ao gerenciamento de notificações.
 *
 */
class DebugTests(private val processorCoroutineScope: CoroutineScope) {

    init {
        if (!BuildConfig.DEBUG) error("Deve ser usada apenas em buids de Debug")
    }

    private var instanceErrorMsg = "use uma instancia dessa classe para cada notificação processada"
    private var errorJob: Job? = null
    private var validationCallbackErrorJob: Job? = null

    /**
     *Esta função é usada para garantir que o aplicativo falhe se o callback não for invocado dentro de um
     * período esperado. Isso ajuda a identificar bugs no processamento da notificação.
     */
    fun crashIfCallbackNotCalled(sbn: StatusBarNotification) {

        if (validationCallbackErrorJob != null) error(instanceErrorMsg)

        validationCallbackErrorJob = processorCoroutineScope.launch {
            delay(3000)
            error("O callback de validação passado para o RuleEnforcer não foi chamado. sbn: $sbn")
        }
    }

    fun cancelCrashIfCallbackNotCalled() {
        validationCallbackErrorJob?.cancel()
    }

    /**
     * Caso alguma alteraçao que impeça o bloqueio das notificações seja feita (como ja foi feita antes...)
     * essa função vai crashar o app para que o jumento do desenvolvedor (eu ;-] ) possa ajeitar a cagada que ele fez
     */
    fun crashIfNotificationDoesNotRemove(activeNotification: ActiveStatusBarNotification) {
        if (errorJob != null) error(instanceErrorMsg)

        errorJob = processorCoroutineScope.launch {

            val removed = withTimeoutOrNull(1000) { // TODO: isso deve ta dando erro
                NotificationListener.onNotificationRemovedFlow
                    .first { it.key == activeNotification.key }
            }

            if (removed != null) errorJob?.cancel()
            else error("A notificação não foi cancelada: OnGoing?${activeNotification.isOngoing}\nMais detalhes:$activeNotification")

        }
    }


}