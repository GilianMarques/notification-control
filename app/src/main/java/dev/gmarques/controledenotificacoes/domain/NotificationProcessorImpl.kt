package dev.gmarques.controledenotificacoes.domain

import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

class NotificationProcessorImpl : NotificationProcessor {
    override fun processNotification(
        activeNotification: ActiveStatusBarNotification,
        appNotification: AppNotification,
        callback: NotificationProcessor.ResultCallback
    ) {
        TODO("Not yet implemented")
    }
}