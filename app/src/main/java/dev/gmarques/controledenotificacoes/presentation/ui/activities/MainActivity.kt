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

package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.databinding.ActivityMainBinding
import dev.gmarques.controledenotificacoes.domain.framework.contracts.VibratorProvider
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListenerManagerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var vibratorProvider: VibratorProvider
    private var backgroundChanged = false
    private lateinit var splashLabel: String
    private lateinit var homeLabel: String
    private var requestIgnoreBatteryOptimizationsJob: Job? = null
    private lateinit var appUpdateManager: AppUpdateManager

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                binding.root,
                getString(R.string.Atualizacao_disponivel_para_instalacao),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.Instalar)) {
                appUpdateManager.completeUpdate()
            }.show()
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 22041961
        private const val UPDATE_REQUEST_CODE = 46251749
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        splashLabel = getString(R.string.Splash_fragment)
        homeLabel = getString(R.string.Fragment_home)

        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observeNavigationChanges()
        checkForAppUpdate()
        NotificationListenerManagerService.startIfNotAlready(this@MainActivity)
    }


    private fun checkForAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateUpdatedListener)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    @Suppress("DEPRECATION")
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (_: Exception) {

                }
            }
        }
    }

    override fun onStop() {
        /**impede que a tela de otimização de bateria (a reserva, caso a primeira nao abra) seja aberta se a primeira for*/
        requestIgnoreBatteryOptimizationsJob?.cancel()
        super.onStop()
    }

    private fun observeNavigationChanges() {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_master) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { navController, destination, bundle ->

            if (destination.id != R.id.splashFragment &&
                destination.id != R.id.loginFragment
            ) applyDefaultBackgroundColor()

        }
    }

    /**
     * Os fragmentos são transparentes por isso preciso remover o background do splashscreen e definir uma cor sólida
     * na activity
     */
    private fun applyDefaultBackgroundColor() {

        if (backgroundChanged) return
        //  AppLog.d("USUK", "MainActivity.applyDefaultBackgroundColor: ")
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.AppColorBackground, typedValue, true)
        window.decorView.setBackgroundColor(typedValue.data)

        backgroundChanged = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!granted) {

                if (PreferencesImpl.showDialogNotPermissionDenied.isDefault()) {

                    MaterialAlertDialogBuilder(this@MainActivity).setTitle(getString(R.string.Permissao_nao_concedida))
                        .setMessage(getString(R.string.Voce_nao_ser_avisado_sobre_notifica_es_bloqueadas_ao_fim_do_per_odo_de_bloqueio_dos_apps_conceda_a_permiss_o_para_n_o_perder_alertas_importantes))
                        .setPositiveButton(getString(R.string.Entendi)) { _, _ ->
                            lifecycleScope.launch { PreferencesImpl.showDialogNotPermissionDenied.set(false) }
                        }.setCancelable(false).show()
                }
            } else {
                NotificationListenerManagerService.restart(this@MainActivity)
            }

        }
    }

    fun isPostNotificationsPermissionEnable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun isListenNotificationPermissionGranted(): Boolean {
        return NotificationListenerManagerService
            .isListenNotificationPermissionGranted(this@MainActivity)
    }

    fun requestNotificationAccessPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
        Toast.makeText(
            this, getString(R.string.Permita_que_x_acesse_as_notificacoes, getString(R.string.app_name)), Toast.LENGTH_LONG
        ).show()
    }

    fun isAppInsetFromBatterySaving(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    /**
     * Tenta abrir a tela de otimização de bateria especifica do app e agenda a tela geral de otimização de bateria para ser aberta após um tempo
     * se a 1° tela for aberta o app sera minimizaodo e o [onStop] da activity vai cancelar o [requestIgnoreBatteryOptimizationsJob] que abriria a 2°
     * tela, senao a 2° tela será aberta para que o usuario procure o app manualmente e o isente das restrilções de bateria
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations() {

        try {
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:$packageName".toUri()))
        } catch (_: Exception) {
        }

        requestIgnoreBatteryOptimizationsJob = lifecycleScope.launch {
            delay(1000)
            try {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (_: Exception) {
                showRequestIgnoreBatteryOptimizationErrorDialog()
            }
        }

    }

    private fun showRequestIgnoreBatteryOptimizationErrorDialog() {
        MaterialAlertDialogBuilder(this@MainActivity).setTitle(getString(R.string.Erro))
            .setMessage(getString(R.string.Nao_foi_poss_vel_abrir_a_tela_de_configuracoes))
            .setPositiveButton(R.string.Entendi) { _, _ -> }.setIcon(R.drawable.vec_alert).show()

    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
        super.onDestroy()
    }

    /**
     * Tenta abrir um app com base no id do pacote
     * @param packageName id do pacote do app a ser aberto ex: com.google.android.youtube
     * @return true se o app foi aberto com sucesso, false caso contrario
     */
    fun launchApp(packageName: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        return if (launchIntent != null) {
            startActivity(launchIntent)
            true
        } else false

    }


     fun openMailToSendFeedback() {
        val email = App.instance.remoteConfigValues.value?.contactEmail
        if (email == null) return

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(
                Intent.EXTRA_TEXT,
                (getString(R.string.Insira_aqui_suas_duvidas_sugestoes_de_melhorias_e_funcionalidades_ou_problemas_que_ocorreram_durante_o_uso))
            )
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.Feedback_do_app))
        }

        if (intent.resolveActivity(App.instance.packageManager) != null) {
            App.instance.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
        } else {
            Toast.makeText(App.instance, getString(R.string.Nenhum_app_de_e_mail_encontrado), Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Abre a página do aplicativo na Play Store.
     *
     * Tenta abrir diretamente no aplicativo da Play Store. Se não estiver instalado,
     * abre no navegador.
     *
     * Utiliza um link do Firebase Remote Config se disponível, caso contrário, usa o nome do pacote do aplicativo.
     */
    fun openPlayStore() {
        val appPackageName = App.instance.packageName
        // TODO: otimizar depois dos testes

        val playStoreLink = App.instance.remoteConfigValues.value?.playStoreAppLink
        if (!playStoreLink.isNullOrBlank()) {

            try {

                val intent = Intent(Intent.ACTION_VIEW, playStoreLink.toUri()).apply {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                    setPackage("com.android.vending")
                }
                startActivity(intent)

            } catch (_: ActivityNotFoundException) {

                val intent = Intent(Intent.ACTION_VIEW, playStoreLink.toUri()).apply {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)

            }
        } else try {

            val intent = Intent(
                Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri()
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.vending")
            startActivity(intent)

        } catch (_: ActivityNotFoundException) {

            val intent = Intent(
                Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
    }

}
