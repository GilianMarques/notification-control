package dev.gmarques.controledenotificacoes.domain.model

import androidx.annotation.Keep
import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 20 de junho de 2025 as 15:03.
 */
@Keep
data class Condition(
    val type:  Type,
    val field: NotificationField,
    val keywords: List<String>,
    val caseSensitive: Boolean = false,
) : Serializable {

    companion object {
        const val SEPARATOR = ","
    }

    /**
     * Criado por Gilian Marques
     * Em sexta-feira, 20 de junho de 2025 as 14:58.
     */
    @Keep
    enum class Type() {
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
}