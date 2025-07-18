@file:Suppress("unused")

package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.data.repository.ActiveNotificationRepository
import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.framework.NotificationRuleProcessor
import dev.gmarques.controledenotificacoes.domain.framework.StringsProvider
import dev.gmarques.controledenotificacoes.domain.framework.VibratorProvider
import dev.gmarques.controledenotificacoes.framework.implementations.ActiveNotificationRepositoryImpl
import dev.gmarques.controledenotificacoes.framework.implementations.AlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.implementations.StringsProviderImpl
import dev.gmarques.controledenotificacoes.framework.implementations.VibratorProviderImpl
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationRuleProcessorImpl

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 02 de abril de 2025 as 22:24.
 *
 * Modulo voltado às dependencias relacionadas a plataforma.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FrameworkModule {

    @Binds
    abstract fun bindVibrator(impl: VibratorProviderImpl): VibratorProvider

    @Binds
    abstract fun bindRuleStringsProvider(impl: StringsProviderImpl): StringsProvider

    @Binds
    abstract fun bindNotificationRuleProcessor(impl: NotificationRuleProcessorImpl): NotificationRuleProcessor

    @Binds
    abstract fun bindScheduleManager(impl: AlarmSchedulerImpl): AlarmScheduler

    @Binds
    abstract fun bindNotificationDataSource(impl: ActiveNotificationRepositoryImpl): ActiveNotificationRepository

}