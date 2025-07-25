/*
 * MIT License
 *
 * Copyright (c) 2025 Gilian Marques Fernandes - linkedin.com/in/gilianmarques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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