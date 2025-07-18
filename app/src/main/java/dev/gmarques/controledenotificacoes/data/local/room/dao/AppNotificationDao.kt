package dev.gmarques.controledenotificacoes.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.gmarques.controledenotificacoes.data.local.room.entities.AppNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotificationEntity)

    @Query("DELETE FROM app_notifications WHERE packageName = :packageName")
    suspend fun deleteAll(packageName: String)

    @Query("SELECT * FROM app_notifications WHERE packageName = :pkg")
    suspend fun getByPkg(pkg: String): AppNotificationEntity?

    @Query("SELECT * FROM app_notifications")
    suspend fun getAll(): List<AppNotificationEntity>

    @Query("SELECT * FROM app_notifications WHERE packageName = :pkg")
    fun observeNotificationsByPkgId(pkg: String): Flow<List<AppNotificationEntity>>
}