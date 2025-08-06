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

package dev.gmarques.controledenotificacoes.domain.data

import androidx.annotation.Keep

@Keep
interface Preferences {

    /**Se deve incluir apps do sistema na lista de seleçao de apps instalados*/
    val includeSystemApps: PreferenceProperty<Boolean>

    /**Se deve incluir apps que ja estao sendo gerenciados na lista de seleçao de apps instalados*/
    val includeManagedApps: PreferenceProperty<Boolean>

    val scheduledReportNotificationAlarms: PreferenceProperty<String>

    val scheduledSnoozedNotificationAlarms: PreferenceProperty<String>
    val lastSelectedRule: PreferenceProperty<String>

    val showDialogNotPermissionDenied: PreferenceProperty<Boolean>


    val echoEnabled: PreferenceProperty<Boolean>

    val detailsPaneScreenPercent: PreferenceProperty<Float>

    val showHomeFragmentUiHints: PreferenceProperty<Boolean>

    val showManageNotificationsFragmentUiHints: PreferenceProperty<Boolean>

    @Keep
    /**Todas as preferencias dentro dessa interface podem ser resetadas pelo usuario*/
    interface ResettableDialogHints {
        val showHintEditFirstRule: PreferenceProperty<Boolean>
        val showHintSelectFirstApp: PreferenceProperty<Boolean>
        val showHintHowRulesAndManagedAppsWork: PreferenceProperty<Boolean>
        val showHintSelectedAppsAlreadyManaged: PreferenceProperty<Boolean>
    }

}






