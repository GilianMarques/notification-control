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
import com.github.zawadz88.materialpopupmenu.popupMenu
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.DeleteSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.ObserveAllSnoozedNotificationsUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.PostSnoozedNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.snoozed_notification.SnoozeNotificationByUserUseCase
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotificationFactory
import dev.gmarques.controledenotificacoes.presentation.model.ManageableNotification
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 25/07/2025 as 16:54
 */
@HiltViewModel
class ManageNotificationsViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val observeAllSnoozedNotificationsUseCase: ObserveAllSnoozedNotificationsUseCase,
    private val snoozeNotificationByUserUseCase: SnoozeNotificationByUserUseCase,
    private val postSnoozedNotificationUseCase: PostSnoozedNotificationUseCase,
    private val deleteSnoozedNotificationUseCase: DeleteSnoozedNotificationUseCase,
    private val systemNotificationManager: SystemNotificationManager,

    ) : ViewModel() {

    private val _notificationsFlow: MutableStateFlow<List<ManageableNotification>> = MutableStateFlow(emptyList())
    val notificationsFlow: Flow<List<ManageableNotification>> get() = _notificationsFlow

    private var observerJob: Job = Job()

    fun loadSnoozedNotifications() {

        observerJob.cancel()
        observerJob = viewModelScope.launch {

            if (!systemNotificationManager.canOperate()) return@launch

            val snoozedNotificationsOnStatusBar = systemNotificationManager.getSnoozedNotificationsFlow()
            val snoozedNotificationsOnDatabase = observeAllSnoozedNotificationsUseCase()

            combine(snoozedNotificationsOnDatabase, snoozedNotificationsOnStatusBar) { dbList, systemList ->

                val dbMap = dbList.associateBy { it.key }
                val systemMap = systemList.associateBy { it.key }

                val allKeys = dbMap.keys union systemMap.keys

                allKeys.map { key ->
                    val db = dbMap[key]
                    val system = systemMap[key]
                    ManageableNotification.from(db, system).copy(
                        permaHidden = db?.permaHidden ?: false,
                        isSnoozed = db?.permaHidden?.not() ?: false,
                        isSystemSnoozed = true,
                    )
                }

            }.collect { allSnoozed ->
                _notificationsFlow.emit(allSnoozed)
            }
        }
    }

    fun loadActiveNotifications() {

        observerJob.cancel()
        observerJob = viewModelScope.launch {

            if (!systemNotificationManager.canOperate()) return@launch

            systemNotificationManager.getActiveWithOngoingNotificationsFlow().collect { systemList ->

                val managed = systemList.map {
                    ManageableNotification.from(system = it)
                }
                _notificationsFlow.tryEmit(managed)

            }
        }
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
        systemNotificationManager.cancelNotification(not.key)
    }

    fun copyTitleAndContent(not: ManageableNotification) {

        val clipBoardData = not.title.plus("\n\n").plus(not.content)

        val clipboardManager = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(not.title, clipBoardData)
        clipboardManager.setPrimaryClip(clip)

    }

    fun createPopUpMenu(
        not: ManageableNotification,
        actionCallback: (NotificationMenuAction) -> Unit
    ) = with(App.instance) {
        popupMenu {

            section {
                if (!not.permaHidden && (not.isSnoozed || not.isSystemSnoozed)) item {
                    label = getString(R.string.Tentar_postar_agora)
                    icon = R.drawable.vec_post_now
                    callback = { actionCallback(NotificationMenuAction.PostNow(not)) }
                }

                if (!not.permaHidden && !not.isSnoozed && !not.isSystemSnoozed) item {
                    label = getString(R.string.Adiar)
                    icon = R.drawable.vec_snooze
                    callback = { actionCallback(NotificationMenuAction.Snooze(not)) }
                }

                if (not.permaHidden && not.isSystemSnoozed) item {
                    label = getString(R.string.Exibir)
                    icon = R.drawable.vec_show
                    callback = { actionCallback(NotificationMenuAction.Show(not)) }
                }

                if (!not.permaHidden && !not.isSnoozed && !not.isSystemSnoozed) item {
                    label = getString(R.string.Ocultar)
                    icon = R.drawable.vec_hide
                    callback = { actionCallback(NotificationMenuAction.Hide(not)) }
                }
            }

            section {
                item {
                    label = getString(R.string.Gerenciar)
                    icon = R.drawable.vec_manage_notification
                    callback = { actionCallback(NotificationMenuAction.Manage(not)) }
                }
            }

            if (not.isOnlyInDatabase) {
                section {
                    item {
                        label = getString(R.string.Remover_registro)
                        icon = R.drawable.vec_remove
                        callback = { actionCallback(NotificationMenuAction.RemoveFromDB(not)) }
                    }
                }
            }

            section {
                if (!not.isOngoing && !not.isSnoozed && !not.permaHidden && (not.isOnlyInSystem || not.isInDBAndSystem) && !not.isSystemSnoozed) item {
                    label = getString(R.string.Dispensar)
                    icon = R.drawable.vec_dismiss
                    callback = { actionCallback(NotificationMenuAction.Cancel(not)) }
                }

                item {
                    label = getString(R.string.Copiar)
                    icon = R.drawable.vec_copy
                    callback = {
                        actionCallback(NotificationMenuAction.Copy(not))
                    }
                }
            }
        }
    }


}

sealed class NotificationMenuAction(val not: ManageableNotification) {
    class PostNow(not: ManageableNotification) : NotificationMenuAction(not)
    class Snooze(not: ManageableNotification) : NotificationMenuAction(not)
    class Show(not: ManageableNotification) : NotificationMenuAction(not)
    class Hide(not: ManageableNotification) : NotificationMenuAction(not)
    class Manage(not: ManageableNotification) : NotificationMenuAction(not)
    class RemoveFromDB(not: ManageableNotification) : NotificationMenuAction(not)
    class Cancel(not: ManageableNotification) : NotificationMenuAction(not)
    class Copy(not: ManageableNotification) : NotificationMenuAction(not)
}

