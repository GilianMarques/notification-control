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
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.data.repository.SystemNotificationRepository
import dev.gmarques.controledenotificacoes.domain.framework.SystemNotificationValidator
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.AllowNotification
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.AppNotManaged
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.CancelNotification
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.SnoozeNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotificationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 * Serviço responsavel por  escutar as notificações do dispositivo, assim que o usuario da permissão de acesso às notificações.
 * Usa-se o [NotificationServiceManager] em primeiro plano pra ver se este listener está concetado e iniciar caso não esteja.
 *
 */
class NotificationListener : NotificationListenerService(), SystemNotificationRepository, CoroutineScope by MainScope() {

    private val echoImpl = HiltEntryPoints.echo()

    private val processIncomingNotificationUseCase = HiltEntryPoints.processIncomingNotificationUseCase()

    private var debugTests: DebugTests? = null

    companion object {

        private var instance: NotificationListener? = null

        fun instance(): SystemNotificationRepository? {
            return if (instance != null) instance as SystemNotificationRepository else null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this@NotificationListener
        if (BuildConfig.DEBUG) debugTests = DebugTests()
        observeRulesChanges()
    }

    /**
     * Observa mudanças nas regras de notificação.
     * Quando uma mudança é detectada (uma regra é adicionada, removida ou atualizada),
     * o mét.odo [processActiveNotifications] é chamado para reavaliar todas as notificações ativas
     * com base nas regras atualizadas. Isso garante que as regras sejam aplicadas dinamicamente.
     */
    private fun observeRulesChanges() = launch(IO) {
        HiltEntryPoints.observeAllRulesUseCase().invoke().collect { rules ->
            processActiveNotifications()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        processNotification(sbn)
    }

    /**
     * Processa uma notificação recebida pra saber se ela sera cancelada, adiada, permitida, ecoada, etc...
     * Ao fim do processamento executa a ação necessario com base na regra, caso exista uma.
     * Usa o [ProcessIncomingNotificationUseCase]
     * para processar os dados e salvar a notificação se necessario.
     */
    private fun processNotification(sbn: StatusBarNotification) {

        if (!SystemNotificationValidator.isValidToProcess(sbn)) return

        debugTests?.crashIfCallbackNotCalled(sbn)

        val result = processIncomingNotificationUseCase(sbn)

        when (result) {
            is AllowNotification -> {
                //   Log.d("USUK", "NotificationListener.processNotification: AllowNotification: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                echoImpl.repostNotification(result.targetNotification)
            }

            is AppNotManaged -> {
                //   Log.d("USUK", "NotificationListener.processNotification: AppNotManaged: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                echoImpl.repostNotification(result.targetNotification)
            }

            is CancelNotification -> {
                //   Log.d("USUK", "NotificationListener.processNotification: CancelNotification: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                debugTests?.crashIfNotificationDoesNotRemove(result.targetNotification)
                cancelNotification(result.targetNotification.key)
            }

            is SnoozeNotification -> {
                /*  Log.d(
                      "USUK",
                      "NotificationListener.processNotification: SnoozeNotification: snoozeFor: ${result.snoozeFor} not: ${result.targetNotification} "
                  )*/
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) error("Essa função nao deve ser chamada em versões anteriores ao Oreo")
                debugTests?.cancelCrashIfCallbackNotCalled()
                debugTests?.crashIfNotificationDoesNotRemove(result.targetNotification)
                snoozeNotification(result.targetNotification.key, result.snoozeFor)
            }
        }


    }

    /**
     * Ajuda  a  [DebugTests] a determinar se a notificação foi de fato cancelada
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        debugTests?.cancelCrashIfNotificationDoesNotRemove(sbn)
        super.onNotificationRemoved(sbn, rankingMap)
    }

    override fun onListenerDisconnected() {
        cancel()
        instance = null
        super.onListenerDisconnected()
    }

    fun isListenerConnected(): Boolean {
        val cn = ComponentName(baseContext, NotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(
            baseContext.contentResolver, "enabled_notification_listeners"
        )
        return (enabledListeners?.contains(cn.flattenToString()) == true)
    }

    override fun getActiveNots(): List<ActiveStatusBarNotification> {

        if (!isListenerConnected()) return emptyList()

        return activeNotifications?.filter {
            SystemNotificationValidator.isValidToProcess(it)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        } ?: emptyList()
    }

    override fun getOngoingNots(): List<ActiveStatusBarNotification> {

        if (!isListenerConnected()) return emptyList()

        return activeNotifications?.filter {
            it.isOngoing && SystemNotificationValidator.isValidToProcess(it, true)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        } ?: emptyList()
    }

    override fun getSnoozedNots(): List<ActiveStatusBarNotification> {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !isListenerConnected()) return emptyList()

        return snoozedNotifications?.filter {
            SystemNotificationValidator.isValidToProcess(it)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        } ?: emptyList()
    }

    /**
     * Usa [processNotification] para processar todas as notificações ativas validas
     */
    override fun processActiveNotifications() {
        if (!isListenerConnected()) return

        val active = activeNotifications ?: return
        active.forEach { sbn ->
            processNotification(sbn)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun snoozeNot(notification: ActiveStatusBarNotification, until: Long) {
        val awaitTimeUntilPost = LocalDateTime(until).toDate().time - LocalDateTime.now().toDate().time
        snoozeNotification(notification.key, awaitTimeUntilPost)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun postSnoozedNotification(notification: ActiveStatusBarNotification) {
        snoozeNotification(notification.key, 500L)
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
     * @see NotificationListener.processNotification
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
