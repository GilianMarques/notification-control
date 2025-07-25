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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.User
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppByPackageOrDefaultUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.ObserveAllManagedApps
import dev.gmarques.controledenotificacoes.domain.usecase.rules.ObserveAllRulesUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRuleFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar o estado da lista de aplicativos controlados.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeAllRulesUseCase: ObserveAllRulesUseCase,
    observeAllManagedApps: ObserveAllManagedApps,
    private val getInstalledAppByPackageOrDefaultUseCase: GetInstalledAppByPackageOrDefaultUseCase,
    private val getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
    private val getUserUseCase: GetUserUseCase,
    @ApplicationContext context: Context,
) : ViewModel() {


    /**
     * A função que combina os  dados ([combineFlows]) é atualizada sempre que qualquer um dos 3 [Flow]s é atualizado.
     * Não existe uma ordem específica em que isso pode acontecer, por esse motivo, é possível que ao combinar os dados um
     * aplicativo gerenciado a sua regra nao esteja presente na lista de regras, isso lançaria uma exceção.
     * Para evitar isso, esta instância de regra é definida até que a lista de regras seja atualizada e os dados sejam combinados
     * novamente, o que dentro do funcionamento normal do aplicativo deve acontecer instantaneamente, uma vez que ao adicionar um
     * aplicativo gerenciado e uma regra as informações entram no banco de dados uma seguida da outra, por consequência as atualizações
     * são disparadas em sequência também.
     *
     * De maneira geral o usuário não verá esta regra na tela porque a atualização será instantânea. Se isso acontecer
     * pode ser indício de um bug e deve ser investigado.
     *
     */
    val defaultRuleIfNotFound: Rule by lazy {
        Rule(
            name = context.getString(R.string.Regra_nao_encontrada),
            days = listOf(Rule.WeekDay.SUNDAY),
            condition = null,
            timeRanges = listOf(TimeRange(1, 2, 3, 4)),
            type = Rule.typeDefault,
            action = Rule.actionDefault,
            keepFullHistory = Rule.keepFullHistoryDefault,
        )
    }

    private val rules = observeAllRulesUseCase().init(null)
    private val managedApps = observeAllManagedApps().init(null)
    val managedAppsWithRules = combine(rules, managedApps, ::combineFlows).init(null)


    /**
     * Essa função existe para que seja feita a delegação da parte de combinar valores do flow, uma vez que o código de inicialização
     * do [managedAppsWithRules] estava muito Comprida
     *
     * Essa função é reativa sempre que houver atualizações em qualquer uma das listas esta será chamada para gerar uma nova lista combinada.
     *
     * Inicializa o [managedAppsWithRules] flow
     *
     * Combina a lista de regras com a lista de aplicativos gerenciados + os aplicativos instalados no dispositivo
     * ou armazenados em cache na memoria para criar uma lista de [ManagedAppWithRule].
     *
     * @param rules Lista de regras.
     * @param managedApps Lista de aplicativos gerenciados.
     * @return Lista de [ManagedAppWithRule].
     */
    private fun combineFlows(
        rules: List<Rule>?,
        managedApps: List<ManagedApp>?,
    ): List<ManagedAppWithRule>? {

        /*Se qualquer uma dessas listas for nula  é indicação de que não foram inicializadas
         com valores do banco de dados ainda, portanto, nenhuma operação deve ser feita */
        if (rules == null || managedApps == null) return null

        /*As regras são a base deste aplicativo se a lista de regras está vazia, nenhuma operação deve ser feita.
         A lista pode ficar vazia se a última regra foi removida ou não existem regras, o que afirma que não existem
         aplicativos gerenciados a menos que seja por um bug */
        if (rules.isEmpty()) return emptyList()

        val rulesMap = rules.associateBy { it.id }

        return managedApps
            .map { managedApp ->

                val installedApp = runBlocking {
                    getInstalledAppByPackageOrDefaultUseCase(managedApp.packageName)
                }

                ManagedAppWithRuleFactory.create(installedApp, managedApp, rulesMap[managedApp.ruleId] ?: defaultRuleIfNotFound)

            }
            .sortedWith(compareByDescending<ManagedAppWithRule> { it.hasPendingNotifications }
                .thenBy { it.name.lowercase() })

    }

    /**
     * Essa função evita boilerplate code e garante a conformidade com o DRY neste viewmodel
     *
     * Transforma um [Flow] qualquer em um [StateFlow] que vai emitir valores enquanto o ciclo de vida
     * da ViewModel estiver ativo.
     *
     * O [StateFlow] vai compartilhar o valor para novos coletores e manterá em memoria o ultimo
     * valor emitido.
     *
     */
    private fun <T> Flow<T>.init(initialValue: T): StateFlow<T> {
        return this.stateIn(
            scope = this@HomeViewModel.viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialValue
        )
    }

    /**
     * Recupera o usuário logado.
     * @return O usuário logado.
     * @throws IllegalStateException Se não houver usuário logado.
     */
    fun getUser(): User {
        return getUserUseCase() ?: error("É necessário estar logado para chegar nesse ponto.")
    }

    /**
     * Recupera o caso de uso para obter o ícone de um aplicativo instalado.
     * @return O caso de uso [GetInstalledAppIconUseCase].
     */
    fun getInstalledAppIcon(): GetInstalledAppIconUseCase = getInstalledAppIconUseCase

}


