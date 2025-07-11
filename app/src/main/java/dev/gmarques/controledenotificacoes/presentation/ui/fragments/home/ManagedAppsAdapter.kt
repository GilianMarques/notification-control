package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.ItemManagedAppGridBinding
import dev.gmarques.controledenotificacoes.databinding.ItemManagedAppListBinding
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nameOrDescription
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.removeDrawables
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.setStartDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch


/**
 * Adapter responsável por exibir a lista de aplicativos controlados na RecyclerView.
 * Criado por Gilian Marques
 * Em sábado, 26 de abril de 2025 as 17:48.
 */
class ManagedAppsAdapter(
    private val iconPermissive: Drawable,
    private val iconRestrictive: Drawable,
    private val iconNotificationIndicator: Drawable,
    private val getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
    private val onItemClick: (ManagedAppWithRule) -> Unit,
) : ListAdapter<ManagedAppWithRule, ManagedAppsAdapter.ViewHolder>(DiffCallback()) {


    var spanCount = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = if (spanCount == 1) {
            ItemManagedAppListBinding.inflate(inflater, parent, false)
        } else {
            ItemManagedAppGridBinding.inflate(inflater, parent, false)
        }

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            iconPermissive,
            iconRestrictive,
            iconNotificationIndicator,
            getInstalledAppIconUseCase,
            getItem(position),
            onItemClick
        )
    }

    fun submitList(apps: List<ManagedAppWithRule>, query: String) {
        submitList(apps.filter {
            it.name.contains(query, ignoreCase = true)
        })
    }

    class ViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        private val tvAppName
            get() = when (binding) {
                is ItemManagedAppListBinding -> binding.tvAppName
                is ItemManagedAppGridBinding -> binding.tvAppName
                else -> throw IllegalStateException("Unexpected binding type")
            }

        private val tvRuleName
            get() = when (binding) {
                is ItemManagedAppListBinding -> binding.tvRuleName
                is ItemManagedAppGridBinding -> binding.tvRuleName
                else -> throw IllegalStateException("Unexpected binding type")
            }

        private val tvUninstalled
            get() = when (binding) {
                is ItemManagedAppListBinding -> binding.tvUninstalled
                is ItemManagedAppGridBinding -> binding.tvUninstalled
                else -> throw IllegalStateException("Unexpected binding type")
            }

        private val ivAppIcon
            get() = when (binding) {
                is ItemManagedAppListBinding -> binding.ivAppIcon
                is ItemManagedAppGridBinding -> binding.ivAppIcon
                else -> throw IllegalStateException("Unexpected binding type")
            }

        fun bind(
            iconPermissive: Drawable,
            iconRestrictive: Drawable,
            iconNotificationIndicator: Drawable,
            getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
            app: ManagedAppWithRule,
            onItemClick: (ManagedAppWithRule) -> Unit,
        ) {
            tvAppName.removeDrawables()
            tvUninstalled.isVisible = app.uninstalled
            tvAppName.text = app.name

            if (app.hasPendingNotifications) {
                tvAppName.setStartDrawable(iconNotificationIndicator)
            }

            tvRuleName.text = app.rule.nameOrDescription()
            tvRuleName.setStartDrawable(
                if (app.rule.ruleType == RuleType.PERMISSIVE) iconPermissive else iconRestrictive
            )

            CoroutineScope(Main).launch {
                Glide.with(App.instance)
                    .load(getInstalledAppIconUseCase(app.packageId))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.vec_app)
                    .into(ivAppIcon)
            }

            binding.root.setOnClickListener(AnimatedClickListener {
                onItemClick(app)
            })
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ManagedAppWithRule>() {
        override fun areItemsTheSame(oldItem: ManagedAppWithRule, newItem: ManagedAppWithRule) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: ManagedAppWithRule, newItem: ManagedAppWithRule) =
            oldItem == newItem
    }
}
