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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app


import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.databinding.ItemAppNotificationBinding
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.bitmapId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.pendingIntentId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.timeFormatted
import dev.gmarques.controledenotificacoes.framework.PendingIntentCache
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import java.io.File

/**
 * Criado por Gilian Marques
 * Em terça-feira, 13 de maio de 2025 as 16:06.
 */
class AppNotificationAdapter(private val appIcon: Drawable, val onNotificationClick: (AppNotification) -> Unit) :
    ListAdapter<AppNotification, AppNotificationAdapter.ViewHolder>(DiffCallback) {

    private val anim = AnimationUtils.loadAnimation(App.instance, android.R.anim.fade_in)

    inner class ViewHolder(private val binding: ItemAppNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AppNotification) = with(binding) {

            tvTitle.text = notification.title
            tvTime.text = notification.timeFormatted()
            ivAppIcon.setImageDrawable(appIcon)

            tvContent.text = notification.content

            val file = File(App.instance.cacheDir, notification.bitmapId())

            // evita poluir o logcat com os avisos de erro ao carregar imagens que nao existem no cache
            if (!file.exists()) ivLargeIcon.isGone = true
            else Glide.with(App.instance)
                .asBitmap()
                .load(file)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        ivLargeIcon.setImageBitmap(resource)
                        ivLargeIcon.isVisible = true
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                })

            tvOpenNotification.setOnClickListener(
                AnimatedClickListener
                {
                    onNotificationClick(notification)
                })

            tvOpenNotification.isVisible = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) false
            else PendingIntentCache(notification.pendingIntentId()) != null

            tvContent.isGone = tvContent.text.isEmpty()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.root.startAnimation(anim)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AppNotification>() {
        override fun areItemsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
            return oldItem.postTime == newItem.postTime && oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
            return oldItem == newItem
        }
    }
}
