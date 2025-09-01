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

package dev.gmarques.controledenotificacoes.framework.report_notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListenerManagerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 14 de maio de 2025 as 12:34.
 * Classe responsável por receber o evento de boot do dispositivo.
 * Garante a execução da [App] que executa as tarefas necessárias e inicializa
 * o [NotificationListenerManagerService] para garantir que o serviço de escuta de notificações
 * seja iniciado após o boot.
 */
class BootReceiver : BroadcastReceiver(), CoroutineScope by MainScope() {
    /**
     * Chamado quando o [BootReceiver] recebe uma intenção de broadcast.
     * Verifica se a ação da intenção é [Intent.ACTION_BOOT_COMPLETED] e, em caso afirmativo,
     * inicia o [NotificationListenerManagerService].
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AppLogger.d("")
            NotificationListenerManagerService.startIfNotAlready(context)
        }
    }

}
