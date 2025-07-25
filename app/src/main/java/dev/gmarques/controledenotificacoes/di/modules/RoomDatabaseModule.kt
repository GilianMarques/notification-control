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

package dev.gmarques.controledenotificacoes.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.data.local.room.dao.AppNotificationDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.migrations.MIGRATION_1_2
import dev.gmarques.controledenotificacoes.data.local.room.migrations.MIGRATION_2_3
import dev.gmarques.controledenotificacoes.data.local.room.migrations.MIGRATION_3_4
import dev.gmarques.controledenotificacoes.data.local.room.migrations.MIGRATION_4_5
import javax.inject.Singleton

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 */
@Module
@InstallIn(SingletonComponent::class)
object RoomDatabaseModule {


    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): RoomDatabase {

        return Room.databaseBuilder(context, RoomDatabase::class.java, "room_database")
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                /**Nao esquece de aumentar a versão do DB em [RoomDatabase]*/
            )

            .fallbackToDestructiveMigrationOnDowngrade(BuildConfig.DEBUG)
            .build()
    }

    @Provides
    fun provideRuleDao(roomDatabase: RoomDatabase): RuleDao {
        return roomDatabase.ruleDao()
    }

    @Provides
    fun provideManagedAppDao(roomDatabase: RoomDatabase): ManagedAppDao {
        return roomDatabase.managedAppDao()
    }

    @Provides
    fun provideAppNotificationDao(roomDatabase: RoomDatabase): AppNotificationDao {
        return roomDatabase.appNotificationDao()
    }
}


