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

package dev.gmarques.controledenotificacoes.presentation

/**
 * Criado por Gilian Marques
 * Em sábado, 12 de abril de 2025 as 16:58.
 * Essa classe auxilia no envio de eventos para a UI
 */
class EventWrapper<T>(val event: T?) {

    private var consumed = false

    /**
     * Consome o evento, marcando-o como processado.
     *
     * Esta função recupera o evento se ele ainda não tiver sido consumido e o marca como consumido.
     * Chamadas subsequentes a esta função retornarão `null`.
     *
     * @return O evento se ele não foi consumido anteriormente, `null` caso contrário.
     */
    fun consume(): T? {
        if (consumed) return null
        consumed = true
        return event
    }

}