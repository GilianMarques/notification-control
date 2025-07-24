@file:Suppress("unused")

package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor
import dev.gmarques.controledenotificacoes.domain.implementations.IncomingNotificationProcessorImpl
import javax.inject.Singleton


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@Module
@InstallIn(SingletonComponent::class) // mesma instancia disponivel em toda aplicação
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindNotificationProcessor(impl: IncomingNotificationProcessorImpl): IncomingNotificationProcessor
}