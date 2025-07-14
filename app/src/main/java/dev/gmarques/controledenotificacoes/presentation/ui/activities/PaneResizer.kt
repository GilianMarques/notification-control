package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 14 de julho de 2025 às 11:45.
 *
 * Classe responsável por obter a posição onde devem ser divididos os navhosts (em tablets ou maiores).
 * Define um touch listener que calcula a posição X da view-alvo como uma porcentagem da largura do pai
 * e retorna esse valor para o listener registrado. Também aplica animações de fade-in e fade-out durante o toque.
 *
 * @param targetView View que o usuário pode arrastar.
 * @param listener Callback que recebe o valor entre 0.0f e 1.0f correspondente à posição horizontal do arrasto.
 */
class PaneResizer(
    private val targetView: View,
    private val listener: PaneResizeListener,
) {

    private var downEventTimestamp = 0L

    init {
        targetView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downEventTimestamp = System.currentTimeMillis()
                    fadeIn(targetView)
                }

                MotionEvent.ACTION_MOVE -> {
                    val parent = targetView.parent as? View ?: return@setOnTouchListener false
                    val x = event.rawX - parent.left
                    val percent = (x / parent.width).coerceIn(0f, 1f)
                    listener.onPaneResized(percent)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (System.currentTimeMillis() - downEventTimestamp <= 75) {
                        targetView.performClick()
                    }
                    fadeOut(targetView)
                }
            }
            true
        }
    }

    /**
     * Anima a view de forma suave para `alpha = 1` e a torna visível.
     * @see fadeOut
     */
    private fun fadeIn(view: View) {
        view.apply {
            clearAnimation()
            alpha = 0f
            animate().alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(300)
                .start()
        }
    }

    /**
     * Anima a view suavemente para `alpha = 0` e depois a torna `GONE`.
     * @see fadeIn
     */
    private fun fadeOut(view: View) {
        view.apply {
            clearAnimation()
            animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .alpha(0f)
                .setDuration(300)
                .start()
        }
    }

    /**
     * Interface para receber a posição percentual horizontal da view durante o arrasto.
     */
    interface PaneResizeListener {
        fun onPaneResized(positionPercent: Float)
    }
}
