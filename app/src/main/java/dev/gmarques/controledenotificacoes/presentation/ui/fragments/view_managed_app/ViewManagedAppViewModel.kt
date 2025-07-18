package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.CancelAlarmForAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.DeleteAllAppNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.ObserveAppNotificationsByPkgIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppByPackageOrDefaultUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.DeleteManagedAppAndItsNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.ObserveManagedApp
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.DeleteRuleWithAppsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveRuleUseCase
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class ViewManagedAppViewModel @Inject constructor(
    private val observeRuleUseCase: ObserveRuleUseCase,
    private val deleteManagedAppAndItsNotificationsUseCase: DeleteManagedAppAndItsNotificationsUseCase,
    private val deleteRuleWithAppsUseCase: DeleteRuleWithAppsUseCase,
    private val observeAppNotificationsByPkgIdUseCase: ObserveAppNotificationsByPkgIdUseCase,
    private val deleteAllAppNotificationsUseCase: DeleteAllAppNotificationsUseCase,
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val updateManagedAppUseCase: UpdateManagedAppUseCase,
    private val getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
    private val getInstalledAppByPackageOrDefaultUseCase: GetInstalledAppByPackageOrDefaultUseCase,
    private val observeManagedApp: ObserveManagedApp,
    private val cancelAlarmForAppUseCase: CancelAlarmForAppUseCase,
) : ViewModel() {


    private var initialized = false

    /**indica que o app gerenciado recebido não existe na lista de apps instalados.*/
    var notFoundApp = false
        private set

    private val _managedAppFlow = MutableStateFlow<ManagedAppWithRule?>(null)
    val managedAppFlow: StateFlow<ManagedAppWithRule?> get() = _managedAppFlow

    private val _appNotificationHistoryFlow = MutableStateFlow<List<AppNotification>>(emptyList<AppNotification>())
    val appNotificationHistoryFlow: StateFlow<List<AppNotification>> get() = _appNotificationHistoryFlow

    private val _eventsFlow = MutableSharedFlow<Event>(replay = 1)
    val eventsFlow: SharedFlow<Event> get() = _eventsFlow

    private var ruleObserverJob: Job? = null

    fun setup(pkg: String) = viewModelScope.launch(IO) {

        val managedApp = getManagedAppByPackageIdUseCase(pkg)

        val ruleId = managedApp?.ruleId
        if (ruleId == null) return@launch

        val rule = getRuleByIdUseCase(ruleId)
        if (rule == null) return@launch

        val installedApp = getInstalledAppByPackageOrDefaultUseCase(pkg)

        setup(ManagedAppWithRule.from(installedApp, managedApp, rule))
    }

    fun setup(app: ManagedAppWithRule) = viewModelScope.launch(IO) {

        if (!initialized) {
            initialized = true

            observeAppChanges(app.packageName)
            observeAppNotifications(app)
        }

        notFoundApp = app.uninstalled
        _managedAppFlow.tryEmit(app)

    }

    /**
     * Necessário observar alterações  no app vindas do DB porque o usuario pode alterar a regra de um app
     * e se acontecer é necessário redefinir o listener que observa a regra para observar a nova regra do app
     */
    private fun observeAppChanges(pkg: String) = viewModelScope.launch(IO) {
        observeManagedApp(pkg).collect {
            //   Log.d("USUK", "ViewManagedAppViewModel.observeAppChanges: $it")

            if (it == null) _eventsFlow.tryEmit(Event.AppRemoved)
            else observeRuleChanges(it.ruleId)
        }
    }

    /**
     * Quando o usuário usa o menu para editar ou alterar uma regra,este listener é disparado para
     * atualizar a interface deste fragmento com a nova regra.
     *
     * Pode ser chamado multiplas vezes pois ecerra a corrotina anterior e cria uma nova.
     */
    private fun observeRuleChanges(ruleId: String) {
        ruleObserverJob?.cancel()
        ruleObserverJob = viewModelScope.launch(IO) {
            observeRuleUseCase(ruleId).collect {
                // a regra sera nula se o usuario a remover, nesse caso o fragmento será fechado
                // Log.d("USUK", "ViewManagedAppViewModel.observeRuleChanges: $it")
                it?.let { _managedAppFlow.emit(_managedAppFlow.value?.copy(rule = it)) }
            }
        }
    }

    private fun observeAppNotifications(app: ManagedAppWithRule) = viewModelScope.launch(IO) {
        observeAppNotificationsByPkgIdUseCase(app.packageName).collect {

            val notifications = it.toMutableList()
                .apply {
                    reverse() // notificações mais recentes por cima
                }.distinctBy { appNotification ->
                    appNotification.title to appNotification.content
                } // remove duplicatas (Impedir duplicatas de entrar no DB nao é viavel).

            _appNotificationHistoryFlow.tryEmit(notifications)
        }
    }

    /**
     * Marca as notificações  como lidas para o app gerenciado e cancela a notificação de relatório
     * que alertaria sobre pendências.
     *
     * Deve ser chamado quando:
     * - O fragmento for aberto ou recriado (usuário está visualizando as notificações).
     *
     * Só atua quando o fragmento abrir, esse comportamento nao é um bug, serve pra evitar que
     * a falta de atençao do usuario o faça perder notificaçoes pedentes
     */

    fun markNotificationsAsRead() = viewModelScope.launch(IO) {

        delay(1500) // pra "dar tempo do usuario ler as notificações"

        val packageName = _managedAppFlow.value?.packageName

        packageName?.let {
            getManagedAppByPackageIdUseCase(packageName)
                ?.let { app ->
                    updateManagedAppUseCase(app.copy(hasPendingNotifications = false))
                    cancelAlarmForAppUseCase(app.packageName)
                }
        }
    }


    fun deleteApp() = viewModelScope.launch {
        deleteManagedAppAndItsNotificationsUseCase(_managedAppFlow.value!!.packageName)
        _eventsFlow.tryEmit(Event.FinishWithSuccess)
    }

    fun deleteRule() = viewModelScope.launch {
        deleteRuleWithAppsUseCase(_managedAppFlow.value!!.rule)
        _eventsFlow.tryEmit(Event.FinishWithSuccess)
    }

    fun clearHistory() = viewModelScope.launch {
        deleteAllAppNotificationsUseCase(managedAppFlow.value!!.packageName)
    }

    fun loadAppIcon(pkg: String, context: Context): Drawable = runBlocking {
        return@runBlocking getInstalledAppIconUseCase(pkg) ?: ContextCompat.getDrawable(context, R.drawable.vec_app)
    }

    /**
     * Atualiza a regra do app no DB.
     */
    fun updateAppsRule(newRule: Rule) = viewModelScope.launch {

        _managedAppFlow.value?.let {
            getManagedAppByPackageIdUseCase(it.packageName)?.let { app ->
                updateManagedAppUseCase(app.copy(ruleId = newRule.id))
                NotificationListener.instance?.evaluateActiveNotifications()
            }
        }
    }

}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    object FinishWithSuccess : Event()
    object AppRemoved : Event()
}
