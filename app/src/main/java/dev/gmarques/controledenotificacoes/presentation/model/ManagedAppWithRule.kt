package dev.gmarques.controledenotificacoes.presentation.model

import androidx.annotation.Keep
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import java.io.Serializable


/**
 *
 * Criado por Gilian Marques
 * Em sábado, 26 de abril de 2025 as 17:41.
 *
 * Representa um [ManagedApp] com mais alguns dados que devem ser obtidos em runtime como nomeda aplicação
 * e uma instancia de [Rule]  pra facilitar o uso na UI e otimizar o processamento.
 *
 * Use [ManagedAppWithRuleFactory] para criar instancias seguras dessa classe.
 */
@Keep
data class ManagedAppWithRule(
    val name: String,
    val packageName: String,
    val rule: Rule,
    val hasPendingNotifications: Boolean,
    val uninstalled: Boolean,
) : Serializable
