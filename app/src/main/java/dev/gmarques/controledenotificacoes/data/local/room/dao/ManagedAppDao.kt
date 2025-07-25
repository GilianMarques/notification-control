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
import androidx.room.Update
import dev.gmarques.controledenotificacoes.data.local.room.entities.ManagedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
/**
 * Criado por Gilian Marques
 * Em sábado, 13 de abril de 2025 às 16:45.
 */
interface ManagedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateManagedApp(managedAppEntity: ManagedAppEntity)

    @Update
    suspend fun updateManagedApp(managedAppEntity: ManagedAppEntity)

    @Query("DELETE FROM managed_apps WHERE packageName = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM managed_apps WHERE packageName = :id")
    suspend fun getManagedAppByPackageId(id: String): ManagedAppEntity?

    @Query("SELECT * FROM managed_apps")
    fun observeAllManagedApps(): Flow<List<ManagedAppEntity>>

    @Query("DELETE FROM managed_apps WHERE ruleId = :ruleId")
    fun deleteManagedAppsByRuleId(ruleId: String): Int

    @Query("SELECT * FROM managed_apps WHERE ruleId = :ruleId")
    suspend fun getManagedAppsByRuleId(ruleId: String): List<ManagedAppEntity?>


    @Query("SELECT * FROM managed_apps WHERE packageName = :pkg")
    fun observeManagedApp(pkg: String): Flow<ManagedAppEntity?>

}
