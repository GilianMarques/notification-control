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

package dev.gmarques.controledenotificacoes.presentation.model

import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em terça-feira, 15 de abril de 2025 as 08:50.
 *
 * Representa um aplicativo instalado no dispositivo do usuário.
 * Esse modelo é usado pelo repositório de aplicativos instalados para exibir as informações
 * na interface. Ele nunca é escrito no banco de dados.
 */
data class InstalledApp(
    val name: String,
    val packageName: String,
    val isBeingManaged: Boolean,
    val uninstalled: Boolean = false,
) : Serializable {
    companion object {

        /**
         * Cria uma instancia de [InstalledApp] para um aplicativo que foi desinstalado.
         *
         * @param targetPackage O nome do pacote do aplicativo desinstalado.
         * @return Uma instancia de [InstalledApp] representando o aplicativo desinstalado.
         */
        fun uninstalledApp(targetPackage: String): InstalledApp {
            return InstalledApp(
                name = extractNameFromPkg(targetPackage),
                packageName = targetPackage,
                isBeingManaged = true,
                uninstalled = true,
            )
        }

        /**
         * Extrai um nome de aplicativo amigável do nome do pacote.
         * Por exemplo, "com.example.myapp" se tornaria "Myapp".
         *
         * @param pkg O nome do pacote do aplicativo.
         * @return Um nome de aplicativo amigável ou o nome do pacote original se ocorrer um erro.
         */
        private fun extractNameFromPkg(pkg: String): String {
            return try {
                pkg.split(".").last().lowercase().replaceFirstChar { it.uppercase() }
            } catch (_: Exception) {
                pkg
            }
        }


    }
}