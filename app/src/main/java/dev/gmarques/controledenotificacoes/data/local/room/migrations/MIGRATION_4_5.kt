package dev.gmarques.controledenotificacoes.data.local.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Criado por Gilian Marques
 * Em quinta-feira, 17 de julho de 2025 as 16:40.
 * Renomeia a coluna `behaviour` para `action` na tabela `rules`.
 */
@Suppress("ClassName")
object MIGRATION_4_5 : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rules RENAME COLUMN behaviour TO action")
    }
}
