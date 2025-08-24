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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.presentation.ui.activities.MainActivity
import java.util.Timer
import java.util.TimerTask

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 09:07.

 * Serviço em primeiro plano que é responsavel por manter o listener de notificaçoes [NotificationListener] sempre conectado
 */
class NotificationListenerManagerService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 220461
        var instance: NotificationListenerManagerService? = null
        fun stopSelf() {
            instance?.disconnectListener()
            instance?.stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    private val checkIntervalMs = if (BuildConfig.DEBUG) 5_000L else 60_000L // intervalo entre checagens
    private var timer: Timer? = null
    private val channelId = "notification_watcher_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        keepCheckingNotificationListenerIsAlive()
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
        timer?.cancel()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    if (!isNotificationListenerConnected()) connectListener()
                }
            }, 0, checkIntervalMs)
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
        val baseContext = App.instance

        val cn = ComponentName(baseContext, NotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(
            baseContext.contentResolver, "enabled_notification_listeners"
        )
        // evita falsos positivos em caso de variaçoes do mesmo app instaladas
        return enabledListeners.split(":").any { it == cn.flattenToString() }
    }

    fun connectListener() {
        val pm = packageManager
        val componentName = ComponentName(this, NotificationListener::class.java)

        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun disconnectListener() {
        val pm = packageManager
        val componentName = ComponentName(this, NotificationListener::class.java)

        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
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
    fun restartListener() {
        AppLogger.d("")
        disconnectListener()
        connectListener()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
