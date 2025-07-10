package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import androidx.core.animation.doOnEnd

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 09 de julho de 2025 as 15:07.
 */
class SlidingPaneController(
    private val activity: Activity,
    private val masterId: Int,
    private val detailId: Int,
) {
    /**Avisa sempre que o estado muda aberto/fechado*/
    lateinit var stateListener: (state: SlidingPaneState) -> Any

    lateinit var state: SlidingPaneState
    private val masterView: View by lazy { activity.findViewById(masterId) }
    private val detailView: View by lazy { activity.findViewById(detailId) }

    /**Recebe a largura da tela, considerando a orientação do dispositivo (sempre recebe o valor horizontal da tela)*/
    private val screenWidth: Int
        get() = activity.resources.displayMetrics.widthPixels


    private val targetPercent = 0.66f // 2/3 da tela
    private val detailTargetWidth: Int
        get() = (screenWidth * targetPercent).toInt()


    fun toggleState(slidingPaneState: SlidingPaneState, callback: () -> Any = {}) {

        this@SlidingPaneController.state = slidingPaneState

        when (slidingPaneState) {
            SlidingPaneState.ONLY_MASTER -> collapseDetails(callback)
            SlidingPaneState.BOTH -> expandDetails(callback)
            SlidingPaneState.ONLY_DETAILS -> expandDetailsToEdge(callback)
        }

    }

    private fun expandDetails(callback: () -> Any = {}) {
        detailView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, screenWidth - detailTargetWidth)
        animateWidth(detailView, detailView.width, detailTargetWidth) {
            callback.invoke()
            stateListener.invoke(state)
        }

    }

    private fun expandDetailsToEdge(callback: () -> Any = {}) {
        detailView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, 0)
        animateWidth(detailView, detailView.width, screenWidth) {
            callback.invoke()
            stateListener.invoke(state)
        }

    }

    private fun collapseDetails(callback: () -> Any = {}) {
        animateWidth(masterView, masterView.width, screenWidth)
        animateWidth(detailView, detailView.width, 0) {
            detailView.visibility = View.GONE
            callback.invoke()
            stateListener.invoke(state)
        }
    }

    private fun animateWidth(
        view: View,
        from: Int,
        to: Int,
        onEnd: (() -> Unit)? = null,
    ) {
        ValueAnimator.ofInt(from, to).apply {
            duration = 200
            addUpdateListener { animator ->
                val params = view.layoutParams
                params.width = animator.animatedValue as Int
                view.layoutParams = params
            }
            doOnEnd { onEnd?.invoke() }
            start()
        }
    }

    enum class SlidingPaneState(val value: Int) {
        ONLY_MASTER(-1), BOTH(1), ONLY_DETAILS(1)
    }
}