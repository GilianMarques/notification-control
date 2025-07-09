package dev.gmarques.controledenotificacoes.presentation.utils

import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 09 de julho de 2025 as 15:07.
 */
class SlidingPaneController(
    private val activity: Activity,
    private val masterId: Int,
    private val detailId: Int,
) {

    private val masterView: View by lazy { activity.findViewById(masterId) }
    private val detailView: View by lazy { activity.findViewById(detailId) }

    private val screenWidth: Int
        get() = activity.resources.displayMetrics.widthPixels

    private val targetPercent = 0.65f
    private val detailTargetWidth: Int
        get() = (screenWidth * targetPercent).toInt()

    fun expand() {
        detailView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, screenWidth - detailTargetWidth)
        animateWidth(detailView, detailView.width, detailTargetWidth)

    }

    fun collapse() {
        animateWidth(masterView, masterView.width, screenWidth)
        animateWidth(detailView, detailView.width, 0) {
            detailView.visibility = View.GONE
        }
    }

    fun isExpanded(): Boolean = detailView.isVisible

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
}