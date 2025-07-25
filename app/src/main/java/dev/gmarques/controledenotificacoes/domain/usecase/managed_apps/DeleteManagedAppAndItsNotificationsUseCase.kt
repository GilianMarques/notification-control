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

package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import android.util.Log
import androidx.room.withTransaction
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.DeleteAllAppNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.DeleteRuleWithAppsUseCase
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 02 de maio de 2025 as 22:34.
 *
 */
class DeleteManagedAppAndItsNotificationsUseCase @Inject constructor(
    private val roomDb: RoomDatabase,
    private val repository: ManagedAppRepository,

    private val deleteAllAppNotificationsUseCase: DeleteAllAppNotificationsUseCase,
) {
    /**
     * Este UseCase pode ser utilizado tanto dentro quanto fora de uma transação do Room.
     *
     * Quando chamado por [DeleteRuleWithAppsUseCase], a operação já está
     * encapsulada em uma transação externa. No entanto, este UseCase também é reutilizado por outras classes que não iniciam
     * transações, o que exige que ele mesmo controle a atomicidade nesses casos.
     *
     * Para evitar transações aninhadas (que não são suportadas pelo Room) e duplicação de código,
     * a flag [doInsideTransaction] foi introduzida. Ela define se as operações devem ser executadas dentro de uma
     * transação interna ou assumem que o contexto já está transacional.
     */

    suspend operator fun invoke(packageName: String, doInsideTransaction: Boolean = true) {

        val action: suspend () -> Unit = {
            /*
                * Esta ordem limpa o cache de pendingIntents e bitmaps e depois apaga as notificaçoes do banco (com [DeleteAllAppNotificationsUseCase])
                * por fim apaga o app, isso garante que os dados em cache nao fiquem orfaos caso as notificações
                * sejam apagadas primeiro e um erro impeça que eles sejam removidos, permanecendo em cache por tempo indefinido.
                * Por fim se algo der errado, o cache foi limpo mas os dados do banco sao restaurados pela transação e aí é só tentar apagar de novo.
                */
            deleteAllAppNotificationsUseCase(packageName)
            repository.deleteManagedAppByPackageId(packageName)
        }

        try {
            if (doInsideTransaction) roomDb.withTransaction { action() }
            else action()
        } catch (e: Exception) {
            Log.e("USUK", "DeleteManagedAppAndItsNotificationsUseCase.invoke: Falha na transação: ${e.message}")
            false
        }

    }
}