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
import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.framework.SystemNotificationValidator
import dev.gmarques.controledenotificacoes.domain.framework.SystemNotificationValidator.applyDefaultFilter
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationFactory
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
import kotlinx.coroutines.MainScope
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
 *  Obtenha uma instancia dessa classe atraves de  [NotificationListener.getWhenReadyOrNull]
 */
class SystemNotificationManagerImpl(
    private val debugTests: DebugTests?,
    private var notificationListener: NotificationListener?,
) :
    SystemNotificationManager, CoroutineScope by MainScope() {

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
        }
            .stateIn(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = emptyList()
            )


    override fun getActiveNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = activeFlow

    override fun getActiveNotifications(): List<ActiveStatusBarNotification> {

        if (notificationListener == null) {
            AppLogger.d("notificationListener == null")
            return emptyList()
        }

        if (notificationListener?.isListenerConnected() == false) return emptyList()
        return notificationListener?.activeNotifications?.filterNot {
            it.isOngoing
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        }.applyDefaultFilter()
    }

    override fun getOngoingNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = ongoingFlow

    override fun getOngoingNotifications(): List<ActiveStatusBarNotification> {

        if (notificationListener == null) {
            AppLogger.d("notificationListener == null")
            return emptyList()
        }

        if (notificationListener?.isListenerConnected() == false) return emptyList()
        return notificationListener?.activeNotifications?.filter {
            it.isOngoing
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        }.applyDefaultFilter()
    }

    override fun getActiveWithOngoingNotificationsFlow() = activeWithOngoingFlow

    override fun getActiveWithOngoingNotifications(): List<ActiveStatusBarNotification> {
        return getOngoingNotifications() + getActiveNotifications()
    }

    override fun getSnoozedNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = snoozedFlow

    /**
     * Retorna a lista de notificações adiadas.
     * Inclui notificações Dispensaveis e Persistentes
     */
    override fun getSnoozedNotifications(): List<ActiveStatusBarNotification> {

        if (notificationListener == null) {
            AppLogger.d("notificationListener == null")
            return emptyList()
        }

        if (notificationListener?.isListenerConnected() == false) return emptyList()
        return notificationListener?.snoozedNotifications?.map {
            ActiveStatusBarNotificationFactory.create(it)
        }.applyDefaultFilter()
    }

    override fun processActiveNotifications() {

        if (notificationListener == null) {
            AppLogger.d("notificationListener == null")
            return
        }

        if (notificationListener?.isListenerConnected() == false) return
        notificationListener?.activeNotifications?.forEach { processNotification(it) }
    }

    override fun snoozeNotification(notification: ActiveStatusBarNotification, until: Long) {
        AppLogger.d(notification.title, AppNotificationFactory.create(notification), "until = $until")

        if (notificationListener == null) {
            AppLogger.d("notificationListener == null", notification)
            return
        }

        val time = LocalDateTime(until).toDate().time - LocalDateTime.now().toDate().time
        notificationListener?.snoozeNotification(notification.key, time)
    }

    override fun cancelNotification(key: String) {

        if (notificationListener == null) {
            AppLogger.d("notificationListener == null key: $key")
            return
        }

        notificationListener?.cancelNotification(key)
    }

    override fun postSnoozedNotification(key: String) {
        if (notificationListener == null) {
            AppLogger.d("notificationListener == null key: $key")
            return
        }

        notificationListener?.snoozeNotification(key, 500L)
    }

    /**
     * Atualiza os Flows de notificações ativas, adiadas e em andamento om base no conteudo do Listener de notificações.
     */
    override fun emitNotifications() {
        activeFlow.value = getActiveNotifications()
        snoozedFlow.value = getSnoozedNotifications()
        ongoingFlow.value = getOngoingNotifications()
    }

    override fun processNotification(sbn: StatusBarNotification) {

        if (!SystemNotificationValidator.validNotification(sbn)) return

        val snoozedNotification = runBlocking { getSnoozedNotificationByKeyUseCase(sbn.key) }

        if (snoozedNotification != null) processSnoozedNotification(snoozedNotification, sbn)
        else processNotificationRule(sbn)

    }

    private fun processSnoozedNotification(snoozedNot: SnoozedNotification, sbn: StatusBarNotification) = runBlocking {
        // TODO: transformar em usecase?

        val activeNot = ActiveStatusBarNotificationFactory.create(sbn)
        if (snoozedNot.permaHidden) {
            snoozeNotification(
                activeNot,
                System.currentTimeMillis() + SnoozedNotification.DEFAULT_SNOOZED_PERIOD
            )
            return@runBlocking
        }
        /** O app pode atualizar a notificação fazendo com que seja reemitida antes da hora*/
        if (snoozedNotificationPostedTooEarly(snoozedNot)) {
            snoozeNotification(activeNot, snoozedNot.snoozeUntil)
            return@runBlocking
        }
        /**
         * O propósito de adiar uma notificação é permitir que o usuário seja lembrado do seu conteúdo
         * em um momento mais conveniente. Frequentemente, aplicativos como o WhatsApp atualizam
         * notificações, modificando seu conteúdo. Consequentemente, quando chega o momento de
         * reexibir a notificação adiada, seu conteúdo pode ter sido alterado.
         *
         * Ao verificar o conteúdo da notificação, além da sua chave (key), garantimos que o
         * agendamento da notificação de backup seja cancelado somente se o sistema emitir
         * a notificação com o conteúdo original novamente. Isso assegura que o usuário seja
         * lembrado do conteúdo específico que ele desejou ver posteriormente.
         *
         * Caso a notificação seja atualizada – mantendo a mesma chave, mas com conteúdo
         * modificado – o aplicativo não cancelará o agendamento. Em vez disso,
         * emitirá uma notificação de backup contendo o conteúdo da notificação
         * originalmente adiada.
         *
         * Este comportamento torna a funcionalidade de adiar notificações mais útil e confiável
         * para o usuário.
         */
        if (activeNot.title == snoozedNot.title && activeNot.content == snoozedNot.content) {
            if (backupNotificationAlarmSchedulerImpl.isThereAnyAlarmSetForKey(snoozedNot.key)) {
                backupNotificationAlarmSchedulerImpl.cancelAlarm(snoozedNot.key)
                deleteSnoozedNotificationUseCase(snoozedNot.key)
            }
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

        val not = AppNotificationFactory.create(sbn)
        AppLogger.d("notificationListener valido: ${notificationListener != null} not: ${not.title}", not)

        debugTests?.crashIfCallbackNotCalled(sbn)

        val result = processIncomingNotificationUseCase(sbn)

        when (result) {

            is AllowNotification -> {
                AppLogger.d("Processing result: AllowNotification ${not.title}", not)
                debugTests?.cancelCrashIfCallbackNotCalled()
                echoImpl.repostNotification(result.targetNotification)
            }

            is AppNotManaged -> {
                AppLogger.d("Processing result: AppNotManaged ${not.title}", not)
                debugTests?.cancelCrashIfCallbackNotCalled()
                echoImpl.repostNotification(result.targetNotification)
            }

            is CancelNotification -> {
                AppLogger.d("Processing result: CancelNotification ${not.title}", not)
                debugTests?.cancelCrashIfCallbackNotCalled()
                debugTests?.crashIfNotificationDoesNotRemove(result.targetNotification)
                notificationListener?.cancelNotification(result.targetNotification.key)


            }

            is SnoozeNotification -> {
                AppLogger.d("Processing result: SnoozeNotification ${not.title}", not)
                debugTests?.cancelCrashIfCallbackNotCalled()
                debugTests?.crashIfNotificationDoesNotRemove(result.targetNotification)
                runBlocking { snoozeNotificationByRuleUseCase(result.targetNotification, result.until) }
            }
        }


    }

    override fun clearNotificationListenerInstance() {
        AppLogger.d("")
        notificationListener = null
    }

}

