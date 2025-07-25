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

package dev.gmarques.controledenotificacoes.framework

import android.app.PendingIntent
import android.util.Log

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 02 de junho de 2025 as 09:21.
 *
 * Armazena na memoria ram as referencias das pendin intents das notificações, o que permite
 * 'abrir' as notificações de dentro do app.
 */

object PendingIntentCache {

    private val cache = HashMap<String, PendingIntent>()

    operator fun invoke(key: String): PendingIntent? = cache[key]

    fun add(key: String, pendingIntent: PendingIntent) {
        cache[key] = pendingIntent
    }

    /**
     * Removes from cache all pendingIntents related to a specific package
     */
    fun removeAllFrom(packageName: String) {
        cache.keys.filter { it.contains(packageName) }
            .forEach {
                cache.remove(it)
                Log.d("USUK", "PendingIntentCache.clearAllFrom: removing pendingIntent with key: ${it}")
            }
    }

    fun remove(pendingIntentId: String) {
        cache.remove(pendingIntentId)
    }

}