package dev.gmarques.controledenotificacoes.domain.usecase.framework

import android.content.Context
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 18 de julho de 2025 as 16:39.
 *
 * Processa uma notificação para determinar se ela deve ser permitada ou bloqueada e executa as ações relacionadas
 * ao processo como manter historico, fazer cache de bitmap, agendar alarme, etc...
 *
 *
 */
class ProcessIncomingNotificationUseCase @Inject constructor(
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val updateManagedAppUseCase: dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase,
    private val insertAppNotificationUseCase: InsertAppNotificationUseCase,
    @ApplicationContext private val context: Context,
) {
// TODO: continuar conversao https://chatgpt.com/share/687ab1a3-f120-8006-822a-6be7488b92df

    operator fun invoke(targetNotification: StatusBarNotification) = runBlocking<Unit> {

        val managedApp = getManagedAppByPackageIdUseCase(targetNotification.packageName)

        if (managedApp == null) {
            Log.d("USUK", "ProcessIncomingNotificationUseCase.invoke: ")
            //callback.onAppNotManaged(activeNotification)
            return@runBlocking
        }

        val rule = getRuleByIdUseCase(managedApp.ruleId)
            ?: error("Um app gerenciado deve ter uma regra. Isso é um Bug $managedApp")


        val condition = rule.condition
        val appInBlockPeriod = rule.isAppInBlockPeriod()


    }
}