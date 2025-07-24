package dev.gmarques.controledenotificacoes.domain.model

import dev.gmarques.controledenotificacoes.domain.model.AppNotificationValidator.AppNotificationValidatorException.BlankPackageNameException


object AppNotificationValidator {
    fun validate(notification: AppNotification) {
        validatePackageName(notification.packageName).getOrThrow()
    }


    fun validatePackageName(packageName: String): Result<String> {
        return if (packageName.isEmpty()) Result.failure(BlankPackageNameException())
        else Result.success(packageName)
    }

    /**
     * Criado por Gilian Marques
     * Em 20/06/2025 as 17:13
     */
    sealed class AppNotificationValidatorException {

        class BlankPackageNameException() :
            Exception("Em hipótese alguma o packageName de um objeto pode ficar vazio.")
    }
}