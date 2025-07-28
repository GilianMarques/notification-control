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

package dev.gmarques.controledenotificacoes.framework.backup_notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.framework.NotificationReceiver
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 28/07/2025 as 13:30
 *
 *  Constrói e exibe notificações de backup de notificações adiadas que nao foram emitidas pelo sistema.
 */
class BackupNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // TODO: terminar de editar
    private val channelId = "backup_snoozed_notification"

    fun showBackupNotification(notification: SnoozedNotification) {
        createNotificationChannelIfNeeded()

        val id = notification.packageName.hashCode()
        val notification = buildNotification(notification, id)
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        notificationManager.notify(id, notification)
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.Notifica_es_adiadas_perdidas)
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(notification: SnoozedNotification, id: Int): Notification {

        val appName = getAppNameFromPackage(notification.packageName)
        val appIcon = getAppIconFromPackage(notification.packageName)

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.Adiada_).plus(notification.title))
            .setContentText(
                notification.content.plus("\n\n")
                    .plus(context.getString(R.string.Clique_aqui_para_abrir_x, appName))
            )
            .setLargeIcon(appIcon)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
            .setAutoCancel(true)
            .setGroup("${System.currentTimeMillis()}")
            .setGroupSummary(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createOpenTargetPendingIntent(notification.packageName, id))
            .build()
    }

    private fun getAppIconFromPackage(packageName: String): Bitmap? = try {
        val drawable = context.packageManager.getApplicationIcon(packageName)
        if (drawable is BitmapDrawable) drawable.bitmap
        else {
            val bitmap = createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1)
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    } catch (_: Exception) {
        null
    }

    private fun getAppNameFromPackage(packageName: String): String = try {
        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(appInfo).toString()
    } catch (_: Exception) {
        packageName
    }

    private fun createOpenTargetPendingIntent(packageName: String, notificationId: Int): PendingIntent? {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.Companion.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationReceiver.Companion.EXTRA_TARGET_PACKAGE, packageName)
        }

        return PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


}
