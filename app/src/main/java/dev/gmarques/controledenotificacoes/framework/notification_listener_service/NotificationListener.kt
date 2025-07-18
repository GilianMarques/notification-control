package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationFactory
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
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

/**
 * Criado por Gilian Marques
 * Em sábado, 03 de maio de 2025 as 16:18.
 */
class NotificationListener : NotificationListenerService(), CoroutineScope by MainScope(), RuleEnforcer.Callback {

    private val ruleEnforcer = HiltEntryPoints.ruleEnforcer()
    private val echoImpl = HiltEntryPoints.echo()
    private var cancelingNotificationKey = ""
    private var errorJob: Job? = null
    private var validationCallbackErrorJob: Job? = null

    companion object {
        var instance: NotificationListener? = null
            private set


    }

    fun cancelOngoingNotificationBySnooze(sbn: StatusBarNotification) {
        Log.d("USUK", "NotificationListener.cancelOngoingNotificationBySnooze: $sbn\n\n")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && sbn.isOngoing && sbn.id == 220461) {
            Log.d("USUK", "NotificationListener.cancelOngoingNotificationBySnooze: trying to cancel")
            try {
                val maxTime = 60_000L
                snoozeNotification(sbn.key, maxTime)
                /*   launch {
                       delay(5000)
                       snoozedNotifications.forEach {
                           snoozeNotification(sbn.key, 100L)
                       }
                   }*/
            } catch (e: Exception) {
                Log.e("NotifSnooze", "Erro ao adiar notificação: ${e.message}")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT //https://blog.stackademic.com/exploring-the-notification-listener-service-in-android-7db54d65eca7
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this@NotificationListener
        observeRulesChanges()

        //  activeNotifications.forEach { cancelOngoingNotificationBySnooze(it) } todo    implementar
    }

    /**
     * Observa mudanças nas regras de notificação.
     * Quando uma mudança é detectada (uma regra é adicionada, removida ou atualizada),
     * o mét.odo [evaluateActiveNotifications] é chamado para reavaliar todas as notificações ativas
     * com base nas regras atualizadas. Isso garante que as regras sejam aplicadas dinamicamente.
     */
    private fun observeRulesChanges() = launch(IO) {
        HiltEntryPoints.observeAllRulesUseCase().invoke().collect { rules ->
            evaluateActiveNotifications()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        manageNotification(sbn)
    }

    /**
     * Lê todas as notificações ativas no momento em que o serviço é conectado.
     * Processa cada notificação ativa usando o mét.odo [manageNotification].
     */

    fun evaluateActiveNotifications() {
        val active = activeNotifications ?: return
        active.forEach { sbn ->
            manageNotification(sbn)
        }
    }

    fun getFilteredActiveNotifications(): List<StatusBarNotification> {

        val notifications = mutableListOf<StatusBarNotification>()
        activeNotifications?.let {
            notifications.addAll(it)
        }

        return notifications.filter {
            !it.isOngoing && it.packageName != BuildConfig.APPLICATION_ID
        }

    }

    /**
     * Processa uma notificação recebida.
     * Extrai informações relevantes e as encapsula em um objeto AppNotification.
     * Em seguida, utiliza o RuleEnforcer para aplicar as regras configuradas.
     *
     */
    private fun manageNotification(sbn: StatusBarNotification) {

        if (sbn.isOngoing) return // TODO: tratar isso, agora vai!
        if (sbn.packageName.contains(BuildConfig.APPLICATION_ID)) return

        crashIfCallbackNotCalled()
        // TODO: continuar aqui, valide o fluxo e continue a implementação
        ruleEnforcer.enforceOnNotification(
            ActiveStatusBarNotificationFactory.create(sbn),
            AppNotificationFactory.create(sbn),
            this@NotificationListener
        )

    }

    /**
     * Cancela o temporizador que monitora se o callback de validação da notificação foi chamado.
     * Esta função é usada em conjunto com [crashIfCallbackNotCalled] para garantir que,
     * em builds de debug, o aplicativo falhe se o callback não for invocado dentro de um
     * período esperado. Isso ajuda a identificar problemas onde o RuleEnforcer não está
     * chamando o callback corretamente, (BUG) o que poderia afetar a função de eco.
     */
    private fun cancelValidationCallbackTimer() = validationCallbackErrorJob?.cancel()

    /**
     * Inicia um temporizador que, se não for cancelado a tempo, causará uma falha no aplicativo.
     * Esta função é destinada a evitar bugs. Ela garante que alterações no RuleEnforcer
     * não impeçam que o callback seja chamado nos casos onde:
     * - A notificação deve ser bloqueada.
     * - A notificação não deve ser bloqueada.
     * - O aplicativo não é gerenciado.
     * Isso serve para impedir que bugs sejam introduzidos no código.
     *
     * @see  cancelValidationCallbackTimer
     * @see manageNotification
     */
    private fun crashIfCallbackNotCalled() {
        validationCallbackErrorJob = CoroutineScope(Main).launch {
            delay(3000)
            error("O callback de validação passado para o RuleEnforcer não foi chamado.")
        }
    }

    /**
     * Essa função serve pra testes apenas e nao sera usada em produção.
     * Caso alguma alteraçao que impeça o bloqueio das notificações seja feita (como ja foi feita antes...)
     * essa função vai crashar o app para que o jumento do desenvolvedor (eu :-] ) possa ajeitar a cagada que ele fez
     */
    private fun crashIfNotificationDoesNotRemoveInDebugBuild(activeNotification: ActiveStatusBarNotification) {
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

    /**
     * Ajuda  a  [crashIfNotificationDoesNotRemoveInDebugBuild] a determinar se a notificação foi de fato cancelada
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {

        if (BuildConfig.DEBUG) {
            if (sbn?.key == cancelingNotificationKey) errorJob?.cancel()
        }

        super.onNotificationRemoved(sbn, rankingMap)
    }

    override fun onListenerDisconnected() {
        instance = null
        cancel()
        super.onListenerDisconnected()
    }

    /** Callback do [RuleEnforcer]*/
    override fun cancelNotification(
        activeNotification: ActiveStatusBarNotification,
        appNotification: AppNotification,
        rule: Rule,
        managedApp: ManagedApp,
    ) {
        Log.d("USUK", "NotificationListener.cancelNotification: ${activeNotification.packageId} ")
        cancelValidationCallbackTimer()
        crashIfNotificationDoesNotRemoveInDebugBuild(activeNotification)
        cancelNotification(activeNotification.key)
    }

    /** Callback do [RuleEnforcer]*/
    override fun snoozeNotification(activeNotification: ActiveStatusBarNotification, snoozePeriod: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) error("Essa função nao deve estar disponivel em versões anteriores ao Oreo")

        Log.d("USUK", "NotificationListener.snoozeNotification: ${activeNotification.packageId} ")
        cancelValidationCallbackTimer()
        crashIfNotificationDoesNotRemoveInDebugBuild(activeNotification)
        snoozeNotification(activeNotification.key, snoozePeriod) // TODO: testar isso! 

    }

    /** Callback do [RuleEnforcer]*/
    override fun appNotManaged(activeNotification: ActiveStatusBarNotification) {
        Log.d("USUK", "NotificationListener.appNotManaged: ${activeNotification.packageId}")
        cancelValidationCallbackTimer()
        echoImpl.repostIfNotification(activeNotification)
    }

    /** Callback do [RuleEnforcer]*/
    override fun allowNotification(activeNotification: ActiveStatusBarNotification) {
        Log.d("USUK", "NotificationListener.allowNotification: ${activeNotification.packageId}")
        cancelValidationCallbackTimer()
        echoImpl.repostIfNotification(activeNotification)
    }

}

