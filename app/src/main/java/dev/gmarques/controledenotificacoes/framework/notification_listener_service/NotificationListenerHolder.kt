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

import dev.gmarques.controledenotificacoes.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Classe responsável por gerenciar a instância do [NotificationListener] e notificar
 * o [SystemNotificationManagerImpl] sobre o estado da conexão.
 *
 * Esta classe atua como um intermediário, permitindo que o [NotificationListener] (que é um Service
 * do Android e tem um ciclo de vida gerenciado pelo sistema) comunique seu estado de conexão
 * ao [SystemNotificationManagerImpl] de forma desacoplada, utilizando injeção de dependência.
 *
 * O [NotificationListener] define sua instância aqui quando está conectado e a remove quando é desconectado.
 * Dessa forma, o [SystemNotificationManagerImpl] pode acessar a instância do listener de forma segura.
 */
@Singleton
class NotificationListenerHolder @Inject constructor() {

    /**
     * Instância atual do [NotificationListener].
     * A anotação `@Volatile` garante que as escritas nesta variável sejam visíveis para todas as threads
     * imediatamente, prevenindo problemas de concorrência ao acessar o listener de diferentes partes do código.
     */
    @Volatile
    private var listener: NotificationListener? = null
    private var callback: ConnectionCallback? = null

    /**
     * Define a instância do [NotificationListener] e notifica o [callback] sobre a mudança de conexão.
     *
     * Esta função deve ser chamada exclusivamente pela classe [NotificationListener] para indicar
     * seu estado de conexão (conectado ou desconectado).
     *
     * @param service A instância do [NotificationListener] quando conectado, ou `null` quando desconectado.
     * @throws IllegalAccessException Se a chamada não for originada da classe [NotificationListener].
     */
    fun setListener(service: NotificationListener?) {
        if (BuildConfig.DEBUG) {
            val callerClass = Class.forName(Throwable().stackTrace[1].className)
            if (callerClass != NotificationListener::class.java) {
                throw IllegalAccessException("setListener só pode ser chamado de NotificationListener. Chamador: $callerClass")
            }
        }

        listener = service
        callback?.onConnectionChanged(listener)
    }

    /**
     * Obtém a instância atual do [NotificationListener].
     *
     * Esta função deve ser chamada exclusivamente pela classe [SystemNotificationManagerImpl]
     * para acessar o listener e interagir com as notificações do sistema.
     *
     * @return A instância do [NotificationListener], ou `null` se não estiver conectado.
     * @throws IllegalAccessException Se a chamada não for originada da classe [SystemNotificationManagerImpl].
     */
    @Suppress("unused")
    fun getListener(): NotificationListener? {
        throwIfInvalidCallerOnDebug("getListener")
        return listener
    }

    /**
     * Registra um [ConnectionCallback] para ser notificado sobre mudanças no estado da conexão
     * com o [NotificationListener].
     *
     * O callback fornecido será invocado imediatamente após o registro (com o estado atual da conexão)
     * e, subsequentemente, toda vez que o [NotificationListener] for conectado ou desconectado.
     *
     * Esta função deve ser chamada exclusivamente pela classe [SystemNotificationManagerImpl]
     * para monitorar a disponibilidade do listener.
     *
     * @param callback O [ConnectionCallback] a ser registrado para receber atualizações de status.
     * @throws IllegalAccessException Se a chamada não for originada da classe [SystemNotificationManagerImpl].
     */
    fun registerCallback(callback: ConnectionCallback) {
        throwIfInvalidCallerOnDebug("registerCallback")
        this.callback = callback
        // Notifica imediatamente o callback com o estado atual do listener
        callback.onConnectionChanged(listener)
    }

    /**
     * Garante que o mét.odo chamador seja da classe [SystemNotificationManagerImpl].
     *
     *  @param callerFunction O nome da função que está sendo chamada.
     * @throws IllegalAccessException se o chamador não for [SystemNotificationManagerImpl].
     */
    private fun throwIfInvalidCallerOnDebug(callerFunction: String) {
        if (!BuildConfig.DEBUG) return

        val callerClass = Class.forName(Throwable().stackTrace[2].className)
        if (callerClass != SystemNotificationManagerImpl::class.java) {
            throw IllegalAccessException("$callerFunction só pode ser chamado de SystemNotificationManagerImpl. Chamador: $callerClass")
        }
    }

    /**
     * Interface para notificar o [SystemNotificationManagerImpl] sobre mudanças na conexão
     * com o [NotificationListener].
     */
    interface ConnectionCallback {
        /**
         * Chamado quando o estado da conexão com o [NotificationListener] muda.
         *
         * @param listener A instância do [NotificationListener] se conectado, ou `null` se desconectado.
         */
        fun onConnectionChanged(listener: NotificationListener?)
    }
}
