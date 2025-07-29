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

package dev.gmarques.controledenotificacoes.di.entry_points

import dagger.hilt.android.EntryPointAccessors
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnBootUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.ScheduleAutoTurnOnUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.ReadPreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.IsRuleInBlockPeriodUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.NextRuleUnlockTimeUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveAllRulesUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.DeleteSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.GetSnoozedNotificationByKeyUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.SnoozeNotificationByRuleUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.framework.backup_notification.BackupNotificationManager
import dev.gmarques.controledenotificacoes.framework.implementations.BackupNotificationAlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.implementations.EchoImpl
import dev.gmarques.controledenotificacoes.framework.implementations.ReportNotificationAlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.report_notification.ReportNotificationManager

/**
 * Criado por Gilian Marques
 * Em sábado, 24 de maio de 2025 as 16:28.
 */
object HiltEntryPoints : FrameworkEntryPoint, UseCasesEntryPoint {

    /**
     * Recupera uma instância de um EntryPoint Hilt a partir do contexto da aplicação.
     *
     * Esta função é genérica e pode ser utilizada para acessar qualquer interface de
     * EntryPoint previamente definida, como por exemplo `UseCasesEntryPoint`,
     * `RuleEnforcerEntryPoint`, `ScheduleManagerEntryPoint`, etc.
     *
     * O parâmetro genérico [T] é automaticamente inferido no momento da chamada,
     * dispensando a necessidade de passar a classe explicitamente.
     *
     * Esta função simplifica o acesso ao mét.odo `EntryPointAccessors.fromApplication(...)`
     * evitando repetição de código e melhorando a legibilidade.
     *
     * @return A instância do EntryPoint correspondente ao tipo [T].
     */
    private inline fun <reified T> entryPoint(): T {
        return EntryPointAccessors.fromApplication(App.instance, T::class.java)
    }

    override fun reportNotificationManager(): ReportNotificationManager {
        return entryPoint<FrameworkEntryPoint>().reportNotificationManager()
    }

    override fun backupNotificationManager(): BackupNotificationManager {
        return entryPoint<FrameworkEntryPoint>().backupNotificationManager()
    }

    override fun reportNotificationScheduleManager(): ReportNotificationAlarmSchedulerImpl {
        return entryPoint<FrameworkEntryPoint>().reportNotificationScheduleManager()
    }

    override fun snoozedNotificationScheduleManager(): BackupNotificationAlarmSchedulerImpl {
        return entryPoint<FrameworkEntryPoint>().snoozedNotificationScheduleManager()
    }

    override fun readPreferenceUseCase(): ReadPreferenceUseCase {
        return entryPoint<FrameworkEntryPoint>().readPreferenceUseCase()
    }

    override fun savePreferenceUseCase(): SavePreferenceUseCase {
        return entryPoint<FrameworkEntryPoint>().savePreferenceUseCase()
    }

    override fun echo(): EchoImpl {
        return entryPoint<FrameworkEntryPoint>().echo()
    }

    override fun getAppUserUseCase(): GetUserUseCase {
        return entryPoint<UseCasesEntryPoint>().getAppUserUseCase()
    }

    override fun rescheduleAlarmsOnBootUseCase(): RescheduleAlarmsOnBootUseCase {
        return entryPoint<UseCasesEntryPoint>().rescheduleAlarmsOnBootUseCase()
    }

    override fun nextAppUnlockUseCase(): NextRuleUnlockTimeUseCase {
        return entryPoint<UseCasesEntryPoint>().nextAppUnlockUseCase()
    }

    override fun isAppInBlockPeriodUseCase(): IsRuleInBlockPeriodUseCase {
        return entryPoint<UseCasesEntryPoint>().isAppInBlockPeriodUseCase()
    }

    override fun generateRuleNameUseCase(): GenerateRuleDescriptionUseCase {
        return entryPoint<UseCasesEntryPoint>().generateRuleNameUseCase()
    }

    override fun updateManagedAppUseCase(): UpdateManagedAppUseCase {
        return entryPoint<UseCasesEntryPoint>().updateManagedAppUseCase()
    }

    override fun insertAppNotificationUseCase(): InsertAppNotificationUseCase {
        return entryPoint<UseCasesEntryPoint>().insertAppNotificationUseCase()
    }

    override fun observeAllRulesUseCase(): ObserveAllRulesUseCase {
        return entryPoint<UseCasesEntryPoint>().observeAllRulesUseCase()
    }

    override fun scheduleAutoTurnOnUseCase(): ScheduleAutoTurnOnUseCase {
        return entryPoint<UseCasesEntryPoint>().scheduleAutoTurnOnUseCase()
    }

    override fun processIncomingNotificationUseCase(): ProcessIncomingNotificationUseCase {
        return entryPoint<UseCasesEntryPoint>().processIncomingNotificationUseCase()
    }

    override fun getGetSnoozedNotificationByKeyUseCase(): GetSnoozedNotificationByKeyUseCase {
        return entryPoint<UseCasesEntryPoint>().getGetSnoozedNotificationByKeyUseCase()
    }

    override fun getDeleteSnoozedNotificationUseCase(): DeleteSnoozedNotificationUseCase {
        return entryPoint<UseCasesEntryPoint>().getDeleteSnoozedNotificationUseCase()
    }

    override fun getSnoozeNotificationByRuleUseCase(): SnoozeNotificationByRuleUseCase {
        return entryPoint<UseCasesEntryPoint>().getSnoozeNotificationByRuleUseCase()
    }

}