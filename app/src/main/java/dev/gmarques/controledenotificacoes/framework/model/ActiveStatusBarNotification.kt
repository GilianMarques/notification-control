package dev.gmarques.controledenotificacoes.framework.model

import android.app.Notification
import android.graphics.drawable.Icon

/**
 * Criado por Gilian Marques
 * Em 01/07/2025 as 17:03
 *
 * Representa uma [android.service.notification.StatusBarNotification] apenas com os dados que o app precisa.
 * Serve para evitar dependência do Domínio com o Framework Android.
 *
 * Use [ActiveStatusBarNotificationFactory] para instanciar o objeto com segurança
 */
data class ActiveStatusBarNotification(
    val title: String,
    val content: String,
    val packageId: String,
    val smallIcon: Icon?,
    val largeIcon: Icon?,
    val postTime: Long,
    val id: Int,
    val key: String,
    val isOngoing: Boolean,
    val notification: Notification,
    val tag: String?,
    )
