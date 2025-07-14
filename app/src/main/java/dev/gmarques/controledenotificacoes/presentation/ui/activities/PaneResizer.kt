package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.view.MotionEvent
import android.view.View

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 14 de julho de 2025 às 11:45.
 *
 * Classe responsável por obter a posição onde devem ser divididos os navhosts (em tablets ou maiores)
 * Ela define um touchlistener em uma view alvo e observa o movimento de arrastar, calculando a porcentagem referente a posição
 * X da view na tela e retorna esse valor para um listener.
 *
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
                }

                MotionEvent.ACTION_MOVE -> {  // a magica acontece aqui
                    val parent = targetView.parent as? View ?: return@setOnTouchListener false
                    val x = event.rawX - parent.left
                    val percent = (x / parent.width).coerceIn(0f, 1f)
                    listener.onPaneResized(percent)
                }

                MotionEvent.ACTION_UP -> { // passa a frente eventos de clique como solicitado pela IDE
                    if (System.currentTimeMillis() - downEventTimestamp <= 75) {
                        targetView.performClick()
                    }
                }
            }
            true
        }
    }

    interface PaneResizeListener {
        fun onPaneResized(positionPercent: Float)
    }


}