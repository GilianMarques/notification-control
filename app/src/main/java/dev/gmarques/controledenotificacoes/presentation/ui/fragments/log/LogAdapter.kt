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

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.AppLogger.AppLog
import dev.gmarques.controledenotificacoes.databinding.ItemLogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogAdapter : ListAdapter<AppLog, LogAdapter.LogViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<AppLog>() {
        override fun areItemsTheSame(oldItem: AppLog, newItem: AppLog): Boolean {
            return oldItem.timeStamp == newItem.timeStamp && oldItem.msg == newItem.msg
        }

        override fun areContentsTheSame(oldItem: AppLog, newItem: AppLog): Boolean {
            return oldItem == newItem
        }
    }

    inner class LogViewHolder(private val binding: ItemLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(appLog: AppLog) {
            // Formatar timestamp
            val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss:SSS", Locale.getDefault())
            binding.tvTimestamp.text =
                dateFormat.format(Date(appLog.timeStamp))

            // Exibir caller
            binding.tvCaller.text = appLog.caller

            // Exibir mensagem
            binding.tvLogMessage.text = appLog.msg + if (appLog.relevantObjects.isNotEmpty()) " (...)" else ""

            // Preparar objetos relevantes usando formatação completa
            val relevantObjectsText = JsonFormatter.formatRelevantObjects(appLog.relevantObjects)
            binding.tvRelevantObjects.text = LogFormatter.formatLog(relevantObjectsText)

            // Configurar visibilidade inicial
            binding.tvRelevantObjects.visibility = View.GONE

            // Click listener para expandir/retrair
            binding.root.setOnClickListener {
                binding.tvRelevantObjects.isVisible =
                    (binding.tvRelevantObjects.text.isNotEmpty() && !binding.tvRelevantObjects.isVisible)
            }

            // Long click para copiar log completo
            binding.root.setOnLongClickListener {
                copyLogToClipboard(appLog)
                true
            }

        }

        private fun copyLogToClipboard(appLog: AppLog) {
            val clipboard = App.instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss:SSS", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(appLog.timeStamp))

            val logText = buildString {
                append("Timestamp: $formattedDate\n")
                append("Caller: ${appLog.caller}\n")
                append("Message: ${appLog.msg}\n\n")
                if (appLog.relevantObjects.isNotEmpty()) {
                    append("Relevant Objects:\n")
                    append(JsonFormatter.formatRelevantObjects(appLog.relevantObjects))
                }
            }

            val clip = ClipData.newPlainText("log completo", logText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(App.instance, "Log completo copiado!", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}