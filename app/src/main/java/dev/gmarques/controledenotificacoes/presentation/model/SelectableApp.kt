package dev.gmarques.controledenotificacoes.presentation.model

import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em terça-feira, 17 de abril de 2025 as 22:21.
 *
 * Representa um [InstalledApp] que pode ser selecionado
 */
data class SelectableApp(
    val installedApp: InstalledApp,
    val isSelected: Boolean = false,
) : Serializable