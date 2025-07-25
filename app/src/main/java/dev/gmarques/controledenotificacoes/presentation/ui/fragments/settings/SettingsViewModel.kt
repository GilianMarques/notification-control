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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.settings


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.domain.data.PreferenceProperty
import dev.gmarques.controledenotificacoes.domain.data.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 30 de maio de 2025 as 11:33.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _eventsFlow = MutableSharedFlow<SettingsEvent>(replay = 1)
    val eventsFlow: SharedFlow<SettingsEvent> get() = _eventsFlow

    fun resetHints() = viewModelScope.launch(Dispatchers.IO) {
        var errors = false

        val resettableDialogHintsPreferences = Preferences.ResettableDialogHints::class.java.declaredMethods.map {
            it.name.removePrefix("get").replaceFirstChar { c -> c.lowercase() }
        }

        PreferencesImpl::class.java.declaredFields.filter { it.name.removeSuffix("\$delegate") in resettableDialogHintsPreferences }
            .forEach { field ->
                field.isAccessible = true
                val lazyValue = field.get(PreferencesImpl)
                val value = if (lazyValue is Lazy<*>) lazyValue.value else lazyValue

                if (value is PreferenceProperty<*>) value.reset()
                else {
                    errors = true
                    Log.e("SettingsViewModel", "resetHints: unsupported type ${field.type}")
                }
            }

        _eventsFlow.tryEmit(SettingsEvent.PreferencesCleaned(!errors))
    }

    fun resetBatteryOptimization() { // TODO: sem uso? 
        //PreferencesImpl.showWarningCardBatteryRestriction.reset()
        _eventsFlow.tryEmit(SettingsEvent.BatteryOptimizationWarningResetted)

    }
}

sealed class SettingsEvent {
    class PreferencesCleaned(val success: Boolean) : SettingsEvent()
    object BatteryOptimizationWarningResetted : SettingsEvent()
}
