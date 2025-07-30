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

package dev.gmarques.controledenotificacoes.framework.report_notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.RescheduleReportNotificationsOnBootUseCase
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 14 de maio de 2025 as 12:34.
 *
 *  Liga o [NotificationServiceManager] para que o [NotificationListener]
 *  seja monitorado em caso de desconexão, Também é responsável por, através do [RescheduleReportNotificationsOnBootUseCase] reagendar os
 *  alarmes que estavam ativos antes do dispositivo reiniciar
 */
class BootReceiver : BroadcastReceiver(), CoroutineScope by MainScope() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAlarmsIfAny()
        }
    }

    private fun rescheduleAlarmsIfAny() = launch(IO) {
        with(HiltEntryPoints) {
            rescheduleReportNotificationsOnBootUseCase().invoke()
            scheduleBackupNotificationsOnBootUseCase().invoke()
        }
    }


}
