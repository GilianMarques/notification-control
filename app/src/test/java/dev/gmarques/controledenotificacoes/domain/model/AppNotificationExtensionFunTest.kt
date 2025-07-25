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

import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.bitmapId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.pendingIntentId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.timeFormatted
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AppNotificationExtensionFunTest {

    @Test
    fun quando_data_for_hoje_timeFormatted_deve_retornar_hora_e_minutos() {
        val timestamp = System.currentTimeMillis()
        val notification = AppNotification("com.app", "titulo", "conteudo", timestamp)

        val expected = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        val result = notification.timeFormatted()

        assertEquals(expected, result)
    }

    @Test
    fun quando_data_for_antiga_timeFormatted_deve_retornar_data_completa() {
        val oldTimestamp = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        val notification = AppNotification("com.app", "titulo", "conteudo", oldTimestamp)

        val expected = SimpleDateFormat("EEEE, dd/MM HH:mm", Locale.getDefault()).format(Date(oldTimestamp))
        val result = notification.timeFormatted()

        assertEquals(expected, result)
    }

    @Test
    fun pendingIntentId_deve_retornar_packageName_mais_timestamp() {
        val notification = AppNotification("com.app", "titulo", "conteudo", 123456789L)

        val result = notification.pendingIntentId()

        assertEquals("com.app_123456789", result)
    }

    @Test
    fun bitmapId_deve_gerar_id_com_caracteres_validos() {
        val notification = AppNotification(
            packageName = "com.exemplo.app",
            title = "Titulo Notificacao!",
            content = "Conteudo #1",
            postTime = 123456789L
        )

        val result = notification.bitmapId()

        val expected = "comexemploappTituloNotificacaoConteudo1.png"
        assertEquals(expected, result)
    }
}
