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
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.presentation.ui.activities.MainActivity
import java.util.Timer
import java.util.TimerTask

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 09:07.

 * Serviço em primeiro plano que é responsavel por manter o listener de notificaçoes [NotificationListener] sempre conectado
 */
class NotificationServiceManager : Service() {

    companion object {
        private const val NOTIFICATION_ID = 220461
        private var instance: NotificationServiceManager? = null
        fun stopSelf() {
            instance?.disconnectListener()
            instance?.stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    private val checkIntervalMs = 10_000L // intervalo entre checagens
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
     * Um [Timer] é utilizado para agendar a execução da função [forceReconnectNotificationListener]
     * a cada [checkIntervalMs] milissegundos.
     */
    private fun keepCheckingNotificationListenerIsAlive() {
        timer?.cancel()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    forceReconnectNotificationListener()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

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
     * Verifica se o listener de notificações está ativo.
     * Essa função pode retornar true por engano em casos onde o usuário mata o aplicativo. Ao abrir ele em seguida essa função vai
     * entender que o listener está ativo mesmo que não esteja, retornando um falso positivo
     */
    @Suppress("unused")
    fun isNotificationListenerActive(): Boolean {
        val cn = ComponentName(baseContext, NotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(
            baseContext.contentResolver,
            "enabled_notification_listeners"
        )
        return (enabledListeners?.contains(cn.flattenToString()) == true).also {
            Log.d(
                "USUK",
                "NotificationServiceManager.isNotificationListenerActive: $it "
            )
        }
    }

    /**
     * Força a reconexão do [NotificationListener].
     * Isso é feito desabilitando e reabilitando o componente [NotificationListener]
     * via [PackageManager]. Esta é uma abordagem conhecida para resolver problemas
     * onde o listener de notificações para de funcionar.
     */
    fun forceReconnectNotificationListener() {
        val pm = packageManager
        val componentName = ComponentName(this, NotificationListener::class.java)

        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun disconnectListener() {
        val pm = packageManager
        val componentName = ComponentName(this, NotificationListener::class.java)
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * Abre as configurações de notificação para o canal específico da notificação em primeiro plano (foreground).
     * Em versões mais recentes do Android (O+), navega diretamente para as configurações do canal.
     * Em versões anteriores (N e N-MR1), abre as configurações gerais do aplicativo.
     *
     */
    fun getPendingIntentForNotificationSettings(): PendingIntent {
        val intent = when {

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // API 26+: vai direto pro canal
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, baseContext.packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                }
            }

            else -> {
                // API 24 e 25: abre tela de configurações gerais do app
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
            }

        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        return PendingIntent.getActivity(
            baseContext, 220462, intent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun getPendingIntentForOpenTheApp(): PendingIntent {

        val intent = Intent(baseContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            baseContext,
            462025, // requestCode, use diferentes se tiver múltiplas notificações com intents distintas
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
