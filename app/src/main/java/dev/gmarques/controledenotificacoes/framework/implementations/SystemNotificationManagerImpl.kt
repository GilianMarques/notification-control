package dev.gmarques.controledenotificacoes.framework.implementations

import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.joda.time.LocalDateTime

/**
 * Criado por Gilian Marques
 * Em domingo, 27 de julho de 2025 as 15:15.
 * Permite obter e gerenciar as notificações disponiveis no sistema
 *
 *  Obtenha uma instancia dessa classe atraves de [NotificationListener.get] ou [NotificationListener.getWhenReady]
 */
class SystemNotificationManagerImpl(private var listener: NotificationListener, private val debugTests: DebugTests?) :
    SystemNotificationManager {

    private val echoImpl = HiltEntryPoints.echo()
    private val processIncomingNotificationUseCase = HiltEntryPoints.processIncomingNotificationUseCase()
    private val getSnoozedNotificationByKeyUseCase = HiltEntryPoints.getGetSnoozedNotificationByKeyUseCase()

    private val activeFlow = MutableStateFlow<List<ActiveStatusBarNotification>>(emptyList())
    private val snoozedFlow = MutableStateFlow<List<ActiveStatusBarNotification>>(emptyList())
    private val ongoingFlow = MutableStateFlow<List<ActiveStatusBarNotification>>(emptyList())

    override fun getActiveNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = activeFlow

    override fun getActiveNotifications(): List<ActiveStatusBarNotification> {
        if (!listener.isListenerConnected()) return emptyList()
        return listener.activeNotifications?.filter {
            SystemNotificationValidator.isValidToProcess(it)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        } ?: emptyList()
    }

    override fun getOngoingNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = ongoingFlow

    override fun getOngoingNotifications(): List<ActiveStatusBarNotification> {
        if (!listener.isListenerConnected()) return emptyList()
        return listener.activeNotifications?.filter {
            it.isOngoing && SystemNotificationValidator.isValidToProcess(it, true)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        } ?: emptyList()
    }

    override fun getSnoozedNotificationsFlow(): Flow<List<ActiveStatusBarNotification>> = snoozedFlow

    override fun getSnoozedNotifications(): List<ActiveStatusBarNotification> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !listener.isListenerConnected()) return emptyList()
        return listener.snoozedNotifications?.filter {
            SystemNotificationValidator.isValidToProcess(it)
        }?.map {
            ActiveStatusBarNotificationFactory.create(it)
        } ?: emptyList()
    }

    override fun processActiveNotifications() {
        if (!listener.isListenerConnected()) return
        listener.activeNotifications?.forEach { processNotification(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun snoozeNotification(notification: ActiveStatusBarNotification, until: Long) {
        val time = LocalDateTime(until).toDate().time - LocalDateTime.now().toDate().time
        listener.snoozeNotification(notification.key, time)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun postSnoozedNotification(notification: ActiveStatusBarNotification) {
        listener.snoozeNotification(notification.key, 500L)
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

        // TODO: ver se é permahidden, ver sem tem alarme agendado pra ela, remover do db
// TODO: continuar aqui
        if (snoozedNotification.permaHidden) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) snoozeNotification(
                ActiveStatusBarNotificationFactory.create(sbn),
                System.currentTimeMillis() + SnoozedNotification.defaultSnoozePeriod
            )
            return@runBlocking
        }


    }

    /**
     * Processa uma notificação recebida pra saber se ela sera cancelada, adiada, permitida, ecoada, etc...
     * Ao fim do processamento executa a ação necessario com base na regra, caso exista uma.
     * Usa o [ProcessIncomingNotificationUseCase]
     * para processar os dados e salvar a notificação se necessario.
     */
    fun processNotificationRule(sbn: StatusBarNotification) {

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
                listener.cancelNotification(result.targetNotification.key)
            }

            is SnoozeNotification -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) error("Essa função nao deve ser chamada em versões anteriores ao Oreo")
                debugTests?.cancelCrashIfCallbackNotCalled()
                debugTests?.crashIfNotificationDoesNotRemove(result.targetNotification)
                listener.snoozeNotification(result.targetNotification.key, result.snoozeFor)
            }
        }


    }
}
