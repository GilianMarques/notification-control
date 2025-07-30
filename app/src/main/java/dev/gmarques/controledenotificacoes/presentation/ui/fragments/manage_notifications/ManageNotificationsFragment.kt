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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.github.zawadz88.materialpopupmenu.popupMenu
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentManageNotificationsBinding
import dev.gmarques.controledenotificacoes.presentation.model.ManageableNotification
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneControllerCallback
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.date_picker.DateTimePickerFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AutoFitGridLayoutManager
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.rebindAdapter

/**
 * Criado por Gilian Marques
 * Em 25/07/2025 as 16:54
 */
@AndroidEntryPoint
class ManageNotificationsFragment : MyFragment(), ManageNotificationsAdapter.Callback {


    private lateinit var binding: FragmentManageNotificationsBinding
    private val viewModel: ManageNotificationsViewModel by viewModels()
    private lateinit var adapter: ManageNotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return FragmentManageNotificationsBinding.inflate(inflater, container, false).also {
            binding = it
            setupActionBar(binding.actionbar)
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToggleButtons()
        setupRecyclerView()
        observeViewModel()
        viewModel.loadActiveNotifications()
    }

    private fun setupToggleButtons() = with(binding) {
        buttonGroup.addOnButtonCheckedListener { mbt, id, checked ->

            when (mbt.checkedButtonId) {
                R.id.button_snoozed -> viewModel.loadSnoozedNotifications()
                R.id.button_active -> viewModel.loadActiveNotifications()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ManageNotificationsAdapter(this)

        requireMainActivity().slidingPaneController
            ?.addStateListener(this@ManageNotificationsFragment, object : SlidingPaneControllerCallback {
                override fun onAnimationStarted(currentState: SlidingPaneController.SlidingPaneState) {
                    binding.rvNotifications.adapter = null
                }

                override fun onAnimationEnd(newState: SlidingPaneController.SlidingPaneState) {
                    binding.rvNotifications.adapter = adapter
                }
            })

        binding.rvNotifications.apply {
            layoutManager = AutoFitGridLayoutManager(requireContext(), 300) {
                binding.rvNotifications.rebindAdapter()
            }
            adapter = this@ManageNotificationsFragment.adapter
        }

    }

    private fun observeViewModel() {
        collectFlow(viewModel.notificationsFlow) { list ->
            binding.progressBar.isVisible = false
            adapter.submitList(list)
            binding.emptyView.isVisible = list.isEmpty()
        }
    }

    /**Callback do adapter do recyclerview [ManageNotificationsAdapter.Callback].*/
    override fun onMenuClicked(notification: ManageableNotification, ivMenu: AppCompatImageView) {
        vibrator.interaction()
        showContextPopUpMenu(notification, ivMenu)
    }

    private fun showContextPopUpMenu(not: ManageableNotification, ivMenu: AppCompatImageView) {

        popupMenu {

            section {

                if (!not.permaHidden && not.isFromSystem) item {
                    label = getString(R.string.Tentar_postar_agora)
                    icon = R.drawable.vec_post_now
                    callback = {
                        viewModel.postSnoozedOrHiddenNotification(not)
                    }
                }

                if (!not.isSnoozed)
                    if (not.permaHidden) item {
                        label = getString(R.string.Exibir)
                        icon = R.drawable.vec_show
                        callback = {
                            viewModel.postSnoozedOrHiddenNotification(not)
                        }
                    }
                    else item {
                        label = getString(R.string.Ocultar)
                        icon = R.drawable.vec_hide
                        callback = {
                            viewModel.hideNotification(not)
                        }
                    }

                if (!not.permaHidden) if (not.isSnoozed) item {
                    label = getString(R.string.Postar_agora)
                    icon = R.drawable.vec_post_now
                    callback = {
                        viewModel.postSnoozedOrHiddenNotification(not)
                    }
                } else
                    item {
                        label = getString(R.string.Adiar)
                        icon = R.drawable.vec_snooze
                        callback = {
                            navigateToPickDateAndTime(not)
                        }
                    }
            }

            section {

                item {
                    label = getString(R.string.Gerenciar)
                    icon = R.drawable.vec_manage_notification
                    callback = {
                        navigateToAddManagedApp(not)
                    }
                }
            }

            if (not.deadRecord) section {

                item {
                    label = getString(R.string.Remover_registro)
                    icon = R.drawable.vec_remove
                    callback = {
                        viewModel.removeNotificationFromDB(not)
                    }
                }
            }

            section {

                if (!not.isOngoing
                    && !not.isSnoozed
                    && !not.permaHidden
                    && !not.deadRecord
                    && not.isFromSystem
                ) item {
                    label = getString(R.string.Dispensar)
                    icon = R.drawable.vec_dismiss
                    callback = {
                        viewModel.cancelNotification(not)
                    }
                }

                item {
                    label = getString(R.string.Copiar)
                    icon = R.drawable.vec_copy
                    callback = {
                        viewModel.copyTitleAndContent(not)
                        vibrator.success()
                    }
                }
            }


        }.show(this@ManageNotificationsFragment.requireContext(), ivMenu)

    }

    private fun navigateToPickDateAndTime(not: ManageableNotification) {

        setFragmentResultListener(DateTimePickerFragment.RESULT_KEY) { _, bundle ->
            val selectedTimestamp = bundle.getLong(DateTimePickerFragment.TIMESTAMP_KEY)
            viewModel.snoozeNotification(not, selectedTimestamp)
        }
        findNavControllerMain().navigate(ManageNotificationsFragmentDirections.toDateTimePickerFragment(System.currentTimeMillis()))
    }

    private fun navigateToAddManagedApp(not: ManageableNotification) {
        findNavControllerMain()
            .navigate(ManageNotificationsFragmentDirections.toAddManagedAppsFragment(not.packageName))
    }

}
