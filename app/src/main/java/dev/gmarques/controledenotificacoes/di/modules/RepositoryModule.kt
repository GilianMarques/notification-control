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

@file:Suppress("unused")

package dev.gmarques.controledenotificacoes.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.data.repository.AppNotificationRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.InstalledInstalledAppRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.ManagedAppRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.PreferencesRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.RuleRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.SnoozedNotificationRepositoryImpl
import dev.gmarques.controledenotificacoes.data.repository.UserRepositoryImpl
import dev.gmarques.controledenotificacoes.domain.data.repository.AppNotificationRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.InstalledAppRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.ManagedAppRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.PreferencesRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.RuleRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.SnoozedNotificationRepository
import dev.gmarques.controledenotificacoes.domain.data.repository.UserRepository
import javax.inject.Singleton


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@Module
@InstallIn(SingletonComponent::class) // mesma instancia disponivel em toda aplicação
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRuleRepository(impl: RuleRepositoryImpl): RuleRepository

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: InstalledInstalledAppRepositoryImpl): InstalledAppRepository

    @Binds
    @Singleton
    abstract fun bindManagedAppRepository(impl: ManagedAppRepositoryImpl): ManagedAppRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAppNotificationRepository(impl: AppNotificationRepositoryImpl): AppNotificationRepository

    @Binds
    @Singleton
    abstract fun bindSnoozedNotificationRepository(impl: SnoozedNotificationRepositoryImpl): SnoozedNotificationRepository
}