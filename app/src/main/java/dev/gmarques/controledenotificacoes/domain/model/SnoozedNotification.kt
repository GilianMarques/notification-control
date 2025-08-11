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

package dev.gmarques.controledenotificacoes.domain.model

import androidx.annotation.Keep
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification.Companion.DEFAULT_SNOOZED_PERIOD
import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em domingo, 26 de julho de 2025 as 17:45.
 *
 * Representa uma notificação do sistema que foi oculta temporaria ou indefinidamente de maneira manual pelo usuario, ou seja,
 * sempre que o ususario selecinar uma notificação e a adiar por um tempo definido ou ilimitado um objeto desses sera criado para
 * permitir o acompanhamento e reemissao da notificação caso o sistema a perca.
 * Use a Factory [SnoozedNotificationFactory] para instanciar o objeto com segurança.
 */
@Keep
data class SnoozedNotification(
    val packageName: String,
    val key: String,
    val title: String,
    val content: String,
    val postTime: Long,
    val permaHidden: Boolean,
    val origin: Origin,
    val snoozeUntil: Long,
) : Serializable {

    companion object {
        /**Intervalo padrao ao qual uma notificação oculta pelo usuario deve ficar adiada no sistema.
         * Use [System.currentTimeMillis] + [DEFAULT_SNOOZED_PERIOD]
         */
        const val DEFAULT_SNOOZED_PERIOD = 24 * 60 * 60 * 1_000L

        /**Tolerancia usada pra verificar se uma notificação adiada emitida pelo sistema foi emitida cedo demais.
         *
         * Quando uma notificação adiada é emitida pelo sistema, verifica-se o horario em que ela deveria ser emitida e se constatado
         * que ela foi emitida mais cedo (EX.: o app emissor repetir a notificação) ela é oculta novamente.
         *
         *  Nessa verificaçao é considerada essa diferença de tempo pra mais ou menos.
         */
        const val SNOOZE_TIME_OFFSET = 60 * 1_000
    }


    /**
     * Serve pra indicar se a notificação foi adiada por uma ação automatica, derivada da execução de uma regra ou manualmente,
     * a partir de uma ação do direta do usuário.
     *
     * Isso permite processar as notificações adiadas em situações onde como quando uma [Rule] é editada e as notificações
     * adiadas via regra devem ser reemitidas para serem reprocessadas. Não conseguir identificar  a origem do adiamento de uma
     * notificação pode levar a notificações adiadas pelo usuario sendo emitidas fora do horario, matando a funcionalidade.
     */
    @Keep
    enum class Origin(val value: Int) {
        /**App lança exceção se tentar salvar isso no DB, serve apenas para evitar passar um valor na inicialização do [SnoozedNotification]*/
        NOT_SET(-1),
        USER(0),
        RULE(1)
    }
}