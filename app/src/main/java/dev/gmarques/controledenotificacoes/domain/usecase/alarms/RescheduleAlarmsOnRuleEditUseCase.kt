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

package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.ReportNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppsByRuleIdUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 19 de maio de 2025 as 13:14.
 */
class RescheduleAlarmsOnRuleEditUseCase @Inject constructor(
    private val reportNotificationAlarmScheduler: ReportNotificationAlarmScheduler,
    private val getManagedAppsByRuleIdUseCase: GetManagedAppsByRuleIdUseCase,
    private val scheduleAlarmForAppUseCase: ScheduleAlarmForAppUseCase,
) {

    /**
     * Reagenda os alarmes para todos os aplicativos gerenciados pela regra editada.
     */
    suspend operator fun invoke(rule: Rule) = withContext(IO) {

        getManagedAppsByRuleIdUseCase(rule.id)
            .map { app ->
                async {
                    app?.let { reschedule(app, rule) }
                }
            }.awaitAll()
    }

    /**
     * Reagenda um alarme para um aplicativo específico com base na regra fornecida.
     * Se já existir um alarme ativo para o aplicativo, ele será cancelado e um novo será agendado.
     */
    private fun reschedule(
        app: ManagedApp,
        rule: Rule,
    ) {
        val isThereAnyActiveAlarm = reportNotificationAlarmScheduler.isThereAnyAlarmSetForPackage(app.packageName)

        if (isThereAnyActiveAlarm) {
            reportNotificationAlarmScheduler.cancelAlarm(app.packageName)
            scheduleAlarmForAppUseCase(app, rule)
        }
    }


}