package dev.gmarques.controledenotificacoes.data.local.room.mapper

import dev.gmarques.controledenotificacoes.data.local.room.entities.AppNotificationEntity
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationValidator

object AppNotificationMapper {

    fun toEntity(model: AppNotification): AppNotificationEntity {

        AppNotificationValidator.validate(model)

        return AppNotificationEntity(
            packageName = model.packageName,
            title = model.title,
            content = model.content,
            timestamp = model.timestamp,
        )
    }

    fun toModel(entity: AppNotificationEntity): AppNotification {
        return AppNotification(
            packageName = entity.packageName,
            title = entity.title,
            content = entity.content,
            timestamp = entity.timestamp,
        )
    }
}