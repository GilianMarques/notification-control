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

package dev.gmarques.controledenotificacoes.domain.usecase.installed_apps

import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 12/06/2025 as 17:37
 *
 * Use case responsável por obter a lista de aplicativos de smartwatch instalados no dispositivo.
 *
 * Este use case utiliza uma lista predefinida de nomes de pacotes de aplicativos de smartwatch
 * e verifica quais deles estão efetivamente instalados no dispositivo do usuário.
 *
 * @property getInstalledAppByPackageOrDefaultUseCase Use case para obter um aplicativo instalado pelo nome do pacote ou um valor padrão.
 */
class GetSmartWatchInstalledAppsUseCase @Inject constructor(
    private val getInstalledAppByPackageOrDefaultUseCase: GetInstalledAppByPackageOrDefaultUseCase,
) {

    private val smartwatchAppPackages = listOf(
        "com.samsung.android.app.watchmanager",
        "com.yingsheng.hayloufun",
        "com.crrepa.band.dafit",
        "com.watch.life",
        "com.veryfit.multi",
        "com.xiaomi.wearable",
        "com.zepp.mgrowth"
    )

    /**
     * Executa o use case.
     *
     * @return Uma lista de objetos [InstalledApp] representando os aplicativos de smartwatch
     *         instalados no dispositivo. Retorna uma lista vazia se nenhum dos aplicativos da lista predefinida estiver instalado.
     */
    suspend operator fun invoke(): List<InstalledApp> {

        val apps = mutableListOf<InstalledApp>()

        smartwatchAppPackages.forEach {
            val app = getInstalledAppByPackageOrDefaultUseCase(it)
            if (!app.uninstalled) apps.add(app)
        }

        return apps
    }
}