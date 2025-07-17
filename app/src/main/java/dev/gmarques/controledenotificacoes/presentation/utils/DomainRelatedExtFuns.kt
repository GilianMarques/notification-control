package dev.gmarques.controledenotificacoes.presentation.utils

import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.Rule.Type

/**
 * Criado por Gilian Marques
 * Em terça-feira, 22 de abril de 2025 as 20:26.
 * Aqui ficam as funçoes de extençao relacionadas aos modelos de objetos de dominio dos quais a aplicação depende
 */
object DomainRelatedExtFuns {

    fun Rule.getAdequateIconReference() = when (this.type) {
        Type.RESTRICTIVE -> R.drawable.vec_rule_restrictive
        Type.PERMISSIVE -> R.drawable.vec_rule_permissive
    }

    fun Rule.getAdequateIconReferenceSmall() = when (this.type) {
        Type.RESTRICTIVE -> R.drawable.vec_rule_restrictive_small
        Type.PERMISSIVE -> R.drawable.vec_rule_permissive_small
    }
}