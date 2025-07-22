package dev.gmarques.controledenotificacoes.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.usecase.alarms.ScheduleAutoTurnOnUseCase

/**
 * Criado por Gilian Marques
 * Em 05/07/2025 as 19:22
 *
 * Responsavel por, de tempos em tempos, ligar o serviço caso seja fechado por erro ou sistema.
 *
 * Após ligar o serviço, reagenda um alarme para reabrir este receiver em um outro intervalo de tempo futuro definido em
 * [ScheduleAutoTurnOnUseCase] criando um loop nifinito que é
 * executado de tempos em tempos.
 */
class AutoTurnOnReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // App.instance.startNotificationService() // nao é necessario fazer a chamada pq a classe app ja faz isso ao ser aberta.
        HiltEntryPoints.scheduleAutoTurnOnUseCase().invoke()
    }

}