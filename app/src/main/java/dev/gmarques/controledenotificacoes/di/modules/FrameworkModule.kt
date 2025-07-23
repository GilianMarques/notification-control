@file:Suppress("unused")

package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.framework.contracts.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.framework.contracts.StringsProvider
import dev.gmarques.controledenotificacoes.domain.framework.contracts.VibratorProvider
import dev.gmarques.controledenotificacoes.framework.implementations.AlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.implementations.StringsProviderImpl
import dev.gmarques.controledenotificacoes.framework.implementations.VibratorProviderImpl

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
    abstract fun bindScheduleManager(impl: AlarmSchedulerImpl): AlarmScheduler


}