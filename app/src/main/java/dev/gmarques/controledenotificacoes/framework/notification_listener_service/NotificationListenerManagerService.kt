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

package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.service.notification.NotificationListenerService.requestUnbind
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startForegroundService
import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListenerManagerService.Companion.instance
import dev.gmarques.controledenotificacoes.presentation.ui.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 09:07.

 * Serviço em primeiro plano que é responsavel por manter o listener de notificaçoes [NotificationListener] sempre conectado
 */
class NotificationListenerManagerService : Service(), CoroutineScope by MainScope() {

    companion object {
        private const val NOTIFICATION_ID = 220461
        var instance: NotificationListenerManagerService? = null

        /**
         * Para o serviço e remove a notificação em primeiro plano e nulifica o [instance].
         */
        fun stopSelf() {
            instance?.requestListenerUnbind()
            instance?.stopForeground(STOP_FOREGROUND_REMOVE)
            instance = null
        }

        /**
         * Inicia o serviço se ele ainda não estiver em execução.
         * @param context O contexto para iniciar o serviço.
         */
        fun startIfNotAlready(context: Context) {
            if (instance != null) return
            val serviceIntent = Intent(context, NotificationListenerManagerService::class.java)
            startForegroundService(context, serviceIntent)
        }

        fun restart(context: Context) {
            stopSelf()
            startIfNotAlready(context)
        }

        /**
         * Vetrifica se o listener se o app tem permissao pra ler notificações
         * Se essa função for chamada após o ap ser morto, ela retorna um falso negativo.
         */
        fun isListenNotificationPermissionGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver, "enabled_notification_listeners"
            ) ?: return false

            /*
            Exemplo de enableListeners string em um Xiaomi com android 13. Até onde sei isso nao muda entre versoes do android e OEMs diferentes:
            'dev.gmarques.controledenotificacoes.staging/dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener:etc...'
            */
            return enabledListeners.split(":") // obtenho os conjuntos app_pkg/nome_completo_classe isolados
                .any {
                    it.split("/")[0] // isolo o app_pkg e comparo
                        .equals(context.packageName, true)
                }
        }


    }


    private val checkIntervalMs = if (BuildConfig.DEBUG) 10_000L else 60_000L // intervalo entre checagens
    private val channelId = "notification_watcher_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        startForeground(NOTIFICATION_ID, buildNotification())
        keepCheckingNotificationListenerIsAlive()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Mantém a checagem periódica para garantir que o [NotificationListener] esteja ativo.
     * Um [Timer] é utilizado para agendar a execução da função [connectListener]
     * a cada [checkIntervalMs] milissegundos.
     */
    private fun keepCheckingNotificationListenerIsAlive() {
        launch {
            while (true) {

                if (NotificationListener.connected) {
                    AppLogger.d("listener conectado")
                    return@launch
                }

                requestListenerRebind()
                delay(checkIntervalMs)
            }
        }
    }

    /**
     * Constrói a notificação em primeiro plano (foreground) para este serviço.
     * Esta notificação é necessária para que o serviço continue executando em segundo plano
     * sem ser finalizado pelo sistema operacional.
     */
    private fun buildNotification(): Notification {
        val channelName = getString(R.string.Monitoramento_de_notificacoes)

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.Monitoramento_de_notificacoes))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(getPendingIntentForOpenTheApp())
            .addAction(R.drawable.vec_open_app, getString(R.string.Desativar), getPendingIntentForNotificationSettings())
            .build()
    }

    /**
     * Abre as configurações de notificação para o canal específico da notificação em primeiro plano (foreground).
     * Em versões mais recentes do Android (O+), navega diretamente para as configurações do canal.
     */
    private fun getPendingIntentForNotificationSettings(): PendingIntent {

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, baseContext.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        return PendingIntent.getActivity(
            baseContext, 220462, intent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getPendingIntentForOpenTheApp(): PendingIntent {

        val intent = Intent(baseContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            baseContext,
            462025, // usar diferentes se tiver múltiplas notificações com intents distintas
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    }

    /**
     * Reanexa o listener de notificações ao sistema, forçando a chamada do mét.odo `onListenerConnected`
     * da classe [NotificationListener]. Este processo é crucial para inicializar objetos necessários
     * para o funcionamento adequado do listener.
     *
     * **Motivação:**
     * Em determinados cenários, especialmente observado em dispositivos Xiaomi com Android 13,
     * o mét.odo `onListenerConnected` do [NotificationListener] pode não ser invocado mesmo quando o
     * listener já está conectado ao sistema. Essa falha impede que o aplicativo receba uma instância
     * válida de [SystemNotificationManager], comprometendo sua funcionalidade principal de gerenciamento
     * de notificações. Este mét.odo visa contornar esse problema ao forçar a reconexão do listener.
     */
    fun requestListenerRebind() {
        AppLogger.d("Solicitando rebind do NotificationListener")

        val componentName = ComponentName(this, NotificationListener::class.java)

        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        //  Por algum motivo usar o packagemanager pra ligar/desligar o listener é mais rapido ~1-2 segs com parado ao
        //  requestRebind() que por sua vez pode levar 7 segs pra reconectar isso quando nunca reconecta
        //requestRebind(componentName)

    }

    private fun requestListenerUnbind() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            AppLogger.d("Solicitando unbind do NotificationListener")
            val componentName = ComponentName(this, NotificationListener::class.java)
            requestUnbind(componentName)
        } else Log.d(
            "USUK",
            "NotificationListenerManagerService.requestListenerUnbind: Impossivel desconectar nessa versao de API"
        )
    }


    override fun onDestroy() {
        AppLogger.d("fechando serviço. Listener conectado? ${NotificationListener.connected}")
        cancel()
        super.onDestroy()
    }
}
