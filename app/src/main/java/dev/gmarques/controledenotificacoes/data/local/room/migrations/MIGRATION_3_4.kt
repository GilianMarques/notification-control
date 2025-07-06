package dev.gmarques.controledenotificacoes.data.local.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
Criado por Gilian Marques
 * Em 29/06/2025 as 10:54
 *
 * Essa migração adiciona uma coluna 'id' à tabela 'app_notifications'.
 *  Esta coluna é usada como chave primária para identificar exclusivamente cada notificação.
 */

@Suppress("ClassName")
object MIGRATION_3_4 : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Cria nova tabela com id como PRIMARY KEY
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS app_notifications_new (
                packageId TEXT NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                id INTEGER NOT NULL PRIMARY KEY
            )
        """.trimIndent())

        // Copia os dados da antiga para a nova, usando timestamp como id
        database.execSQL("""
            INSERT INTO app_notifications_new (packageId, title, content, timestamp, id)
            SELECT packageId, title, content, timestamp, timestamp
            FROM app_notifications
        """.trimIndent())

        // Remove tabela antiga
        database.execSQL("DROP TABLE app_notifications")

        // Renomeia tabela nova
        database.execSQL("ALTER TABLE app_notifications_new RENAME TO app_notifications")
    }
}
