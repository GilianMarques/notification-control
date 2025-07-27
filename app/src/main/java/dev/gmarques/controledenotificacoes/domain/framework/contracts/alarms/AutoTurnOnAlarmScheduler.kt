package dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 16 de maio de 2025 as 11:00.
 *
 * Gerencia o agendamento do alarme responsável por reativar o serviço de escuta de notificações periodicamente.
 */
interface AutoTurnOnAlarmScheduler {

    /**
     * Agenda o alarme responsavel por ligar o serviço de escuta de notificações de tempos em tempos
     */
    fun scheduleAutoTurnOnAlarm(millis: Long)
}
