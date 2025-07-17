package dev.gmarques.controledenotificacoes.presentation.ui.activities

import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl.detailsPaneScreenPercent
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneState.BOTH
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneState.ONLY_DETAILS
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneState.ONLY_MASTER

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 09 de julho de 2025 as 15:07.
 */
class SlidingPaneController(
    private val activity: Activity,
    masterId: Int,
    detailId: Int,
) {

    companion object {
        const val DEFAULT_TARGET_PERCENT = 0.66f // 2/3 da tela
        const val MIN_TARGET_PERCENT = 0.3f
        const val MAX_TARGET_PERCENT = 0.7f
        const val DEFAULT_ANIM_DURATION = 200L
    }

    var isAnimating: Boolean = false
        private set

    /**Avisa sempre que o estado muda aberto/fechado*/
    private val stateListener = HashMap<String, SlidingPaneControllerCallback>()

    var state: SlidingPaneState = ONLY_MASTER
        private set

    private val masterView: View = activity.findViewById(masterId)
    private val detailView: View = activity.findViewById(detailId)

    /**Recebe a largura da tela, considerando a orientação do dispositivo (sempre recebe o valor horizontal da tela)*/
    private val screenWidth: Int
        get() = activity.resources.displayMetrics.widthPixels

    private val targetPercent: Float
        get() = detailsPaneScreenPercent.value

    private val animDuration = DEFAULT_ANIM_DURATION
    private val detailTargetWidth: Int
        get() = (screenWidth * targetPercent).toInt()


    fun showMasterAndDetails(callback: () -> Unit = {}) {

        if (state == BOTH) {
            callback.invoke()
            return
        }

        stateListener.values.forEach { it.onAnimationStarted(state) }
        state = BOTH
        detailView.visibility = View.VISIBLE
        masterView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, screenWidth - detailTargetWidth)
        animateWidth(detailView, detailView.width, detailTargetWidth) {
            callback.invoke()
            stateListener.values.forEach { it.onAnimationEnd(state) }
        }

    }

    fun showOnlyDetails(callback: () -> Unit = {}) {

        if (state == ONLY_DETAILS) {
            callback.invoke()
            return
        }

        stateListener.values.forEach { it.onAnimationStarted(state) }
        state = ONLY_DETAILS
        detailView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, 0)
        animateWidth(detailView, detailView.width, screenWidth) {
            masterView.visibility = View.GONE
            callback.invoke()
            stateListener.values.forEach { it.onAnimationEnd(state) }
        }

    }

    fun showOnlyMaster(callback: () -> Unit = {}) {

        if (state == ONLY_MASTER) {
            callback.invoke()
            return
        }

        stateListener.values.forEach { it.onAnimationStarted(state) }
        state = ONLY_MASTER
        masterView.visibility = View.VISIBLE

        animateWidth(masterView, masterView.width, screenWidth)
        animateWidth(detailView, detailView.width, 0) {
            detailView.visibility = View.GONE
            callback.invoke()
            stateListener.values.forEach { it.onAnimationEnd(state) }
        }
    }

    private fun animateWidth(
        view: View,
        from: Int,
        to: Int,
        onEnd: (() -> Unit)? = null,
    ) {
        isAnimating = true
        ValueAnimator.ofInt(from, to).apply {
            duration = animDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val params = view.layoutParams
                params.width = animator.animatedValue as Int
                view.layoutParams = params
            }
            doOnEnd {
                isAnimating = false
                onEnd?.invoke()
            }
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

    /**
     * Ajusta a largura dos painéis master e details  quando o usuário redimensiona manualmente a divisão.
     *
     * Pontos chave:
     * - Valida se `newPercent` está entre `MIN_TARGET_PERCENT` e `MAX_TARGET_PERCENT`.
     * - Atualiza a preferência da largura do painel de detalhes
     * - Se ambos os painéis estiverem visíveis (`state == BOTH`), recalcula e aplica as novas larguras e solicita um novo layout.
     *
     * @param newPercent A nova porcentagem da largura da tela que o painel mestre deve ocupar.
     *                   Este valor deve estar entre `MIN_TARGET_PERCENT` e `MAX_TARGET_PERCENT`.
     */
    fun onPaneResizedByHand(newPercent: Float) {

        if (newPercent !in MIN_TARGET_PERCENT..MAX_TARGET_PERCENT || state != BOTH) return
        detailsPaneScreenPercent.set(1 - newPercent)

        val masterWidth = (screenWidth * newPercent).toInt()
        val detailWidth = screenWidth - masterWidth
        masterView.layoutParams.width = masterWidth
        detailView.layoutParams.width = detailWidth
        masterView.requestLayout()
        detailView.requestLayout()

    }

    enum class SlidingPaneState(val value: Int) {
        ONLY_MASTER(-1), BOTH(1), ONLY_DETAILS(1)
    }

    interface SlidingPaneControllerCallback {
        fun onAnimationStarted(currentState: SlidingPaneState)
        fun onAnimationEnd(newState: SlidingPaneState)
    }
}