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

package dev.gmarques.controledenotificacoes

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.framework.model.RemoteConfigValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@HiltAndroidApp
class App() : Application(), CoroutineScope by MainScope() {

    companion object {
        lateinit var instance: App
            private set

        /**
         * Necessário verificar sempre porque o ususario pode alterar as configurações de densidade da tela mudando o formato
         * é um caso extremo mas agora esta coberto
         * */
        var largeScreenDevice = false
            private set
            get() = this.instance.resources.getBoolean(R.bool.device_is_tablet)

    }

    private val _remoteConfigValues = MutableStateFlow<RemoteConfigValues?>(null)
    val remoteConfigValues get() = _remoteConfigValues

    override fun onCreate() {
        instance = this
        setupRemoteConfig()
        setupCrashLytics()
        scheduleAlarms()
        super.onCreate()
    }



    /**
     * Reagenda os alarmes necessários pro funcionamento correto da aplicação no sistma.
     * Sempre que o aplicativo é morto (forçar parada, algum crash mais sério, reboot do dispositivo, etc...)
     * os alarmes se perdem, sendo necessário o reagendamento.
     *
     * A execução dessa classe não indica que o app foi morto.
     */
    private fun scheduleAlarms() = CoroutineScope(IO).launch {
        /** Vai agendar em loop um broadcast que liga o serviço de notificações */
        HiltEntryPoints.scheduleAutoTurnOnUseCase().invoke()
        HiltEntryPoints.scheduleBackupNotificationsOnBootUseCase().invoke()
        HiltEntryPoints.rescheduleReportNotificationsOnBootUseCase().invoke()
    }

    private fun setupCrashLytics() {

        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            return
        }

        val getAppUserUseCase = HiltEntryPoints.getAppUserUseCase()

        FirebaseCrashlytics.getInstance().apply {
            setUserId(getAppUserUseCase()?.email ?: "not_logged_in")
        }
    }

    private fun setupRemoteConfig() = CoroutineScope(IO).launch {

        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 10 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        val fetchResult = withTimeoutOrNull(5000) { // 5 segundos
            try {
                remoteConfig.fetchAndActivate().await()
                _remoteConfigValues.tryEmit(
                    RemoteConfigValues(
                        blockApp = remoteConfig.getLong("blockBelow").toInt() > BuildConfig.VERSION_CODE,
                        contactEmail = remoteConfig.getString("contactEmail"),
                        playStoreAppLink = remoteConfig.getString("playStoreAppLink"),
                        privacyUrl = remoteConfig.getString("privacyUrl"),

                        )
                )
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                null
            }
        }

        if (fetchResult == null) {
            _remoteConfigValues.tryEmit(
                RemoteConfigValues()
            )
        }

    }
}