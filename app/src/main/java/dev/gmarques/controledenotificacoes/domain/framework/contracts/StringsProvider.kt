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

package dev.gmarques.controledenotificacoes.domain.framework.contracts

/**
 *
 * Fornece recursos de strings relacionados a regras de negócio e dias da semana.
 *
 * Esta interface garante que a camada de domínio permaneça independente da plataforma ou framework específico,
 * aderindo ao princípio de separação de responsabilidades da Clean Architecture. Ela permite que a camada de
 * domínio acesse representações em string de regras e dias sem depender diretamente de recursos de string
 * específicos da plataforma como o Context.
 *
 * Criado por Gilian Marques
 * Em Quarta-feira, 16 de abril de 2025, às 20:51.
 */
interface StringsProvider {

    fun monday(): String
    fun tuesday(): String
    fun wednesday(): String
    fun thursday(): String
    fun friday(): String
    fun saturday(): String
    fun sunday(): String

    fun permissive(): String
    fun restrictive(): String
    fun wholeDay(): String
    fun everyDay(): String

    fun guest(): String

}