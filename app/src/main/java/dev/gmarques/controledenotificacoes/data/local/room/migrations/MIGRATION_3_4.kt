package dev.gmarques.controledenotificacoes.data.local.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Criado por Gilian Marques
 * Em 17/07/2025 as 15:16
 * Essa migração adiciona a coluna `behaviour` na tabela `rules`
 * para armazenar o comportamento da regra.
 */

@Suppress("ClassName")
object MIGRATION_3_4 : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS rules_new (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                days TEXT NOT NULL,
                timeRanges TEXT NOT NULL,
                ruleType TEXT NOT NULL,
                behaviour TEXT NOT NULL DEFAULT 'SNOOZE',
                condition TEXT
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO rules_new (id, name, days, timeRanges, ruleType, behaviour, condition)
            SELECT id, name, days, timeRanges, ruleType, 
                   COALESCE(behaviour, 'SNOOZE'), condition
            FROM rules
        """.trimIndent()
        )

        db.execSQL("DROP TABLE rules")
        db.execSQL("ALTER TABLE rules_new RENAME TO rules")
    }
}
