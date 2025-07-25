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

@file:Suppress("ClassName")

package dev.gmarques.controledenotificacoes.domain

/**
 * Representa o resultado de uma operação que pode falhar com um erro [Exception]
 * ou ter sucesso com um valor [Value].
 * Criado por Gilian Marques
 * Em 20/06/2025 as 17:34
 *
 * É um alternativa ao Result padrão do kotlin, que permite especificar o tipo de exceçao e retorno, permitidno o
 * uso de exceçoes com classes seladas e when.
 *
 *  @param Exception O tipo de exceção que pode ser lançada durante a operação.
 * @param Value O tipo de valor que será retornado em caso de sucesso.
 *
 */
sealed class OperationResult<out Exception, out Value> {

    /**
     * Representa um resultado de sucesso, contendo o valor da operação.
     * @param value O valor resultante da operação bem-sucedida.
     */
    data class success<Value>(val value: Value) : OperationResult<Nothing, Value>()

    /**
     * Representa um resultado de falha, contendo o erro ocorrido durante a operação.
     * @param error A exceção que causou a falha da operação.
     */
    data class failure<Exception>(val error: Exception) : OperationResult<Exception, Nothing>()

    /**
     * Verifica se o resultado da operação foi um sucesso.
     * @return `true` se a operação foi bem-sucedida, `false` caso contrário.
     */
    val isSuccess: Boolean get() = this is success

    /**
     * Verifica se o resultado da operação foi uma falha.
     * @return `true` se a operação falhou, `false` caso contrário.
     */
    val isFailure: Boolean get() = this is failure

    /**
     * Retorna o valor do resultado se a operação foi bem-sucedida, ou `null` se falhou.
     * @return O valor de sucesso ou `null`.
     */
    fun getOrNull(): Value? = when (this) {
        is success -> value
        is failure -> null
    }

    /**
     * Retorna a exceção se a operação falhou, ou `null` se foi bem-sucedida.
     * @return A exceção da falha ou `null`.
     */
    fun exceptionOrNull(): Exception? = when (this) {
        is failure -> error
        is success -> null
    }

    /**
     * Retorna o valor do resultado se a operação foi bem-sucedida, ou lança uma exceção se falhou.
     * @param mapError Uma função opcional para transformar a exceção original em outra `Throwable`.
     *                 Por padrão, lança uma `RuntimeException` com a mensagem "Erro: [erro]".
     * @return O valor de sucesso.
     * @throws Throwable A exceção mapeada (ou a original se `mapError` não for fornecida) se a operação falhou.
     */
    fun getOrThrow(mapError: (Exception) -> Throwable = { RuntimeException("Erro: $it") }): Value = when (this) {
        is success -> value
        is failure -> throw mapError(error)
    }
}

