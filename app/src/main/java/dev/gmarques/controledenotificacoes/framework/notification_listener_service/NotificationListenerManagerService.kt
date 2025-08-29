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
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.service.notification.NotificationListenerService.requestRebind
import android.service.notification.NotificationListenerService.requestUnbind
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startForegroundService
import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
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
class NotificationListenerManagerService : Service(), CoroutineScope by MainScope() { // TODO: usar workmanager?

    companion object {
        private const val NOTIFICATION_ID = 220461
        var instance: NotificationListenerManagerService? = null

        fun stopSelf() {
            instance?.requestListenerUnbind()
            instance?.stopForeground(STOP_FOREGROUND_REMOVE)
        }

        fun start(context: Context) {
            val serviceIntent = Intent(context, NotificationListenerManagerService::class.java)
            startForegroundService(context, serviceIntent)
        }

        fun restart(context: Context) {
            stopSelf()
            start(context)
        }


    }


    private val checkIntervalMs = if (BuildConfig.DEBUG) 5_000L else 60_000L // intervalo entre checagens
    private val channelId = "notification_watcher_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        keepCheckingNotificationListenerIsAlive()
        turnListenerOnIfNeeded()
        instance = this
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
                AppLogger.d("Checando se o listener de notificações está conectado...")
                if (!isNotificationListenerConnected()) requestListenerRebind()
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
     * Verifica se o listener de notificações está ativo.
     * Essa função pode retornar true por engano em casos onde o usuário mata o aplicativo. Ao abrir ele em seguida essa função vai
     * entender que o listener está ativo mesmo que não esteja, retornando um falso positivo
     */
    fun isNotificationListenerConnected(): Boolean {
        val cn = ComponentName(baseContext, NotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(
            baseContext.contentResolver, "enabled_notification_listeners"
        )
        // evita falsos positivos em caso de variaçoes do mesmo app instaladas
        return enabledListeners.split(":").any { it == cn.flattenToString() }
    }

    /**
     * Serve pra reanexar o listener de notificações no sistema.
     * Isso faz com que o onListenerConnected do [NotificationListener] Seja chamado E inicialize Os objetos necessários.
     *
     * Por que existe?
     * Em dispositivo Xiaomi com Android 13 (Foi onde vi o bug) as vezes onListenerConnected do [NotificationListener] nunca é
     * chamado. Ao verificar o porque, descobri que o listener ja estava conectado e deduzi que essa era a causa.
     * Quando esse bug ocorria fazia com que o restante do aplicativo nunca conseguisse receber uma instância valida
     *  de [SystemNotificationManager] para uso, o inutilizando.
     */

    fun requestListenerRebind() {
        AppLogger.d("Solicitando rebind do NotificationListener")
        val componentName = ComponentName(this, NotificationListener::class.java)
        requestRebind(componentName)
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


    /**
     * Garante que o listener esteja conectado ao abrir o processo
     */
    private fun turnListenerOnIfNeeded() {
        AppLogger.d("Ligando listener. havera rebind se necessario")
        if (!isNotificationListenerConnected()) {
            requestListenerRebind()
        }
    }


    override fun onDestroy() {
        AppLogger.d("fechando serviço. Listener conectado? ${isNotificationListenerConnected()}{}")
        cancel()
        super.onDestroy()
    }
}
