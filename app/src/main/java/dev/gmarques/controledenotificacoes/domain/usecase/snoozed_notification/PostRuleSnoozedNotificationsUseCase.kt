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

package dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppsByRuleIdUseCase
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 23 de julho de 2025 às 13:14.
 *
 * Responsável por cancelar o adiamento e emitir imediatamente todas as notificações adiadas
 * de aplicativos regidos por uma regra específica.
 *
 * Criado originalmente para ser utilizado após a edição de uma regra, uma vez que a reemissão
 * das notificações faz com que elas sejam reprocessadas de acordo com a nova regra.
 */

class PostRuleSnoozedNotificationsUseCase @Inject constructor(
    private val getManagedAppsByRuleIdUseCase: GetManagedAppsByRuleIdUseCase,
    private val postAppSnoozedNotificationsUseCase: PostAppSnoozedNotificationsUseCase,
) {
    suspend operator fun invoke(rule: Rule) {
        getManagedAppsByRuleIdUseCase(rule.id).onEach { app ->
            app?.let { postAppSnoozedNotificationsUseCase(app) }
        }
    }
}
