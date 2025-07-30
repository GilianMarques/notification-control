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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.DeleteSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.GetAllSnoozedNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.PostSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.SnoozeNotificationByUserUseCase
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotificationFactory
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import dev.gmarques.controledenotificacoes.presentation.model.ManageableNotification
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 25/07/2025 as 16:54
 */
@HiltViewModel
class ManageNotificationsViewModel @Inject constructor(
    private val getAllSnoozedNotificationsUseCase: GetAllSnoozedNotificationsUseCase,
    private val snoozeNotificationByUserUseCase: SnoozeNotificationByUserUseCase,
    private val postSnoozedNotificationUseCase: PostSnoozedNotificationUseCase,
    private val deleteSnoozedNotificationUseCase: DeleteSnoozedNotificationUseCase,
    @ApplicationContext private val applicationContext: Context,
) : ViewModel() {

    private val _notificationsFlow: MutableStateFlow<List<ManageableNotification>> = MutableStateFlow(emptyList())
    val notificationsFlow: Flow<List<ManageableNotification>> get() = _notificationsFlow

    private var observerJob: Job = Job()

    fun loadSnoozedNotifications() {

        observerJob.cancel()
        observerJob = viewModelScope.launch {

            NotificationListener.getWhenReady()
                .getSnoozedNotificationsFlow()
                .collect { systemList ->
                    val dbList = getAllSnoozedNotificationsUseCase()
                    val filteredSystemList = applyDefaultFilters(systemList)

                    val dbMap = dbList.associateBy { it.key }
                    val systemMap = filteredSystemList.associateBy { it.key }

                    val allKeys = dbMap.keys union systemMap.keys

                    val allSnoozed = allKeys.map { key ->
                        val db = dbMap[key]
                        val system = systemMap[key]
                        ManageableNotification.from(db, system).copy(
                            permaHidden = db?.permaHidden == true,
                            isSnoozed = db?.permaHidden == false,
                            deadRecord = db != null && system == null,
                        )
                    }
                    _notificationsFlow.tryEmit(allSnoozed)
                }
        }
    }

    fun loadActiveNotifications() {

        observerJob.cancel()
        observerJob = viewModelScope.launch {
            NotificationListener.getWhenReady()
                .getActiveWithOngoingNotificationsFlow()
                .collect { systemList ->
                    val filteredSystemList = applyDefaultFilters(systemList)
                    val combined = filteredSystemList.map {
                        ManageableNotification.from(system = it)
                    }
                    _notificationsFlow.tryEmit(combined)

                }
        }
    }

    private fun applyDefaultFilters(snoozedNotifications: List<ActiveStatusBarNotification>): List<ActiveStatusBarNotification> {
        return snoozedNotifications
            .filterNot { it.content.isEmpty() && it.title.isEmpty() }
            .distinctBy { it.title to it.content }
            .distinctBy { it.key }
    }

    fun hideNotification(notification: ManageableNotification) = viewModelScope.launch {
        snoozeNotificationByUserUseCase(ActiveStatusBarNotificationFactory.create(notification), 0, true)
    }

    fun snoozeNotification(notification: ManageableNotification, until: Long) = viewModelScope.launch {
        snoozeNotificationByUserUseCase(ActiveStatusBarNotificationFactory.create(notification), until, false)
    }

    fun postSnoozedOrHiddenNotification(not: ManageableNotification) = viewModelScope.launch {
        postSnoozedNotificationUseCase(not.key)
    }

    fun removeNotificationFromDB(not: ManageableNotification) = viewModelScope.launch {
        deleteSnoozedNotificationUseCase(not.key)
        loadSnoozedNotifications()
    }

    fun cancelNotification(not: ManageableNotification) = viewModelScope.launch {
        NotificationListener.getWhenReady().cancelNotification(not.key)
    }

    fun copyTitleAndContent(not: ManageableNotification) {

        val clipBoardData = not.title.plus("\n\n").plus(not.content)

        val clipboardManager = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(not.title, clipBoardData)
        clipboardManager.setPrimaryClip(clip)

    }
}
