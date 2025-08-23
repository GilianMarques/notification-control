package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import dev.gmarques.controledenotificacoes.databinding.ItemAppNotificationManageableCompactBinding
import dev.gmarques.controledenotificacoes.presentation.model.ManageableNotification
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.canUseReadMoreFeature
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.readMoreFeature


/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:17.
 */
class ActiveNotificationsAdapter(
    private val callback: Callback
) : ListAdapter<ManageableNotification, ActiveNotificationsAdapter.ViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppNotificationManageableCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppNotificationManageableCompactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ManageableNotification) = with(binding) {

            tvTitle.text = notification.title
            ivAppIcon.setImageIcon(notification.smallIcon)
            ivMenu.setOnClickListener(AnimatedClickListener {
                callback.onMenuClicked(notification, ivMenu)
            })

            val lp: ViewGroup.LayoutParams? = root.layoutParams
            if (lp is FlexboxLayoutManager.LayoutParams) {
                val flexboxLp = lp
                flexboxLp.flexGrow = 1.0f
                flexboxLp.alignSelf = AlignItems.FLEX_END
            }

            with(tvContent) {

                this.isVisible = !notification.content.isEmpty()
                isSelected = true
                if (this.canUseReadMoreFeature(notification.content)) {
                    this.readMoreFeature(notification.content)
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