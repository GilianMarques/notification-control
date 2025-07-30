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

package dev.gmarques.controledenotificacoes.framework.implementations

import android.service.notification.StatusBarNotification
import android.util.Log
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.framework.SystemNotificationValidator
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.AllowNotification
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.AppNotManaged
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.CancelNotification
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.SnoozeNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotificationFactory
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.DebugTests
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import org.joda.time.LocalDateTime

/**
 * Criado por Gilian Marques
 * Em domingo, 27 de julho de 2025 as 15:15.
 * Permite obter e gerenciar as notificações disponiveis no sistema
 *
 *  Obtenha uma instancia dessa classe atraves de [NotificationListener.getOrNull] ou [NotificationListener.getWhenReady]
 */
class SystemNotificationManagerImpl(private var listener: NotificationListener, private val debugTests: DebugTests?) :
    SystemNotificationManager {

    private val echoImpl = HiltEntryPoints.echo()
    private val processIncomingNotificationUseCase = HiltEntryPoints.processIncomingNotificationUseCase()
    private val getSnoozedNotificationByKeyUseCase = HiltEntryPoints.getGetSnoozedNotificationByKeyUseCase()
    private val deleteSnoozedNotificationUseCase = HiltEntryPoints.getDeleteSnoozedNotificationUseCase()
    private val snoozeNotificationByRuleUseCase = HiltEntryPoints.getSnoozeNotificationByRuleUseCase()

    private val backupNotificationAlarmSchedulerImpl = HiltEntryPoints.backupNotificationAlarmSchedulerImpl()

    private val snoozedFlow = MutableStateFlow<List<ActiveStatusBarNotification>>(emptyList())
    private val activeFlow = MutableStateFlow<List<ActiveStatusBarNotification>>(emptyList())
    private val ongoingFlow = MutableStateFlow<List<ActiveStatusBarNotification>>(emptyList())
    val activeWithOngoingFlow: StateFlow<List<ActiveStatusBarNotification>> =
        combine(activeFlow, ongoingFlow) { active, ongoing ->
            active + ongoing
        }.stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )


    override fun getActiveNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = activeFlow

    override fun getActiveNotifications(): List<ActiveStatusBarNotification> {
        if (!listener.isListenerConnected()) return emptyList()
        return listener.activeNotifications?.filter {
            SystemNotificationValidator.isValidToProcess(it)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        }.applyDefaultFilter()
    }

    override fun getOngoingNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = ongoingFlow

    override fun getOngoingNotifications(): List<ActiveStatusBarNotification> {
        if (!listener.isListenerConnected()) return emptyList()
        return listener.activeNotifications?.filter {
            it.isOngoing && SystemNotificationValidator.isValidToProcess(it, true)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        }.applyDefaultFilter()
    }

    override fun getActiveWithOngoingNotificationsFlow() = activeWithOngoingFlow

    override fun getActiveWithOngoingNotifications(): List<ActiveStatusBarNotification> {
        return getOngoingNotifications() + getActiveNotifications()
    }

    override fun getSnoozedNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = snoozedFlow

    override fun getSnoozedNotifications(): List<ActiveStatusBarNotification> {
        if (!listener.isListenerConnected()) return emptyList()
        return listener.snoozedNotifications?.filter {
            SystemNotificationValidator.isValidToProcess(it)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        }.applyDefaultFilter()
    }

    override fun processActiveNotifications() {
        if (!listener.isListenerConnected()) return
        listener.activeNotifications?.forEach { processNotification(it) }
    }

    override fun snoozeNotification(notification: ActiveStatusBarNotification, until: Long) {
        val time = LocalDateTime(until).toDate().time - LocalDateTime.now().toDate().time
        listener.snoozeNotification(notification.key, time)
    }

    override fun cancelNotification(key: String) {
        listener.cancelNotification(key)
    }

    override fun postSnoozedNotification(key: String) {
        listener.snoozeNotification(key, 500L)
    }

    /**
     * Atualiza os FLows de notificações ativas, adiadas e em andamento om base no conteudo do Listener de notificações.
     */
    fun emitNotifications() {
        activeFlow.value = getActiveNotifications()
        snoozedFlow.value = getSnoozedNotifications()
        ongoingFlow.value = getOngoingNotifications()
    }

    fun processNotification(sbn: StatusBarNotification) {

        if (!SystemNotificationValidator.isValidToProcess(sbn, true)) return

        val snoozedNotification = runBlocking { getSnoozedNotificationByKeyUseCase(sbn.key) }

        if (snoozedNotification != null) processSnoozedNotification(snoozedNotification, sbn)
        else processNotificationRule(sbn)

    }

    private fun processSnoozedNotification(snoozedNotification: SnoozedNotification, sbn: StatusBarNotification) = runBlocking {
        // TODO: transformar em usecase?

        if (snoozedNotification.permaHidden) {
            snoozeNotification(
                ActiveStatusBarNotificationFactory.create(sbn),
                System.currentTimeMillis() + SnoozedNotification.DEFAULT_SNOOZED_PERIOD
            )
            return@runBlocking
        }

        if (snoozedNotificationPostedTooEarly(snoozedNotification)) {
            snoozeNotification(ActiveStatusBarNotificationFactory.create(sbn), snoozedNotification.snoozeUntil)
            return@runBlocking
        }

        if (backupNotificationAlarmSchedulerImpl.isThereAnyAlarmSetForKey(snoozedNotification.key)) {
            backupNotificationAlarmSchedulerImpl.cancelAlarm(snoozedNotification.key)
            deleteSnoozedNotificationUseCase(snoozedNotification.key)
        }

    }

    /**
     * Ajuda a determinar se a notificação adiada foi postada pelo sistema mais cedo do que deveria. Isso garante que a
     * notificação fique adiada até o horario definido pelo usuário, mesmo que seja repostada pelo app emissor.
     *
     * @return true se a notificação foi postada mais cedo do que deveria, senão, false.
     */
    private fun snoozedNotificationPostedTooEarly(snoozedNotification: SnoozedNotification): Boolean {
        val nowWithOffset = LocalDateTime.now().minusMillis(SnoozedNotification.SNOOZE_TIME_OFFSET)
        return LocalDateTime(snoozedNotification.snoozeUntil).isBefore(nowWithOffset)
    }

    /**
     * Processa uma notificação recebida pra saber se ela sera cancelada, adiada, permitida, ecoada, etc...
     * Ao fim do processamento executa a ação necessario com base na regra, caso exista uma.
     * Usa o [ProcessIncomingNotificationUseCase]
     * para processar os dados e salvar a notificação se necessario.
     */
    fun processNotificationRule(sbn: StatusBarNotification) {

        debugTests?.crashIfCallbackNotCalled(sbn)

        val result = processIncomingNotificationUseCase(sbn)

        when (result) {
            is AllowNotification -> {
                Log.d("USUK", "NotificationListener.processNotification: AllowNotification: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                echoImpl.repostNotification(result.targetNotification)
            }

            is AppNotManaged -> {
                Log.d("USUK", "NotificationListener.processNotification: AppNotManaged: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                echoImpl.repostNotification(result.targetNotification)
            }

            is CancelNotification -> {
                Log.d("USUK", "NotificationListener.processNotification: CancelNotification: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                debugTests?.crashIfNotificationDoesNotRemove(result.targetNotification)
                listener.cancelNotification(result.targetNotification.key)
            }

            is SnoozeNotification -> {
                Log.d("USUK", "NotificationListener.processNotification: SnoozeNotification: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                debugTests?.crashIfNotificationDoesNotRemove(result.targetNotification)
                runBlocking { snoozeNotificationByRuleUseCase(result.targetNotification, result.snoozeFor) }
            }
        }


    }

    private fun List<ActiveStatusBarNotification>?.applyDefaultFilter(): List<ActiveStatusBarNotification> {
        return this?.filterNot { it.content.isEmpty() && it.title.isEmpty() }
            ?.distinctBy { it.title to it.content }
            ?.distinctBy { it.key }
            ?: emptyList()
    }


}

