package dev.gmarques.controledenotificacoes.domain.model.enums

import androidx.annotation.Keep

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 20 de junho de 2025 as 14:58.
 */
@Keep
enum class ConditionType() {
    ONLY_IF, EXCEPT
}

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 20 de junho de 2025 as 15:03.
 */
@Keep
enum class NotificationField() {
    TITLE, CONTENT, BOTH
}