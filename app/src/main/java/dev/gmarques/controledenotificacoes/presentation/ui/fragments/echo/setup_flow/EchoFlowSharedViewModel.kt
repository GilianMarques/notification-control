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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.setup_flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetSmartWatchInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.echo.EchoIsEnabledFragment
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar o estado do processo de echo
 * e fornecer informações relevantes para a interface de usuário.
 *
 * Criado por Gilian Marques
 * Em 12/06/2025 as 14:29
 */
@HiltViewModel
class EchoFlowSharedViewModel @Inject constructor(
    private val getSmartWatchInstalledAppsUseCase: GetSmartWatchInstalledAppsUseCase,
) : ViewModel() {

    private val _eventsFlow = MutableSharedFlow<EchoEvent>(replay = 1)
    val eventsFlow: SharedFlow<EchoEvent> get() = _eventsFlow

    private val _statesFlow = MutableStateFlow<EchoState>(EchoState.Idle)
    val statesFlow: StateFlow<EchoState> get() = _statesFlow

    var makeStepOnFabVisible = false

    /**ajuda o [EchoIsEnabledFragment] a saber se deve ou não mostrar o botão de desativar o Echo*/
    var setupConcluded = false

    fun loadSmartWatchApps() = viewModelScope.launch(IO) {
        val apps = getSmartWatchInstalledAppsUseCase()
        _statesFlow.tryEmit(EchoState.StepOne.SmartWatchApps(apps))
    }

    fun enableEcho() {
        PreferencesImpl.echoEnabled.set(true)
    }

    fun disableEcho() {
        PreferencesImpl.echoEnabled.set(false)
    }

    fun isEchoEnabled(): Boolean {
        return PreferencesImpl.echoEnabled.value
    }


}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class EchoEvent {
    object EchoOn : EchoEvent()
    object EchoOff : EchoEvent() // TODO: sem uso?
}

/**
 * Representa os estados da interface
 */
sealed class EchoState {
    object Idle : EchoState()
    sealed class StepOne : EchoState() {
        data class SmartWatchApps(val apps: List<InstalledApp>) : StepOne()
    }
}


