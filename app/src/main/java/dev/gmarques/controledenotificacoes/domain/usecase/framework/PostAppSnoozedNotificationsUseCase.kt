package dev.gmarques.controledenotificacoes.domain.usecase.framework

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 23 de julho de 2025 às 13:14.
 *
 * Responsável por cancelar o adiamento e emitir imediatamente todas as notificações adiadas
 * de um aplicativo específico.
 *
 * Criado originalmente para ser utilizado após a edição de uma regra, uma vez que a reemissão
 * das notificações faz com que elas sejam reprocessadas de acordo com a nova regra.
 */
class PostAppSnoozedNotificationsUseCase @Inject constructor() {

    operator fun invoke(app: ManagedApp) {
        val notificationListener = NotificationListener.instance()

        notificationListener?.getSnoozedNots()
            ?.filter { it.packageName == app.packageName }
            ?.onEach { notificationListener.postSnoozedNotification(it) }
    }
}
