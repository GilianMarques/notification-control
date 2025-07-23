package dev.gmarques.controledenotificacoes.presentation.ui.fragments.add_managed_apps


import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmOnAppsRuleChangeUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.framework.PostAppSnoozedNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppByPackageOrDefaultUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.AddManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetAllRulesUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddManagedAppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addManagedAppUseCase: AddManagedAppUseCase,
    private val rescheduleAlarmOnAppsRuleChangeUseCase: RescheduleAlarmOnAppsRuleChangeUseCase,
    private val getInstalledAppByPackageOrDefaultUseCase: GetInstalledAppByPackageOrDefaultUseCase,
    private val getAllRulesUseCase: GetAllRulesUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
    private val postAppSnoozedNotificationsUseCase: PostAppSnoozedNotificationsUseCase,
) : ViewModel() {


    private val _selectedApps = MutableStateFlow<Map<String, InstalledApp>>(emptyMap())
    val selectedApps: Flow<Map<String, InstalledApp>> = _selectedApps

    private val _selectedRule = MutableStateFlow<Rule?>(null)
    val selectedRule: Flow<Rule?> = _selectedRule

    private val _eventsChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow: Flow<Event> get() = _eventsChannel.receiveAsFlow()


    fun addNewlySelectedApps(apps: List<InstalledApp>) {
        _selectedApps.value = _selectedApps.value.values.toMutableList().apply {
            addAll(apps)
        }.associateBy { it.packageName }
    }

    fun setRule(rule: Rule?) = viewModelScope.launch(Main) {
        _selectedRule.value = rule
        rule?.let { PreferencesImpl.lastSelectedRule.set(rule.id) }
    }

    fun getSelectedPackages(): Array<String> {
        return _selectedApps.value.values.map { it.packageName }.toTypedArray()
    }

    /**
     * Remove um aplicativo da lista de aplicativos atualmente selecionados.
     *
     * A função recebe um objeto [InstalledApp] representando o aplicativo a ser removido.
     * Ela localiza o aplicativo na lista usando o packageName do aplicativo e o remove.
     * A remoção atualiza o [Flow] `_selectedApps`, que notifica os observadores
     * sobre a mudança na lista de aplicativos selecionados.
     *
     * @param app O [InstalledApp] a ser removido da lista de aplicativos selecionados.
     */
    fun deleteApp(app: InstalledApp) {
        _selectedApps.value = _selectedApps.value.toMutableMap().apply { remove(app.packageName) }
    }

    /**
     * Valida a seleção atual de aplicativos e regra.
     *
     * Esta função verifica se uma regra foi selecionada e se pelo menos um aplicativo foi selecionado.
     * Se a regra ou nenhum aplicativo foi selecionado, um erro correspondente é exibido ao usuário.
     * Caso contrário, adiciona os aplicativos selecionados à lista de aplicativos gerenciados
     * associados à regra selecionada.
     * Após a adição bem-sucedida, notifica os observadores para fechar o fragmento.
     *
     * Esta operação é realizada em um [viewModelScope] e no [Main] dispatcher para garantir
     * que as atualizações da UI ocorram na thread principal.
     */
    fun validateSelection() = viewModelScope.launch(Main) {

        val rule = _selectedRule.value
        val apps = _selectedApps.value.values.toList()

        if (rule == null) {
            _eventsChannel.trySend(Event.Error(context.getString(R.string.Selecione_uma_regra)))
            return@launch
        }

        if (apps.isEmpty()) {
            _eventsChannel.trySend(Event.Error(context.getString(R.string.Selecione_pelo_menos_um_aplicativo)))
            return@launch
        }

        apps.map {
            async {
                val managedApp = ManagedApp(
                    packageName = it.packageName,
                    ruleId = rule.id,
                    hasPendingNotifications = false
                )
                addManagedApp(managedApp)
                rescheduleAlarm(it, managedApp, rule)
            }
        }.awaitAll()

        requestActiveNotificationsEvaluation()

        _eventsChannel.trySend(Event.SuccessCloseFrag)

    }

    /**
     * Reagenda o alarme para um aplicativo, se necessário.
     *
     * Esta função verifica se o aplicativo instalado já está sendo gerenciado.
     * Se estiver, ela usa o [rescheduleAlarmOnAppsRuleChangeUseCase] para verificar se tem algum alarme agendado para mostrar a
     * notificação de relatorio de notificações recebidas durante bloqueio e reagendar esse alarme (se houver) considerando
     * a nova regra do aplicativo.
     *
     * @param installedApp O [InstalledApp] que está sendo verificado.
     * @param managedApp O [ManagedApp] representando o aplicativo com a nova regra. (é um objeto contendo as mesmas informações do [InstalledApp] só por conveniência)
     * @param rule A nova regra [Rule] que foi aplicada ao aplicativo.
     *
     */
    private suspend fun rescheduleAlarm(
        installedApp: InstalledApp,
        managedApp: ManagedApp,
        rule: Rule,
    ) {
        if (!installedApp.isBeingManaged) return
        rescheduleAlarmOnAppsRuleChangeUseCase(managedApp, rule)
    }

    /**
     * Solicita a reavaliação das notificações ativas.
     *
     * Esta função invoca o mét.odo `reEvaluateActiveNotifications` na instância do [NotificationListener].
     * Isso é útil quando as regras de notificação ou apps gerenciados são alteradas e as notificações ativas precisam ser
     * reavaliadas para aplicar as novas regras imediatamente.
     *
     * Se a instância do [NotificationListener] não estiver disponível, a função não faz nada.
     */
    private fun requestActiveNotificationsEvaluation() {
        NotificationListener.instance().processActiveNotifications()
    }

    /**
     * Adiciona um aplicativo gerenciado ao armazenamento de dados.
     *
     * @param app O objeto [ManagedApp] a ser adicionado.
     *
     * Esta função suspensa chama o [addManagedAppUseCase] para adicionar o aplicativo gerenciado.
     */
    private suspend fun addManagedApp(app: ManagedApp) {
        addManagedAppUseCase(app)
        /*Como da pra atualizar um app que ja esta sendo gerenciado por aqui é necessário
        * repostar as notificaçoes adiadas caso haja alguma*/
        postAppSnoozedNotificationsUseCase(app)
    }

    fun addSelectedAppByPkgId(packageName: String) = viewModelScope.launch {
        val installedApp = getInstalledAppByPackageOrDefaultUseCase(packageName)

        if (installedApp.uninstalled) {
            _eventsChannel.trySend(Event.Error(context.getString(R.string.O_aplicativo_n_o_pode_ser_selecionado)))
            return@launch
        }

        addNewlySelectedApps(listOf(installedApp))
    }

    suspend fun getAllRules(): List<Rule> {
        return getAllRulesUseCase()
    }

    suspend fun getRuleById(id: String): Rule? {
        return getRuleByIdUseCase(id)
    }

    suspend fun getInstalledAppIcon(packageName: String): Drawable? {
        return getInstalledAppIconUseCase(packageName)
    }

    fun getSelectedRule(): Rule? = _selectedRule.value
    fun getSelectedApps(): Map<String, InstalledApp> = _selectedApps.value


}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    data class Error(val msg: String) : Event()
    object SuccessCloseFrag : Event()
}