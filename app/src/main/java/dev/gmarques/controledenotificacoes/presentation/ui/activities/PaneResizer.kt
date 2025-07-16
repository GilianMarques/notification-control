package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import dev.gmarques.controledenotificacoes.domain.framework.VibratorProvider

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 14 de julho de 2025 às 11:45.
 *
 * Classe responsável por obter a posição onde devem ser divididos os navhosts (em tablets ou maiores).
 * Define um touch listener que calcula a posição X da view-alvo como uma porcentagem da largura do pai
 * e retorna esse valor para o listener registrado. Também aplica animações de fade-in e fade-out durante o toque.
 *
 * @param handleParent View que o usuário pode arrastar.
 * @param listener Callback que recebe o valor entre 0.0f e 1.0f correspondente à posição horizontal do arrasto.
 */
class PaneResizer(
    private val handleParent: View,
    private val dragHandler: View,
    private val listener: PaneResizeListener,
    private val vibratorProvider: VibratorProvider,
) {

    private var downEventTimestamp = 0L
    private val animDuration = 200L
    private var originalHandleY = 0f

    init {
        handleParent.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onTouchStart()
                MotionEvent.ACTION_MOVE -> onTouchMove(event)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (System.currentTimeMillis() - downEventTimestamp <= 75) {
                        handleParent.performClick()
                    }
                    onTouchEnd()
                }
            }
            true
        }
    }

    private fun onTouchStart() {
        downEventTimestamp = System.currentTimeMillis()
        originalHandleY = dragHandler.y
        fadeIn(handleParent)
    }

    private fun onTouchMove(event: MotionEvent) {

        val parent = handleParent.parent as? View ?: return
        val x = event.rawX - parent.left
        val percent = (x / parent.width).coerceIn(0f, 1f)
        listener.onPaneResized(percent)
        moveHandleWithFinger(event.rawY)

        if ((percent * 100).toInt() % 2 == 0) vibratorProvider.interaction()

    }

    private fun onTouchEnd() {
        fadeOut(handleParent)
        animateHandleBackToOriginalY()
    }

    private fun moveHandleWithFinger(rawY: Float) {
        dragHandler.y = rawY - dragHandler.height / 2f
    }

    private fun animateHandleBackToOriginalY() {
        dragHandler.animate()
            .y(originalHandleY)
            .setInterpolator(AnticipateOvershootInterpolator())
            .setDuration(animDuration / 2)
            .start()
    }

    private fun fadeIn(view: View) {
        view.clearAnimation()
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(animDuration)
            .start()
    }

    private fun fadeOut(view: View) {
        view.clearAnimation()
        view.animate()
            .alpha(0f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(animDuration)
            .start()
    }

    interface PaneResizeListener {
        fun onPaneResized(positionPercent: Float)
    }
}
