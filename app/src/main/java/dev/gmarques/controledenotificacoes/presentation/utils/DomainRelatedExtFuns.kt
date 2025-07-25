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

package dev.gmarques.controledenotificacoes.presentation.utils

import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.Rule.Type

/**
 * Criado por Gilian Marques
 * Em terça-feira, 22 de abril de 2025 as 20:26.
 * Aqui ficam as funçoes de extençao relacionadas aos modelos de objetos de dominio dos quais a aplicação depende mas que nao podem
 * ficar nas classes de extfuns no pacote de dominio porque envolvem recursos da plataforma.
 */
object DomainRelatedExtFuns {

    fun Rule.getAdequateIconReference() = when (this.type) {
        Type.RESTRICTIVE -> R.drawable.vec_rule_restrictive
        Type.PERMISSIVE -> R.drawable.vec_rule_permissive
    }

    fun Rule.getAdequateIconReferenceSmall() = when (this.type) {
        Type.RESTRICTIVE -> R.drawable.vec_rule_restrictive_small
        Type.PERMISSIVE -> R.drawable.vec_rule_permissive_small
    }
}