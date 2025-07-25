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

import dev.gmarques.controledenotificacoes.domain.framework.contracts.AlarmScheduler
import dev.gmarques.controledenotificacoes.framework.AutoTurnOnReceiver
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.at
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.withSecondsAndMillisSetToZero
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sábado, 05 de julho de 2025 as 19:26.
 *
 * Agenda alarmes que abrirao o [AutoTurnOnReceiver] que é responsavel por iniciar o serviço que mantem o listener de
 * notificações ativo. Isso garante que se o app fechar por algum motivo, será reaberto e o serviç seguirá rodando.
 */
class ScheduleAutoTurnOnUseCase @Inject constructor(private val scheduler: AlarmScheduler) {

    operator fun invoke() {

        val now = LocalDateTime()

        val times = listOf(
            LocalDateTime().at(12, 0).withSecondsAndMillisSetToZero(),
            LocalDateTime().at(0, 0).plusDays(1).withSecondsAndMillisSetToZero()// primeiro instante do dia seguinte
        )

        for (time in times) {
            if (time.isAfter(now)) {
                scheduler.scheduleAutoTurnOnAlarm(time.toDate().time)
                break
            }
        }
    }
}