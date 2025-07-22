package dev.gmarques.controledenotificacoes.domain.model

import java.io.Serializable
import java.util.UUID

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 21:15.
 *
 * Acesse funções de extensão utilitárias para essa classe através de [TimeRangeExtensionFun]
 *
 * Representa periodos de tempo em que uma regra deve atuar
 */

data class TimeRange(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val id: String = UUID.randomUUID().toString(), // TODO: remover valor padrao e deifna [defaultId] maunalmente
    val allDay: Boolean = false,
) : Serializable {
    constructor(allDay: Boolean) : this(startHour = 0, startMinute = 0, endHour = 0, endMinute = 0, allDay = allDay)

    companion object {
        val defaultId = UUID.randomUUID().toString()
    }
}
