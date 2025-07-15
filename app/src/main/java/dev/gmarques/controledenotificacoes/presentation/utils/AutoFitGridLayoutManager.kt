package dev.gmarques.controledenotificacoes.presentation.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max

/**
 * Criado por Gilian Marques
 * Em terça-feira, 08 de julho de 2025 as 14:33.
 */
class AutoFitGridLayoutManager(
    context: Context,
    itemWidthDp: Int,
    private val spanCountChangeListener: (spanCount: Int) -> Unit ={},
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

        if (newSpanCount != spanCount)  {
            spanCount = newSpanCount
            spanCountChangeListener.invoke(spanCount)
        }

    }
}
