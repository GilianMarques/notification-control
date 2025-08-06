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

package dev.gmarques.controledenotificacoes.data.local

import androidx.annotation.Keep
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.data.PreferenceProperty
import dev.gmarques.controledenotificacoes.domain.data.Preferences
import dev.gmarques.controledenotificacoes.domain.data.Preferences.ResettableDialogHints
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.ReadPreferenceUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.SavePreferenceUseCase
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 28 de maio de 2025 as 20:43.
 *
 * Implementa [Preferences] e [Preferences.ResettableDialogHints] que definem quais sao as preferencias disponiveis no app e faz a implementação
 * dessas preferencias usando instancias de [PreferenceProperty] inicializadas sob demanda para  facilitar o acesso, leitura e escrita
 * das preferências através dos usecases
 * [ReadPreferenceUseCase] e [SavePreferenceUseCase]
 * Isso permite acessar e modificar as preferencias de maneira simples, funcional e escalavel
 *
 */
@Keep
object PreferencesImpl : Preferences, ResettableDialogHints {

    private val reader = HiltEntryPoints.readPreferenceUseCase()
    private val saver = HiltEntryPoints.savePreferenceUseCase()

    override val showHintEditFirstRule: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_hint_edit_first_rule",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }
    override val showHintSelectFirstApp: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_hint_select_first_app",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val showHintHowRulesAndManagedAppsWork: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_hint_how_rules_and_managed_apps_work",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val showHintSelectedAppsAlreadyManaged: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_hint_selected_apps_already_managed",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val includeSystemApps: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "pref_include_system_apps",
            defaultValue = false,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke,
        )
    }

    override val includeManagedApps: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "pref_include_managed_apps",
            defaultValue = false,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val scheduledReportNotificationAlarms: PreferenceProperty<String> by lazy {
        PreferenceProperty(
            key = "scheduled_report_notification_alarms",
            defaultValue = "",
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val scheduledSnoozedNotificationAlarms: PreferenceProperty<String> by lazy {
        PreferenceProperty(
            key = "scheduled_snoozed_notification_alarms",
            defaultValue = "",
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }

    override val lastSelectedRule: PreferenceProperty<String> by lazy {
        PreferenceProperty(
            key = "last_selected_rule", defaultValue = "null", preferenceReader = reader::invoke, preferenceSaver = saver::invoke
        )
    }

    override val showDialogNotPermissionDenied: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "show_dialog_not_permission_denied",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }


    override val echoEnabled: PreferenceProperty<Boolean> by lazy {
        PreferenceProperty(
            key = "echo_enabled", defaultValue = false, preferenceReader = reader::invoke, preferenceSaver = saver::invoke
        )
    }
    override val detailsPaneScreenPercent: PreferenceProperty<Float> by lazy {
        PreferenceProperty(
            key = "details_pane_screen_percent",
            defaultValue = SlidingPaneController.DEFAULT_TARGET_PERCENT,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }
    override val showHomeFragmentUiHints: PreferenceProperty<Boolean> by lazy {

        PreferenceProperty(
            key = "show_home_fragment_ui_hints",
            defaultValue = true,
            preferenceReader = reader::invoke,
            preferenceSaver = saver::invoke
        )
    }


}