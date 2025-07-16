@file:Suppress("unused")

package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.data.repository.ActiveNotificationRepository
import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.framework.StringsProvider
import dev.gmarques.controledenotificacoes.domain.framework.VibratorProvider
import dev.gmarques.controledenotificacoes.framework.ActiveNotificationRepositoryImpl
import dev.gmarques.controledenotificacoes.framework.AlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.StringsProviderImpl
import dev.gmarques.controledenotificacoes.framework.VibratorProviderImpl
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.RuleEnforcerImpl

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
    abstract fun bindRuleEnforcer(impl: RuleEnforcerImpl): RuleEnforcer

    @Binds
    abstract fun bindScheduleManager(impl: AlarmSchedulerImpl): AlarmScheduler

    @Binds
    abstract fun bindNotificationDataSource(impl: ActiveNotificationRepositoryImpl): ActiveNotificationRepository

}