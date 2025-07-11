package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import androidx.core.animation.doOnEnd
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneState.*

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
    private val stateListener = HashMap<String, SlidingPaneControllerCallback>()

    var state: SlidingPaneState = ONLY_MASTER
        private set

    private val masterView: View by lazy { activity.findViewById(masterId) }
    private val detailView: View by lazy { activity.findViewById(detailId) }

    /**Recebe a largura da tela, considerando a orientação do dispositivo (sempre recebe o valor horizontal da tela)*/
    private val screenWidth: Int
        get() = activity.resources.displayMetrics.widthPixels


    private val targetPercent = 0.66f // 2/3 da tela
    private val detailTargetWidth: Int
        get() = (screenWidth * targetPercent).toInt()


    fun showMasterAndDetails(callback: () -> Unit = {}) {

        if (state == BOTH) {
            callback.invoke()
            return
        }

        state = BOTH
        detailView.visibility = View.VISIBLE
        masterView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, screenWidth - detailTargetWidth)
        animateWidth(detailView, detailView.width, detailTargetWidth) {
            callback.invoke()
            stateListener.values.forEach { it.onSlidingPaneStateChanged(state) }
        }

    }

    fun showOnlyDetails(callback: () -> Unit = {}) {

        if (state == ONLY_DETAILS) {
            callback.invoke()
            return
        }

        state = ONLY_DETAILS
        detailView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, 0)
        animateWidth(detailView, detailView.width, screenWidth) {
            masterView.visibility = View.GONE
            callback.invoke()
            stateListener.values.forEach { it.onSlidingPaneStateChanged(state) }
        }

    }

    fun showOnlyMaster(callback: () -> Unit = {}) {

        if (state == ONLY_MASTER) {
            callback.invoke()
            return
        }

        state = ONLY_MASTER
        masterView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, screenWidth)
        animateWidth(detailView, detailView.width, 0) {
            detailView.visibility = View.GONE
            callback.invoke()
            stateListener.values.forEach { it.onSlidingPaneStateChanged(state) }
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

    /**
     * Adiciona um ouvinte para ser notificado sobre mudanças no estado do painel deslizante.
     *
     * O estado do painel deslizante pode ser:
     * - `ONLY_MASTER`: Apenas o painel mestre está visível.
     * - `BOTH`: Ambos os painéis, mestre e detalhes, estão visíveis.
     * - `ONLY_DETAILS`: Apenas o painel de detalhes está visível.
     *
     * @param caller O objeto que está registrando o ouvinte. Usado como chave para identificar o ouvinte.
     *               Geralmente, `this` é passado aqui pela classe que implementa `SlidingPaneControllerCallback`.
     *               Ajuda a Evitar callbacks duplicados e memoryleaks ao substituir um callback de um fragmento que fechou por um
     *               config change ou outro motivo
     * @param callback A implementação da interface `SlidingPaneControllerCallback` que será chamada quando o estado mudar.
     */
    fun addStateListener(caller: Any, callback: SlidingPaneControllerCallback) {
        stateListener[caller.javaClass.simpleName] = callback
    }

    enum class SlidingPaneState(val value: Int) {
        ONLY_MASTER(-1), BOTH(1), ONLY_DETAILS(1)
    }

    interface SlidingPaneControllerCallback {
        fun onSlidingPaneStateChanged(newState: SlidingPaneState)
    }
}