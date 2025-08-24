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

package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.framework.implementations.SystemNotificationManagerImpl
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 * Serviço responsavel por  escutar as notificações do dispositivo, assim que o usuario da permissão de acesso às notificações.
 * Usa-se o [NotificationListenerManagerService] em primeiro plano pra ver se este listener está concetado e iniciar caso não esteja.
 *
 */
class NotificationListener : NotificationListenerService(), CoroutineScope by MainScope() {

    private var debugTests = if (BuildConfig.DEBUG) DebugTests() else null


    companion object {
        /**
         * É um MutableStateFlow para que os observadores possam ser notificados quando a instância do serviço estiver pronta.
         */
        private val serviceInstanceFlow: MutableStateFlow<SystemNotificationManager?> = MutableStateFlow(null)

        /**
         * Obtém a instância do [SystemNotificationManager] de forma assíncrona.
         * Aguarda até que o serviço esteja pronto, mas com um tempo limite.
         * Retorna a instância do serviço se estiver pronta dentro do tempo limite, caso contrário, retorna null.
         */
        suspend fun getWhenReadyOrNull(): SystemNotificationManager? {

            with(serviceInstanceFlow.value) {
                if (this != null) return this
                else NotificationListenerManagerService.instance?.restartListener()
            }

            val result = withTimeoutOrNull(2_000L) {
                serviceInstanceFlow.filterNotNull().first()
            }

            AppLogger.d("reiniciar listener ${if (result == null) "nao resolveu" else "resolveu"}")

            return result
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        AppLogger.d("")

        serviceInstanceFlow.value = SystemNotificationManagerImpl(
            debugTests = debugTests, notificationListener = this@NotificationListener
        )

        observeRulesChanges()
        serviceInstanceFlow.value?.emitNotifications()
    }

    override fun onListenerDisconnected() {
        AppLogger.d("")
        cancel()

        with(serviceInstanceFlow) {
            this.value?.close()
            this.value = null
        }

        /* Isso impede crashes quando o listener é desconectado e o app fica incapaz de remover notificações
         ou chamar o callback pra cancelar o crash agendado.*/
        debugTests?.cancel("Listener desconectado")
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        serviceInstanceFlow.value?.emitNotifications()
        serviceInstanceFlow.value?.processNotification(sbn)
    }

    /**
     * Ajuda  a  [DebugTests] a determinar se a notificação foi de fato cancelada
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        debugTests?.cancelCrashIfNotificationDoesNotRemove(sbn)
        serviceInstanceFlow.value?.emitNotifications()
        super.onNotificationRemoved(sbn, rankingMap)
    }

    /**
     * Observa mudanças nas regras de notificação.
     * Quando uma mudança é detectada (uma regra é adicionada, removida ou atualizada),
     * o mét.odo [SystemNotificationManagerImpl.processActiveNotifications] é chamado para reavaliar todas as notificações ativas
     * com base nas regras atualizadas. Isso garante que as regras sejam aplicadas dinamicamente.
     */
    private fun observeRulesChanges() = launch(IO) {
        HiltEntryPoints.observeAllRulesUseCase().invoke().collect { rules ->
            serviceInstanceFlow.value?.processActiveNotifications()
        }
    }

}

/**
 * Esta classe encapsula funcionalidades de teste e depuração destinadas a serem usadas
 * exclusivamente durante o desenvolvimento (builds de debug).
 * Ela fornece métodos para simular cenários de falha e verificar o comportamento
 * esperado do aplicativo em relação ao gerenciamento de notificações.
 *
 */
class DebugTests : CoroutineScope by MainScope() {


    init {
        if (!BuildConfig.DEBUG) error("Deve ser usada apenas em buids de Debug")
    }

    private var cancelingNotificationKey = ""
    private var errorJob: Job? = null
    private var validationCallbackErrorJob: Job? = null

    /**
     *Esta função é usada para garantir que o aplicativo falhe se o callback não for invocado dentro de um
     * período esperado. Isso ajuda a identificar bugs no processamento da notificação.
     */
    fun crashIfCallbackNotCalled(sbn: StatusBarNotification) {
        validationCallbackErrorJob = launch {
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
        if (BuildConfig.DEBUG) {
            if (activeNotification.isOngoing) return // nao se considera esse tipo de notificação

            cancelingNotificationKey = activeNotification.key
            errorJob?.cancel()
            errorJob = launch {
                delay(1000)
                error("A notificaçao nao foi cancelada: OnGoing?${activeNotification.isOngoing}\nMais detalhes:$activeNotification")
            }
        }
    }

    fun cancelCrashIfNotificationDoesNotRemove(sbn: StatusBarNotification?) {
        if (sbn?.key == cancelingNotificationKey) errorJob?.cancel()
    }
}
