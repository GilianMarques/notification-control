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

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleAlarmsOnBootUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.ScheduleAutoTurnOnUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.IsRuleInBlockPeriodUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.NextRuleUnlockTimeUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveAllRulesUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.DeleteSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.GetSnoozedNotificationByKeyUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.SnoozeNotificationByRuleUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UseCasesEntryPoint {
    fun getAppUserUseCase(): GetUserUseCase
    fun rescheduleAlarmsOnBootUseCase(): RescheduleAlarmsOnBootUseCase
    fun nextAppUnlockUseCase(): NextRuleUnlockTimeUseCase
    fun isAppInBlockPeriodUseCase(): IsRuleInBlockPeriodUseCase
    fun generateRuleNameUseCase(): GenerateRuleDescriptionUseCase
    fun updateManagedAppUseCase(): UpdateManagedAppUseCase
    fun insertAppNotificationUseCase(): InsertAppNotificationUseCase
    fun observeAllRulesUseCase(): ObserveAllRulesUseCase
    fun scheduleAutoTurnOnUseCase(): ScheduleAutoTurnOnUseCase
    fun processIncomingNotificationUseCase(): ProcessIncomingNotificationUseCase
    fun getGetSnoozedNotificationByKeyUseCase(): GetSnoozedNotificationByKeyUseCase
    fun getDeleteSnoozedNotificationUseCase(): DeleteSnoozedNotificationUseCase
    fun getSnoozeNotificationByRuleUseCase(): SnoozeNotificationByRuleUseCase
}