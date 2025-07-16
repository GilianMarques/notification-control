package dev.gmarques.controledenotificacoes.domain.usecase.alarms

import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 16/07/2025 as 14:39
 *
 * Use-case para cancelar um alarme para um aplicativo específico.
 *
 */
class CancelAlarmForAppUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
) {
    /**
     * Cancela o alarme associado ao ID do pacote fornecido.
     * Não verifica se existe um agendamento ativo para o aplicativo, apenas manda cancelar.
     * @param pkgId O ID do pacote do aplicativo para o qual o alarme deve ser cancelado.
     */
    suspend operator fun invoke(pkgId: String) = withContext(IO) {
        alarmScheduler.cancelAlarm(pkgId)
    }

}