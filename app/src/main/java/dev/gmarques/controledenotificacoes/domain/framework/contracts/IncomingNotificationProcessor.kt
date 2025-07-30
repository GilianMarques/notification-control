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

package dev.gmarques.controledenotificacoes.domain.framework.contracts

import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 18 de julho de 2025 as 17:41.
 *
 * A implementação dessa interface é usada para processar notificações recebidas pelo sistema e determinar como trata-las de
 * acordo com as regras definidas pelo usuario. Após determinar a ação, retorna um [PerformAction] indicando a ação a ser tomada.
 */
interface IncomingNotificationProcessor {

    fun processNotification(
        appNotification: AppNotification,
        rule: Rule,
        managedApp: ManagedApp,
    ): PerformAction

    sealed class PerformAction {
        /**
         * Cancela uma notificação. Não funciona com notificações persistentes
         */
        object Cancel : PerformAction()

        /**
         * Adia uma notificação mesmo que seja Persistente
         */
        object Snooze : PerformAction()

        /**App em periodo de desbloqueio, permita a notificação*/
        object Allow : PerformAction()
    }
}