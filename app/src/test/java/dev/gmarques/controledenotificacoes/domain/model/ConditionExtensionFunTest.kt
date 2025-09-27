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

package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.ConditionExtensionFun.isSatisfiedBy
 
 
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConditionExtensionFunTest {

    @Test
    fun `retorna verdadeiro quando palavra chave esta no titulo com sensibilidade desligada`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("boleto"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Novo BOLETO disponível",
            "verifique o app",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna falso quando palavra chave esta no titulo mas com case sensitive ligado`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("boleto"),
            caseSensitive = true
        )
        val notification = AppNotification(
            "pkg",
            "Novo BOLETO disponível",
            "verifique o app",
            postTime = 123456789L
        )

        assertFalse(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro quando palavra chave esta no conteudo`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.CONTENT,
            keywords = listOf("disponível"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Titulo qualquer",
            "Novo boleto disponível",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna falso quando palavra chave nao esta no campo especificado`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("urgente"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Pagamento disponível",
            "Mensagem URGENTE",
            postTime = 123456789L
        )

        assertFalse(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro quando palavra chave esta tanto no titulo quanto no conteudo`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.BOTH,
            keywords = listOf("banco"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Banco atualizou",
            "Seu banco enviou um aviso",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro se ao menos uma palavra chave corresponder`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf(
                "nada",
                "teste",
                "boleto"
            ),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Você tem um boleto",
            "mensagem irrelevante",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna falso quando nenhuma palavra chave corresponde`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.CONTENT,
            keywords = listOf(
                "urgente",
                "erro"
            ),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Titulo qualquer",
            "tudo certo com seu app",
            postTime = 123456789L
        )

        assertFalse(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna falso quando lista de palavras chave esta vazia`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.BOTH,
            keywords = emptyList(),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Qualquer título",
            "Qualquer conteúdo",
            postTime = 123456789L
        )

        assertFalse(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro com case sensitive ativado e correspondencia exata`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("BOLETO"),
            caseSensitive = true
        )
        val notification = AppNotification(
            "pkg",
            "BOLETO gerado",
            "confira no app",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna falso quando campo especificado e vazio`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.CONTENT,
            keywords = listOf("importante"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Notificação vazia",
            "",
            postTime = 123456789L
        )

        assertFalse(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro quando titulo e conteudo combinados contem a palavra chave`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.BOTH,
            keywords = listOf("verifique"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Notificação",
            "Por favor," +
                    " verifique agora",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro com acentuacao mesmo com case insensitive`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("ação"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Nova AÇÃO requerida",
            "leia com atenção",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro quando palavra chave possui emoji`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.CONTENT,
            keywords = listOf("🚨urgente"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Aviso",
            "🚨URGENTE! Verifique já",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna true mesmo quando palavra chave e substring de outra`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("ban"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Banco notificou",
            "veja os detalhes",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro com grande volume de palavras chave`() {
        val keywords = List(1000) { "chave$it" } + "alvo"
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.CONTENT,
            keywords = keywords,
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Sem importância",
            "conteúdo com Alvo esperado",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna falso com campo titulo e conteudo vazios`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.BOTH,
            keywords = listOf("qualquer"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "",
            "",
            postTime = 123456789L
        )

        assertFalse(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro quando palavra chave e composta e esta no titulo`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.TITLE,
            keywords = listOf("pague agora"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Por favor," +
                    " pague agora seu boleto",
            "Aviso do sistema",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }

    @Test
    fun `retorna verdadeiro mesmo com repeticoes da palavra chave`() {
        val condition = Condition(
            type = Condition.Type.ONLY_IF,
            field = Condition.NotificationField.CONTENT,
            keywords = listOf("teste"),
            caseSensitive = false
        )
        val notification = AppNotification(
            "pkg",
            "Titulo",
            "Este é um teste teste teste",
            postTime = 123456789L
        )

        assertTrue(condition.isSatisfiedBy(notification))
    }
}
