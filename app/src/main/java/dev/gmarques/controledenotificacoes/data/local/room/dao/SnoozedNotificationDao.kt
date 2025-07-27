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

package dev.gmarques.controledenotificacoes.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.gmarques.controledenotificacoes.data.local.room.entities.SnoozedNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SnoozedNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: SnoozedNotificationEntity)

    @Query("DELETE FROM snoozed_notifications WHERE packageName = :packageName")
    suspend fun deleteAll(packageName: String)

    @Query("DELETE FROM snoozed_notifications WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("SELECT * FROM snoozed_notifications WHERE `key` = :key")
    suspend fun getByKey(key: String): SnoozedNotificationEntity?

    @Query("SELECT * FROM snoozed_notifications")
    suspend fun getAll(): List<SnoozedNotificationEntity>

    @Query("SELECT * FROM snoozed_notifications WHERE packageName = :pkg")
    fun observeNotificationsByPkgId(pkg: String): Flow<List<SnoozedNotificationEntity>>
}