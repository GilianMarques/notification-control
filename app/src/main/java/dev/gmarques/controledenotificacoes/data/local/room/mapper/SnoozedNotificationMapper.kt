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

package dev.gmarques.controledenotificacoes.data.local.room.mapper

import dev.gmarques.controledenotificacoes.data.local.room.entities.SnoozedNotificationEntity
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotification
import dev.gmarques.controledenotificacoes.domain.model.SnoozedNotificationValidator


object SnoozedNotificationMapper {

    fun toEntity(model: SnoozedNotification): SnoozedNotificationEntity {

        SnoozedNotificationValidator.validate(model)

        return SnoozedNotificationEntity(
            packageName = model.packageName,
            title = model.title,
            content = model.content,
            postTime = model.postTime,
            key = model.key,
            permaHidden = model.permaHidden,
            origin = model.origin,
            snoozeUntil = model.snoozeUntil
        )
    }

    fun toModel(entity: SnoozedNotificationEntity): SnoozedNotification {
        return SnoozedNotification(
            packageName = entity.packageName,
            title = entity.title,
            content = entity.content,
            postTime = entity.postTime,
            key = entity.key,
            permaHidden = entity.permaHidden,
            origin = entity.origin,
            snoozeUntil = entity.snoozeUntil
        )
    }
}