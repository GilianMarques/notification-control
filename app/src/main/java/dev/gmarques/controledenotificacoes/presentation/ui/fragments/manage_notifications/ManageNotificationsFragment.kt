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
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl.showManageNotificationsFragmentUiHints
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
class ManageNotificationsFragment : MyFragment(), ManagedNotificationsAdapter.Callback {


    private lateinit var binding: FragmentManageNotificationsBinding
    private val viewModel: ManageNotificationsViewModel by viewModels()
    private lateinit var adapter: ManagedNotificationsAdapter

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

            if (checked) when (mbt.checkedButtonId) {
                R.id.button_snoozed -> {
                    viewModel.loadSnoozedNotifications()
                    showHintView(
                        llHintParent,
                        showManageNotificationsFragmentUiHints,
                        getString(
                            R.string.Notifica_es_adiadas_podem_ser_perdidas_caso_o_sistema_seja_reiniciado_o_app_emissor_seja_for_ado_a_parar_ou_cancele_a_notifica_o_enquanto_ela_estiver_adiada_n_nnesses_casos_o_X_,
                            getString(R.string.app_name)
                        )
                    )
                }

                R.id.button_active -> {
                    llHintParent.removeAllViews()
                    viewModel.loadActiveNotifications()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ManagedNotificationsAdapter(this)
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

    /**Callback do adapter do recyclerview [ManagedNotificationsAdapter.Callback].*/
    override fun onMenuClicked(notification: ManageableNotification, ivMenu: AppCompatImageView) {
        vibrator.interaction()
        showContextPopUpMenu(notification, ivMenu)
    }

    private fun showContextPopUpMenu(not: ManageableNotification, ivMenu: AppCompatImageView) {

        viewModel.createPopUpMenu(not) { action ->
            when (action) {
                is NotificationMenuAction.PostNow -> viewModel.postSnoozedOrHiddenNotification(action.not)
                is NotificationMenuAction.Snooze -> navigateToPickDateAndTime(action.not)
                is NotificationMenuAction.Show -> viewModel.postSnoozedOrHiddenNotification(action.not)
                is NotificationMenuAction.Hide -> viewModel.hideNotification(action.not)
                is NotificationMenuAction.Manage -> navigateToAddManagedApp(action.not)
                is NotificationMenuAction.RemoveFromDB -> viewModel.removeNotificationFromDB(action.not)
                is NotificationMenuAction.Cancel -> viewModel.cancelNotification(action.not)
                is NotificationMenuAction.Copy -> {
                    vibrator.success()
                    viewModel.copyTitleAndContent(action.not)
                }
            }
        }.show(this@ManageNotificationsFragment.requireContext(), ivMenu)

    }

    private fun navigateToPickDateAndTime(not: ManageableNotification) {

        setFragmentResultListener(DateTimePickerFragment.RESULT_KEY) { _, bundle ->
            val selectedTimestamp = bundle.getLong(DateTimePickerFragment.TIMESTAMP_KEY)
            viewModel.snoozeNotification(not, selectedTimestamp)
        }
        findNavController().navigate(ManageNotificationsFragmentDirections.toDateTimePickerFragment(System.currentTimeMillis()))
    }

    private fun navigateToAddManagedApp(not: ManageableNotification) {
        findNavController()
            .navigate(ManageNotificationsFragmentDirections.toAddManagedAppsFragment(not.packageName))
    }

}
