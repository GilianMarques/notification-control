package dev.gmarques.controledenotificacoes.domain.usecase.framework

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
