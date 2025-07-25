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
import dev.gmarques.controledenotificacoes.domain.framework.contracts.AlarmScheduler

/**
 * É executado mediante agendamento no sistema para  emitir notificações ao usuario.
 * Usa [ReportNotificationManager] para exibir a notificação de relatório e [AlarmScheduler]
 * para limpar os dados de agendamento após a emissao das notificações.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val PACKAGE_ID = "packageName"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pkg = intent.getStringExtra(PACKAGE_ID) ?: return
        // Log.d("USUK", "AlarmReceiver.onReceive: alarm received for $pkg")

        getReportNotificationManager().showReportNotification(pkg)

        clearPreferenceForPackage(pkg)
    }

    private fun getReportNotificationManager(): ReportNotificationManager {
        return HiltEntryPoints.reportNotificationManager()
    }

    /**
     *Remove das preferências ou o nome de pacote do aplicativo que acabou de ter a notificação de relatório exibida garantindo
     * que os registros nas preferências  estejam sempre atualizados em relação aos alarmes agendados no sistema e prevenindo que
     * um alarme que já foi disparado seja reagendado por acidente causando inconsistências.
     *
     * @param pkg O nome do pacote do aplicativo cujos dados de agendamento devem ser limpos.
     */
    private fun clearPreferenceForPackage(pkg: String) {

        val scheduleManager = HiltEntryPoints.scheduleManager()

        scheduleManager.deleteScheduleData(pkg)
    }
}