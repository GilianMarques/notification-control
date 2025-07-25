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
