package dev.gmarques.controledenotificacoes.domain.model

import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 14:21.
 *
 * Representa a notificação bloqueada de um app
 * Use a Factory [AppNotificationFactory] para instanciar o objeto com segurança.
 */
data class AppNotification(
    val packageName: String,
    val title: String,
    val content: String,
    val postTime: Long,
) : Serializable