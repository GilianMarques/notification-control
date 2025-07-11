package dev.gmarques.controledenotificacoes.presentation.utils

import android.content.Context
import android.os.SystemClock
import android.util.DisplayMetrics
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

/**
 * Criado por Gilian Marques
 * Em terça-feira, 08 de julho de 2025 as 14:33.
 */
class AutoFitGridLayoutManager(
    context: Context,
    itemWidthDp: Int,
    private val spanCountChangeListener: (spanCount: Int) -> Unit,
) : GridLayoutManager(context, 1) {

    private var itemWidthPx = 0
    private var lastUpdateTime = 0L

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

        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastUpdateTime < 75) return // throttle: necessario pra suavizar a transição  do painel de detalhes

        val totalSpace = if (orientation == VERTICAL) width - paddingRight - paddingLeft
        else height - paddingTop - paddingBottom

        val newSpanCount = max(1, totalSpace / itemWidthPx)
        if (newSpanCount != spanCount) {
            spanCount = newSpanCount
            spanCountChangeListener.invoke(spanCount)
        }

        lastUpdateTime = currentTime
    }
}
