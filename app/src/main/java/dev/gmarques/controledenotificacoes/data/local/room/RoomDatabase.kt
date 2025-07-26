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

package dev.gmarques.controledenotificacoes.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.gmarques.controledenotificacoes.data.local.room.dao.AppNotificationDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.ManagedAppDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.RuleDao
import dev.gmarques.controledenotificacoes.data.local.room.dao.SnoozedNotificationDao
import dev.gmarques.controledenotificacoes.data.local.room.entities.AppNotificationEntity
import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import dev.gmarques.controledenotificacoes.data.local.room.entities.RuleEntity
import dev.gmarques.controledenotificacoes.data.local.room.entities.SnoozedNotificationEntity


/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 * @see dev.gmarques.controledenotificacoes.di.modules.RoomDatabaseModule
 */

@Database(
    entities = [
        RuleEntity::class,
        ManagedAppEntity::class,
        AppNotificationEntity::class,
        SnoozedNotificationEntity::class,
    ],
    version = 2
)
abstract class RoomDatabase : RoomDatabase() {

    abstract fun ruleDao(): RuleDao
    abstract fun managedAppDao(): ManagedAppDao
    abstract fun appNotificationDao(): AppNotificationDao

    abstract fun snoozedNotificationDao(): SnoozedNotificationDao

}

