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

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.canUseReadMoreFeature
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.readMoreFeature


/**
 * Criado por Gilian Marques
 * Em terça-feira, 01 de abril de 2025 as 00:09.
 */
object ViewExtFuns {

    /**
     * Adiciona uma view filha ao ViewGroup com uma animação de duas etapas.
     *
     * A view é adicionada ao container oculta e, após um breve atraso, torna-se visível,
     * dando tempo do container ajustar suas dimensoes antes da view ser exibida com um fade-in.
     *
     * @param child A view filha a ser adicionada.
     * @param index O índice onde a view será adicionada. -1 para adicionar ao final (padrão).
     * @throws IllegalArgumentException Se o índice estiver fora do intervalo (< -1 ou > childCount).
     */
    fun ViewGroup.addViewWithTwoStepsAnimation(child: View, index: Int = -1) {

        child.visibility = View.INVISIBLE
        addView(child, if (index != -1) index else childCount)

        child.postDelayed({
            child.visibility = View.VISIBLE
        }, 300)
    }

    /**
     * Define um drawable à esquerda (start) de um TextView.
     *
     * Esta função de extensão simplifica a definição de um drawable no início do texto
     * de um TextView, mantendo os outros drawables (topo, direita, baixo) como nulos.
     *
     * @param adequatedDrawable O Drawable a ser definido à esquerda do texto.
     *                          Pode ser qualquer objeto Drawable, como um ColorDrawable,
     *                          BitmapDrawable, etc.
     */
    fun TextView.setStartDrawable(adequatedDrawable: Drawable) {
        this.setCompoundDrawablesWithIntrinsicBounds(
            adequatedDrawable,
            null,
            null,
            null
        )
    }

    /**
     * Remove todos os drawables (esquerda, topo, direita, baixo) de um TextView.
     *
     * Esta função de extensão define todos os drawables compostos do TextView como nulos,
     * efetivamente removendo qualquer imagem ou ícone que estava associado ao texto.
     *
     * @see TextView.setCompoundDrawablesWithIntrinsicBounds
     */
    fun TextView.removeDrawables() {
        this.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            null,
            null
        )
    }

    /**
     * Exibe o teclado virtual para a View especificada.
     *
     * Esta função de extensão primeiro solicita o foco para a View e, em seguida,
     * utiliza o InputMethodManager para mostrar o teclado virtual.
     *
     * @see InputMethodManager.showSoftInput
     * @see InputMethodManager.SHOW_IMPLICIT
     */
    fun View.showKeyboard() {
        this.requestFocus()

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * Esconde o teclado virtual se ele estiver atualmente visível e associado a esta View.
     *
     * Esta função de extensão primeiro solicita o foco para a View (para garantir que
     * o contexto do InputMethodManager esteja correto) e, em seguida, utiliza o
     * InputMethodManager para ocultar o teclado virtual da janela associada à View.
     *
     * @see InputMethodManager.hideSoftInputFromWindow
     * @see InputMethodManager.HIDE_NOT_ALWAYS
     */
    fun View.hideKeyboard() {
        this.requestFocus()

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * Reatribui o adapter do RecyclerView para forçar a recriação das views.
     *
     * Esta função é útil para acionar animações de transição ao mudar o layout do RecyclerView,
     * por exemplo, ao alternar entre uma visualização em lista e em grade com múltiplas colunas.
     *
     * Embora não seja estritamente necessário reatribuir o adapter (o LayoutManager
     * geralmente ajusta as colunas e tamanhos automaticamente), esta abordagem garante
     * que as views sejam recriadas, possibilitando a animação de transição desejada.
     */
    fun RecyclerView.rebindAdapter() {
        val adapter = this.adapter
        this.adapter = null
        this.adapter = adapter
    }

    /**
     * Implementa a funcionalidade "Leia mais" para um TextView.
     *
     * Esta função de extensão encurta um texto longo exibido em um TextView,
     * adicionando um link "Leia mais" no final. Ao clicar no link, o texto completo
     * é exibido. Clicar novamente retorna ao texto encurtado.
     *
     * A quebra do texto é determinada pelo valor `split_hint_max_chars` definido nos recursos.
     * O texto do link "Leia mais" é obtido do recurso de string `R.string.Leia_mais`.
     * O link "Leia mais" é estilizado com sublinhado e a cor padrão de link do tema.
     *
     * @param msg O texto completo a ser exibido. use [canUseReadMoreFeature] para determinar se deve chamar essa função.
     * @param callback Uma função de callback opcional que é invocada sempre que o estado
     *                 de visibilidade do texto (completo ou encurtado) é alterado.
     *                 O parâmetro `fullText` do callback será `true` se o texto completo
     *                 estiver sendo exibido, e `false` caso contrário.
     *
     * @throws IllegalArgumentException Se o texto for muito curto para aplicar a funcionalidade.
     */
    fun TextView.readMoreFeature(msg: String, callback: (fullText: Boolean) -> Unit = {}) {

        val splitHintAt = resources.getInteger(R.integer.split_hint_max_chars)
        if (splitHintAt >= msg.length) error("Nao passe mensagens curtas para essa função")
        val readMore = context.getString(R.string.Leia_mais)

        val shortenedHint = SpannableString(
            msg.substring(0, splitHintAt)
                .plus("… ")
                .plus(readMore)
        )

        shortenedHint.apply {
            setSpan(
                UnderlineSpan(),
                shortenedHint.length - readMore.length,
                shortenedHint.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            setSpan(
                ForegroundColorSpan(linkTextColors.defaultColor),
                shortenedHint.length - readMore.length,
                shortenedHint.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val toggleVisibility = {
            text = if (text == shortenedHint) msg else shortenedHint
            callback.invoke(text == msg)
        }.apply { invoke() }

        setOnClickListener {
            toggleVisibility.invoke()
        }
    }

    /**
     * Verifica se a funcionalidade "Leia mais" pode ser aplicada a um determinado texto
     * em um TextView.
     *
     * A funcionalidade "Leia mais" só é aplicável se o comprimento do texto (`msg`)
     * for maior do que o limite de caracteres definido em `R.integer.split_hint_max_chars`.
     *
     * @param msg O texto a ser verificado.
     * @return `true` se a funcionalidade "Leia mais" puder ser usada (ou seja, se o texto
     *         for mais longo que o limite), `false` caso contrário.
     * @see readMoreFeature
     */
    fun TextView.canUseReadMoreFeature(msg: String): Boolean {
        val splitHintAt = resources.getInteger(R.integer.split_hint_max_chars)
        return splitHintAt < msg.length
    }
}
