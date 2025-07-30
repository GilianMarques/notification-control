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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.manage_notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ItemAppNotificationManageableBinding
import dev.gmarques.controledenotificacoes.framework.FormatDateAndTimeString
import dev.gmarques.controledenotificacoes.presentation.model.ManageableNotification
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.canUseReadMoreFeature
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.readMoreFeature
import org.joda.time.LocalDateTime

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:17.
 */
class ManageNotificationsAdapter(
    private val callback: Callback
) : ListAdapter<ManageableNotification, ManageNotificationsAdapter.ViewHolder>(DiffCallback()) {

    private val anim = AnimationUtils.loadAnimation(App.instance, android.R.anim.fade_in)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppNotificationManageableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.startAnimation(anim)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppNotificationManageableBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ManageableNotification) = with(binding) {

            tvTitle.text = notification.title
            ivAppIcon.setImageIcon(notification.smallIcon)
            ivMenu.setOnClickListener(AnimatedClickListener {
                callback.onMenuClicked(notification, ivMenu)
            })

            ivOngoing.isVisible = notification.isOngoing
            ivDismissible.isVisible = ivOngoing.isVisible.not()

            with(ivLargeIcon) {
                this.setImageIcon(notification.largeIcon)
                this.isVisible = notification.largeIcon != null
            }

            with(tvStatus) {
                this.text = if (notification.permaHidden) App.instance.getString(R.string.Oculta_por_tempo_indeterminado)
                else if (notification.snoozeUntil > System.currentTimeMillis()) {
                    val date = FormatDateAndTimeString.format(LocalDateTime(notification.snoozeUntil))
                    App.instance.getString(R.string.Adiada_ate_x, date.lowercase())
                } else ""

                this.isVisible = tvStatus.text.toString().isNotEmpty()
            }

            with(tvContent) {

                this.isVisible = !notification.content.isEmpty()

                if (this.canUseReadMoreFeature(notification.content)) {
                    ivLargeIcon.isGone = true

                    this.readMoreFeature(notification.content) { fullText ->
                        ivLargeIcon.isVisible = fullText && notification.largeIcon != null
                    }
                } else this.text = notification.content
            }

        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ManageableNotification>() {
        override fun areItemsTheSame(oldItem: ManageableNotification, newItem: ManageableNotification): Boolean {
            return oldItem.postTime == newItem.postTime && oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: ManageableNotification, newItem: ManageableNotification): Boolean {
            return oldItem.title == newItem.title && oldItem.content == newItem.content && oldItem.packageName == newItem.packageName && oldItem.postTime == newItem.postTime
        }
    }

    interface Callback {
        fun onMenuClicked(notification: ManageableNotification, ivMenu: AppCompatImageView)
    }

}
