package dev.gmarques.controledenotificacoes.data.local.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Criado por Gilian Marques
 * Em sábado, 26 de julho de 2025 às 18:16.
 */
@Suppress("ClassName")
object MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `snoozed_notifications` (
                `packageName` TEXT NOT NULL,
                `key` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `postTime` INTEGER NOT NULL,
                `permaHidden` INTEGER NOT NULL,
                PRIMARY KEY(`key`)
            )
            """.trimIndent()
        )
    }
}
