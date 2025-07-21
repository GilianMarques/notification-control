package dev.gmarques.controledenotificacoes.domain.usecase.app_notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.framework.PendingIntentCache
import javax.inject.Inject

/**
 * Remove todas as notificações de um determinado aplicativo  assim como as PendingIntents e Imagens em cache referentes à essas
 * notificações
 *
 * Atençao: Este usecase é usando em uma Transação do Room. Não crie corrotinas ou mude o escopo do contexto para que a
 * transação não perca o efeito.
 * */
class DeleteAllAppNotificationsUseCase @Inject constructor(
    private val repository: AppNotificationRepository,
    @ApplicationContext private val context: Context,
) {
    /**
     * Limpa o cache de intents e de bitmaps relacionado ao app e por fim apaga as notificaçoes do db
     * de forma que o cache nao fique orfao se houver algum erro entre as operações
     */
    suspend operator fun invoke(packageName: String) {
        removeBitmapsFromCache(packageName)
        removePendingIntentsFromCache(packageName)
        repository.deleteAll(packageName)
    }

    private fun removeBitmapsFromCache(packageName: String) {
        context.cacheDir.listFiles()?.forEach {
            if (it.name.contains(packageName)) {
                it.delete()
            }
        }
    }

    private fun removePendingIntentsFromCache(packageName: String) {
        PendingIntentCache.removeAllFrom(packageName)
    }
}