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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentManageNotificationsBinding
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneControllerCallback
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
        setupRecyclerView()
        observeViewModel()
        viewModel.loadNotifications()
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
    override fun onManageClicked(notification: ActiveStatusBarNotification) {
        TODO("Not yet implemented")
    }

    /**Callback do adapter do recyclerview [ManageNotificationsAdapter.Callback].*/
    override fun onHideClicked(notification: ActiveStatusBarNotification) {
        TODO("Not yet implemented")
    }

    /**Callback do adapter do recyclerview [ManageNotificationsAdapter.Callback].*/
    override fun onSnoozeClicked(notification: ActiveStatusBarNotification) {
       viewModel.snoozeNotification(notification)
    }
}
