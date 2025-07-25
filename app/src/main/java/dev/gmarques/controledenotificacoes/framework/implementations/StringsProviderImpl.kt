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

package dev.gmarques.controledenotificacoes.framework.implementations

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.framework.contracts.StringsProvider
import javax.inject.Inject

/**
 * Tem como proposito garantir que a camada de dominio nao dependa diretamente da plataforma
 * garantindo o principio de separação de camadas da clean architecture.
 *
 * Implementação concreta de [StringsProvider] que fornece strings localizadas
 * para termos relacionados a regras.
 *
 * Esta classe utiliza o contexto da aplicação para acessar os recursos de string de
 * `R.string`. Ela fornece representações em string para os dias da semana (abreviados)
 * e tipos de regras (permissiva e restritiva).
 *
 * @property context O contexto da aplicação usado para recuperar os recursos de string.
 * @constructor Injeta o contexto da aplicação.
 */

class StringsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : StringsProvider {

    override fun monday(): String = context.getString(R.string.segunda_abrev)


    override fun tuesday(): String = context.getString(R.string.terca_abrev)


    override fun wednesday(): String = context.getString(R.string.quarta_abrev)


    override fun thursday(): String = context.getString(R.string.quinta_abrev)


    override fun friday(): String = context.getString(R.string.sexta_abrev)


    override fun saturday(): String = context.getString(R.string.sabado_abrev)


    override fun sunday(): String = context.getString(R.string.domingo_abrev)


    override fun permissive(): String = context.getString(R.string.regra_tipo_permissiva)

    override fun restrictive(): String = context.getString(R.string.regra_tipo_restritiva)

    override fun wholeDay(): String = context.getString(R.string.Dia_inteiro)

    override fun everyDay(): String = context.getString(R.string.Todos_os_dias)
    override fun guest(): String = context.getString(R.string.Convidado)

}