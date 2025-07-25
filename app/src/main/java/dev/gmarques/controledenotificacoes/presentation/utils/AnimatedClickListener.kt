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

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.ScaleAnimation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [AnimatedClickListener] é um [View.OnClickListener] personalizado que fornece um feedback visual
 * ao usuário ao clicar em uma view. Isso é feito redimensionando a view brevemente, simulando
 * um efeito de pressionamento, e disparando uma ação personalizada após a conclusão da animação.
 *
 * @property action Uma função lambda que representa a ação a ser executada após o término da animação de clique.
 */
class AnimatedClickListener(private val action: () -> Unit) : View.OnClickListener {

    override fun onClick(v: View) {
        animateView(v)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun animateView(view: View) = GlobalScope.launch(Main) {
        val animation = ScaleAnimation(
            1f, 0.96f,
            1f, 0.96f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ScaleAnimation.REVERSE
            repeatCount = 1
        }

        view.startAnimation(animation)
        action.invoke()
    }
}
