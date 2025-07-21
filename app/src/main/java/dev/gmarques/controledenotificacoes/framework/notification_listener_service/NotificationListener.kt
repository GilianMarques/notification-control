package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.framework.SystemNotificationValidator
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.AllowNotification
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.AppNotManaged
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.CancelNotification
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase.ProcessingResult.SnoozeNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener.Companion.processActiveNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 */
class NotificationListener : NotificationListenerService(), CoroutineScope by MainScope() {

    private val echoImpl = HiltEntryPoints.echo()

    private val processIncomingNotificationUseCase = HiltEntryPoints.processIncomingNotificationUseCase()

    private var debugTests: DebugTests? = null

    companion object {
        /**Ao nao expor a instancia publicamente eu consigo garantir que asm notificações extraidas daqui seguem as regras de negocio.*/
        private var instance: NotificationListener? = null

        fun getActiveNotifications(): List<StatusBarNotification> {
            val notifications = mutableListOf<StatusBarNotification>()

            instance?.activeNotifications?.let {
                notifications.addAll(it)
            }

            return notifications.filter {
                SystemNotificationValidator.isValidToProcess(it)
            }
        }

        /**
         * Processa toas as notificaçõesa tivas validas  usando o mét.odo [processNotification].
         */
        fun processActiveNotifications() {
            val active = instance?.activeNotifications ?: return
            active.forEach { sbn ->
                instance?.processNotification(sbn)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (BuildConfig.DEBUG) debugTests = DebugTests()
        instance = this@NotificationListener
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
     * Processa uma notificação recebida pra sabr se ela sera cancelada, adiada, permitida, ecoada, etc...
     * Ao fim do processamento executa a ação necessario com base na regra, caso exista uma.
     * Usa o [dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase]
     * para processar os dados e salvar a notificação se necessario.
     */
    private fun processNotification(sbn: StatusBarNotification) {

        if (!SystemNotificationValidator.isValidToProcess(sbn)) return

        debugTests?.crashIfCallbackNotCalled()

        val result = processIncomingNotificationUseCase(sbn)

        when (result) {
            is AllowNotification -> {
                Log.d("USUK", "NotificationListener.processNotification: AllowNotification: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                echoImpl.repostIfNotification(result.targetNotification)
            }

            is AppNotManaged -> {
                Log.d("USUK", "NotificationListener.processNotification: AppNotManaged: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                echoImpl.repostIfNotification(result.targetNotification)
            }

            is CancelNotification -> {
                Log.d("USUK", "NotificationListener.processNotification: CancelNotification: ${result.targetNotification} ")
                debugTests?.cancelCrashIfCallbackNotCalled()
                debugTests?.crashIfNotificationDoesNotRemove(result.targetNotification)
                cancelNotification(result.targetNotification.key)
            }

            is SnoozeNotification -> {
                Log.d("USUK", "NotificationListener.processNotification: SnoozeNotification: snoozeFor: ${result.snoozeFor} not: ${result.targetNotification} ")
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
        instance = null
        cancel()
        super.onListenerDisconnected()
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
         *Esta função é usada para garantir que em builds de debug, o aplicativo falhe se o callback não for invocado dentro de um
         * período esperado. Isso ajuda a identificar bugs no processamento da notificação.
         * @see processNotification
         */
        fun crashIfCallbackNotCalled() {
            validationCallbackErrorJob = CoroutineScope(Main).launch {
                delay(3000)
                error("O callback de validação passado para o RuleEnforcer não foi chamado.")
            }
        }

        fun cancelCrashIfCallbackNotCalled() {
            validationCallbackErrorJob?.cancel()
        }

        /**
         * Essa função serve pra testes apenas e nao sera usada em produção.
         * Caso alguma alteraçao que impeça o bloqueio das notificações seja feita (como ja foi feita antes...)
         * essa função vai crashar o app para que o jumento do desenvolvedor (eu :-] ) possa ajeitar a cagada que ele fez
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
}

