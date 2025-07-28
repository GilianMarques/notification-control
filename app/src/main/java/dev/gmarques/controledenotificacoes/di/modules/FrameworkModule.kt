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
import dev.gmarques.controledenotificacoes.domain.framework.contracts.StringsProvider
import dev.gmarques.controledenotificacoes.domain.framework.contracts.VibratorProvider
import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.AutoTurnOnAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.BackupNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.ReportNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.framework.implementations.AutoTurnOnAlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.implementations.BackupNotificationAlarmSchedulerImpl
import dev.gmarques.controledenotificacoes.framework.implementations.ReportNotificationAlarmSchedulerImpl
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
    abstract fun bindVibratorProvider(impl: VibratorProviderImpl): VibratorProvider

    @Binds
    abstract fun bindStringsProvider(impl: StringsProviderImpl): StringsProvider

    @Binds
    abstract fun bindReportNotificationAlarmScheduler(impl: ReportNotificationAlarmSchedulerImpl): ReportNotificationAlarmScheduler

    @Binds
    abstract fun bindBackupNotificationAlarmScheduler(impl: BackupNotificationAlarmSchedulerImpl): BackupNotificationAlarmScheduler

    @Binds
    abstract fun bindAutoTurnOnAlarmScheduler(impl: AutoTurnOnAlarmSchedulerImpl): AutoTurnOnAlarmScheduler


}