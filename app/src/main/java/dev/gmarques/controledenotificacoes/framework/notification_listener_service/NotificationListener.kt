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

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.framework.implementations.SystemNotificationManagerImpl
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 * Serviço responsavel por  escutar as notificações do dispositivo, assim que o usuario da permissão de acesso às notificações.
 * Usa-se o [NotificationServiceManager] em primeiro plano pra ver se este listener está concetado e iniciar caso não esteja.
 *
 */
class NotificationListener : NotificationListenerService(), CoroutineScope by MainScope() {

    private var debugTests: DebugTests? = null

    /**
     * Responsavel por expor as funçoes necessarias dessa classe + funções utilitarias para o resto do sistema
     */
    private val systemNotificationManager: SystemNotificationManagerImpl = SystemNotificationManagerImpl(this, debugTests)

    companion object {
        /**
         * É um MutableStateFlow para que os observadores possam ser notificados quando a instância do serviço estiver pronta.
         */
        private val serviceInstance: MutableStateFlow<SystemNotificationManager?> = MutableStateFlow(null)

        /**
         * Obtém a instância do serviço [SystemNotificationManager].
         * Retorna a instância atual do serviço se estiver disponível, caso contrário, retorna null.
         *
         * @return A instância do [SystemNotificationManager]  ou null se não estiver disponível.
         */
        fun getOrNull(): SystemNotificationManager? {
            return if (serviceInstance.value != null) serviceInstance.value else null
        }

        /**
         * Obtém a instância do [SystemNotificationManager] de forma síncrona,
         * bloqueando a thread atual até que o serviço esteja pronto.
         * @return A instância do [SystemNotificationManager].
         */
        suspend fun getWhenReady(): SystemNotificationManager {
            return serviceInstance.filterNotNull().first()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceInstance.tryEmit(systemNotificationManager)
        if (BuildConfig.DEBUG) debugTests = DebugTests()
        observeRulesChanges()
        systemNotificationManager.emitNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        systemNotificationManager.emitNotifications()
        systemNotificationManager.processNotification(sbn)
    }

    /**
     * Ajuda  a  [DebugTests] a determinar se a notificação foi de fato cancelada
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        debugTests?.cancelCrashIfNotificationDoesNotRemove(sbn)
        systemNotificationManager.emitNotifications()
        super.onNotificationRemoved(sbn, rankingMap)
    }

    override fun onListenerDisconnected() {
        cancel()
        serviceInstance.tryEmit(null)
        super.onListenerDisconnected()
    }

    fun isListenerConnected(): Boolean {
        val cn = ComponentName(baseContext, NotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(
            baseContext.contentResolver, "enabled_notification_listeners"
        )
        return (enabledListeners?.contains(cn.flattenToString()) == true)
    }

    /**
     * Observa mudanças nas regras de notificação.
     * Quando uma mudança é detectada (uma regra é adicionada, removida ou atualizada),
     * o mét.odo [SystemNotificationManagerImpl.processActiveNotifications] é chamado para reavaliar todas as notificações ativas
     * com base nas regras atualizadas. Isso garante que as regras sejam aplicadas dinamicamente.
     */
    private fun observeRulesChanges() = launch(IO) {
        HiltEntryPoints.observeAllRulesUseCase().invoke().collect { rules ->
            systemNotificationManager.processActiveNotifications()
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
class DebugTests {


    init {
        if (!BuildConfig.DEBUG) error("Deve ser usada apenas em buids de begug")
    }

    private var cancelingNotificationKey = ""
    private var errorJob: Job? = null
    private var validationCallbackErrorJob: Job? = null

    /**
     *Esta função é usada para garantir que o aplicativo falhe se o callback não for invocado dentro de um
     * período esperado. Isso ajuda a identificar bugs no processamento da notificação.
     * @see NotificationListener.processNotificationRule
     */
    fun crashIfCallbackNotCalled(sbn: StatusBarNotification) {
        validationCallbackErrorJob = CoroutineScope(Main).launch {
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
            errorJob = CoroutineScope(Main).launch {
                delay(1000)
                error("A notificaçao nao foi cancelada: OnGoing?${activeNotification.isOngoing}\nMais detalhes:$activeNotification")
            }
        }
    }

    fun cancelCrashIfNotificationDoesNotRemove(sbn: StatusBarNotification?) {
        if (BuildConfig.DEBUG) if (sbn?.key == cancelingNotificationKey) errorJob?.cancel()
    }
}
