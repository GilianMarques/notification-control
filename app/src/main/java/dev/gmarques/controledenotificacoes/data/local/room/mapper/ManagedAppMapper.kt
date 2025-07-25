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

package  dev.gmarques.controledenotificacoes.data.local.room.mapper

import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp

/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 as 16:08.
 */
object ManagedAppMapper {

    /**
     * Mapeia um objeto [ManagedApp] para um objeto [ManagedAppEntity].
     *
     * Esta função recebe uma instância de [ManagedApp], que representa um aplicativo
     * gerenciado por um sistema, e a transforma em uma instância de [ManagedAppEntity],
     * adequada para persistência ou outras necessidades de armazenamento de dados.
     * Ela extrai as propriedades relevantes (ruleId e packageName) do [ManagedApp] e as utiliza
     * para construir um novo [ManagedAppEntity].
     *
     * @param managedApp O objeto [ManagedApp] a ser mapeado.
     * @return Um novo objeto [ManagedAppEntity] contendo os dados mapeados.
     */
    fun mapToEntity(managedApp: ManagedApp): ManagedAppEntity {

        return ManagedAppEntity(
            ruleId = managedApp.ruleId,
            packageName = managedApp.packageName,
            hasPendingNotifications = managedApp.hasPendingNotifications
        )
    }

    /**
     * Mapeia uma [ManagedAppEntity] para um modelo [ManagedApp].
     *
     * Esta função recebe uma [ManagedAppEntity] representando os dados persistentes
     * e a transforma em um objeto [ManagedApp] que é usado na lógica de domínio da aplicação.
     * Ela extrai os campos necessários (ruleId e packageName) da entidade e cria uma
     * instância [ManagedApp] correspondente.
     *
     * @param entity A [ManagedAppEntity] a ser mapeada.
     * @return Uma instância [ManagedApp] representando a entidade mapeada.
     */
    fun mapToModel(entity: ManagedAppEntity): ManagedApp {

        return ManagedApp(
            ruleId = entity.ruleId,
            packageName = entity.packageName,
            hasPendingNotifications = entity.hasPendingNotifications
        )
    }

}