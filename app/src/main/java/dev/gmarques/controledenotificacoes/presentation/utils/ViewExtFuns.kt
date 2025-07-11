package dev.gmarques.controledenotificacoes.presentation.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView


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

}

