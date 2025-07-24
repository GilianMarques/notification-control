package dev.gmarques.controledenotificacoes.framework.report_notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Classe que extende `BroadcastReceiver` responsavel por exibir notificações recebidas e tratar
 * as interações do usuário com essas notificações.
 *
 * Quando o ususario clica em uma notificação de relatorio, esta classe é invocada pelo sistema Android.
 * Ela extrai os dados relevantes da notificação, como o ID da notificação, o nome do pacote de
 * destino e a intenção original que disparou a notificação.
 *
 * Se a notificação tiver um ID válido e uma intenção de destino válida, esta classe cancela a
 * notificação original e inicia a atividade de destino, garantindo que a atividade seja iniciada
 * em uma nova tarefa e que as tarefas existentes sejam limpas.
 *
 * Criado por Gilian Marques
 * Em sábado, 07 de junho de 2025 as 17:43.
 */
class ReportNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_ORIGINAL_INTENT = "original_intent"
        const val EXTRA_TARGET_PACKAGE = "target_package"
    }

    override fun onReceive(context: Context, intent: Intent) {

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val packageName = intent.getStringExtra(EXTRA_TARGET_PACKAGE)

        val targetIntent = if (packageName != null) {
            context.packageManager.getLaunchIntentForPackage(packageName)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_ORIGINAL_INTENT, Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_ORIGINAL_INTENT)
            }
        }

        if (notificationId == -1 || targetIntent == null) {
            Log.e("ReportReceiver", "Erro: notificationId=$notificationId, intent=$targetIntent")
            return
        }

        context.getSystemService(NotificationManager::class.java).cancel(notificationId)

        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(targetIntent)
    }
}
