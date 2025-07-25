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

package dev.gmarques.controledenotificacoes.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 06 de maio de 2025 as 13:28.
 */
class PreferencesRepositoryImpl @Inject constructor(@ApplicationContext context: Context) : PreferencesRepository {

    private val dataStore = context.dataStore

    override suspend fun <T> save(key: String, value: T) {
        dataStore.edit { prefs ->
            when (value) {
                is String -> prefs[stringPreferencesKey(key)] = value
                is Boolean -> prefs[booleanPreferencesKey(key)] = value
                is Int -> prefs[intPreferencesKey(key)] = value
                is Float -> prefs[floatPreferencesKey(key)] = value
                is Long -> prefs[longPreferencesKey(key)] = value
                else -> throw IllegalArgumentException("Tipo não suportado: ${value!!::class.java.simpleName}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> read(key: String, default: T): T {
        val prefs = dataStore.data.first()
        return when (default) {
            is String -> prefs[stringPreferencesKey(key)] as T? ?: default
            is Boolean -> prefs[booleanPreferencesKey(key)] as T? ?: default
            is Int -> prefs[intPreferencesKey(key)] as T? ?: default
            is Float -> prefs[floatPreferencesKey(key)] as T? ?: default
            is Long -> prefs[longPreferencesKey(key)] as T? ?: default
            else -> throw IllegalArgumentException("Tipo não suportado: ${default!!::class.java.simpleName}")
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    override suspend fun deletePreferenceByName(key: String) {
        dataStore.edit { prefs ->
            prefs.asMap().keys
                .find {
                    it.name == key
                }
                ?.let {
                    prefs.remove(it)
                }
        }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "app_settings")
    }
}