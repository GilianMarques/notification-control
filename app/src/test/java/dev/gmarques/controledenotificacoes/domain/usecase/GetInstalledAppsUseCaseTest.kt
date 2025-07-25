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

package dev.gmarques.controledenotificacoes.domain.usecase

import dev.gmarques.controledenotificacoes.domain.data.repository.InstalledAppRepository
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetAllInstalledAppsUseCase
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GetInstalledAppsUseCaseTest {

    private lateinit var useCase: GetAllInstalledAppsUseCase
    private lateinit var repository: InstalledAppRepository

    @BeforeEach
    fun configurar() {
        repository = mock()
        useCase = GetAllInstalledAppsUseCase(repository)
    }

    @Test
    fun `dado um nome alvo e pacotes excluidos, quando executar e chamado, entao o repositorio getInstalledApps deve ser invocado`() =
        runTest {
            val excludedPackages = hashSetOf("com.facebook.katana")
            val targetName = "chat"

            val expectedApps = listOf(
                mockApp("WhatsApp", "com.whatsapp"),
                mockApp("Telegram", "org.telegram.messenger")
            )

            whenever(repository.getInstalledApps(targetName, true, true, excludedPackages)).thenReturn(expectedApps)

            val result = useCase(targetName, excludedPackages, true, true)

            assertEquals(expectedApps, result)
            verify(repository).getInstalledApps(targetName, true, true, excludedPackages)
    }

    @Test
    fun `dado um nome vazio e sem pacotes excluidos, quando executar e chamado, entao o repositorio getInstalledApps deve retornar todos os apps nao excluidos`() =
        runTest {
            val excludedPackages = hashSetOf<String>()
            val targetName = ""

            val expectedApps = listOf(
                mockApp("Instagram", "com.instagram.android"),
                mockApp("Twitter", "com.twitter.android")
            )

            whenever(repository.getInstalledApps(targetName, true, true, excludedPackages)).thenReturn(expectedApps)

            val result = useCase(targetName, excludedPackages, true, true)

            assertEquals(expectedApps, result)
            verify(repository).getInstalledApps(targetName, true, true, excludedPackages)
    }

    private fun mockApp(name: String, packageName: String): InstalledApp {
        return InstalledApp(name, packageName, false)
    }
}
