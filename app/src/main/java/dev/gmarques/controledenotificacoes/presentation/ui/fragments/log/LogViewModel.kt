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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.log

import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor() : ViewModel() {

    private val allLogs = mutableListOf<String>()
    private val _logsFlow = MutableStateFlow<List<String>>(emptyList())
    val logsFlow: StateFlow<List<String>> get() = _logsFlow

    fun loadLogs() {
        allLogs.clear()
        allLogs.addAll(getLogs())
        _logsFlow.value = allLogs.toList()
    }

    fun filter(query: String) {
        if (query.isBlank()) {
            _logsFlow.value = allLogs
        } else {
            _logsFlow.value = allLogs.filter { it.contains(query, ignoreCase = true) }
        }
    }

    private fun getLogs(): List<String> = PreferencesImpl.log.value.let { jsonLogs ->

        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(MutableList::class.java, String::class.java)
        val adapter = moshi.adapter<MutableList<String>>(type)

        if (jsonLogs.isEmpty()) return@let emptyList()

        val list: MutableList<String>? = adapter.fromJson(jsonLogs)

        return list ?: emptyList()

    }
}