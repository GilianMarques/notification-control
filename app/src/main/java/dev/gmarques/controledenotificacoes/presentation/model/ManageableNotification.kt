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

package dev.gmarques.controledenotificacoes.presentation.model

import android.app.Notification
import android.graphics.drawable.Icon
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em terça-feira, 29 de julho de 2025 às 11:41.
 *
 * Representa a combinação de [SnoozedNotification] e [ActiveStatusBarNotification]
 */
data class ManageableNotification(
    val title: String,
    val content: String,
    val packageName: String,
    val smallIcon: Icon?,
    val largeIcon: Icon?,
    val postTime: Long,
    val id: Int?,
    val key: String,
    val isOngoing: Boolean,
    val notification: Notification?,
    val tag: String?,
    val isSnoozed: Boolean,
    val permaHidden: Boolean,
    val origin: SnoozedNotification.Origin?,
    val snoozeUntil: Long,
    val isOnlyInDatabase: Boolean,
    val isOnlyInSystem: Boolean,
    val isInDBAndSystem: Boolean,
    val isSystemSnoozed: Boolean,
) {
    companion object {
        fun from(
            database: SnoozedNotification? = null,
            system: ActiveStatusBarNotification? = null
        ): ManageableNotification {
            return ManageableNotification(
                title = database?.title ?: system?.title.orEmpty(),
                content = database?.content ?: system?.content.orEmpty(),
                packageName = database?.packageName ?: system?.packageName.orEmpty(),
                smallIcon = system?.smallIcon,
                largeIcon = system?.largeIcon,
                postTime = database?.postTime ?: system?.postTime ?: 0L,
                id = system?.id,
                key = database?.key ?: system?.key.orEmpty(),
                isOngoing = system?.isOngoing ?: false,
                notification = system?.notification,
                tag = system?.tag,
                origin = database?.origin,
                isOnlyInDatabase = database != null && system == null,
                isOnlyInSystem = database == null && system != null,
                isInDBAndSystem = database != null && system != null,
                snoozeUntil = database?.snoozeUntil ?: 0,
                permaHidden = database?.permaHidden ?: false,
                isSnoozed = false,
                isSystemSnoozed = false,
            )
        }
    }
}
