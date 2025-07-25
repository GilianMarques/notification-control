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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.select_apps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dev.gmarques.controledenotificacoes.databinding.ItemAppSelectableBinding
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.presentation.model.SelectableApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 09:19.
 */
class AppsAdapter(
    private val getInstalledAppIconUseCase: GetInstalledAppIconUseCase,
    private val onItemCheck: (SelectableApp, Boolean) -> Unit,
) : ListAdapter<SelectableApp, AppsAdapter.AppViewHolder>(DiffCallback()) {

    private var blockSelection = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppSelectableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setBlockSelection(block: Boolean) {
        this.blockSelection = block
    }

    fun submitList(apps: List<SelectableApp>, query: String) {
        submitList(apps.filter {
            it.installedApp.name.contains(query, ignoreCase = true)
        })

    }


    inner class AppViewHolder(private val binding: ItemAppSelectableBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(selectedApp: SelectableApp) = with(binding) {

            cbSelect.setOnCheckedChangeListener(null)
            parent.setOnClickListener(null)
            tvAppManaged.isVisible = false

            CoroutineScope(Main).launch {

                tvAppName.text = selectedApp.installedApp.name

                Glide.with(binding.ivAppIcon.context)
                    .load(getInstalledAppIconUseCase(selectedApp.installedApp.packageName))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivAppIcon)
            }

            if (selectedApp.installedApp.isBeingManaged) {
                tvAppManaged.isVisible = true
            }

            cbSelect.isChecked = selectedApp.isSelected

            parent.setOnClickListener {
                cbSelect.isChecked = !cbSelect.isChecked
            }

            cbSelect.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked && blockSelection) cbSelect.isChecked = false

                onItemCheck(selectedApp, isChecked)
            }


        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SelectableApp>() {

        override fun areItemsTheSame(oldItem: SelectableApp, newItem: SelectableApp): Boolean {
            return oldItem.installedApp.packageName == newItem.installedApp.packageName
                    && oldItem.isSelected == newItem.isSelected

        }

        override fun areContentsTheSame(oldItem: SelectableApp, newItem: SelectableApp): Boolean {
            return oldItem == newItem
        }
    }
}