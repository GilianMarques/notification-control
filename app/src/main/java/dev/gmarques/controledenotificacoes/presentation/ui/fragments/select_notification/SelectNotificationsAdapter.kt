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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_notification

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.databinding.ItemAppNotificationBinding
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.canUseReadMoreFeature
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.readMoreFeature

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:17.
 */
class SelectNotificationsAdapter(
    private val onItemClick: (ActiveStatusBarNotification) -> Unit,
) : ListAdapter<ActiveStatusBarNotification, SelectNotificationsAdapter.ViewHolder>(DiffCallback()) {

    private val anim = AnimationUtils.loadAnimation(App.instance, android.R.anim.fade_in)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.startAnimation(anim)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ActiveStatusBarNotification) = with(binding) {

            tvTitle.text = notification.title

            with(tvContent) {

                this.isVisible = !notification.content.isEmpty()

                if (this.canUseReadMoreFeature(notification.content)) {
                    ivLargeIcon.isGone = true

                    this.readMoreFeature(notification.content) { fullText ->
                        ivLargeIcon.isVisible = fullText && notification.largeIcon != null
                    }
                } else this.text = notification.content
            }


            ivAppIcon.setImageIcon(notification.smallIcon)

            ivLargeIcon.setImageIcon(notification.largeIcon)
            ivLargeIcon.isVisible = notification.largeIcon != null

            tvOpenNotification.isVisible = false
            tvTime.isVisible = false

            parent.setOnClickListener {
                onItemClick(notification)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ActiveStatusBarNotification>() {
        override fun areItemsTheSame(oldItem: ActiveStatusBarNotification, newItem: ActiveStatusBarNotification): Boolean {
            return oldItem.postTime == newItem.postTime && oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: ActiveStatusBarNotification, newItem: ActiveStatusBarNotification): Boolean {
            return oldItem.title == newItem.title &&
                    oldItem.content == newItem.content &&
                    oldItem.packageName == newItem.packageName &&
                    oldItem.postTime == newItem.postTime
        }
    }
}
