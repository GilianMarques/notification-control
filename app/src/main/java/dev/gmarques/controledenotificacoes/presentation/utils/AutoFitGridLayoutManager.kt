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
import android.util.DisplayMetrics
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

/**
 * Criado por Gilian Marques
 * Em terça-feira, 08 de julho de 2025 as 14:33.
 *
 * Um GridLayoutManager personalizado que ajusta automaticamente o número de colunas.
 */
class AutoFitGridLayoutManager(
    context: Context,
    itemWidthDp: Int,
    private val spanCountChangeListener: (spanCount: Int) -> Unit = {},
) : GridLayoutManager(context, 1) {

    private var itemWidthPx = 0
    private var newSpanCount = 1

    init {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        itemWidthPx = (itemWidthDp * displayMetrics.density).toInt()
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        updateSpanCount()
        super.onLayoutChildren(recycler, state)
    }

    private fun updateSpanCount() {
        if (itemWidthPx <= 0) return

        val totalSpace = if (orientation == VERTICAL) width - paddingRight - paddingLeft
        else height - paddingTop - paddingBottom

        newSpanCount = max(1, totalSpace / itemWidthPx)

        if (newSpanCount != spanCount) {
            spanCount = newSpanCount
            spanCountChangeListener.invoke(spanCount)
        }

    }
}
