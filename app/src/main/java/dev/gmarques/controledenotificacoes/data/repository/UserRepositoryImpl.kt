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
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.data.repository.UserRepository
import dev.gmarques.controledenotificacoes.domain.framework.contracts.StringsProvider
import dev.gmarques.controledenotificacoes.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 11:03.
 */
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stringProvider: StringsProvider,
) : UserRepository {

    @Suppress("unused")
    private val guestUser by lazy {
        User(
            stringProvider.guest(),
            "guest@mail.com",
            "https://picsum.photos/200"
        )
    }

    /**
     * Retorna o usuário logado ou nulo.
     * Quando em depuração pode retornar um usuário padrão para testes
     */
    override fun getUser(): User? {

        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser == null) return null
        if (fUser.isAnonymous) return guestUser

        /*Não vejo como pode ser possível uma pessoa logar com sua conta do Google sem ter um nome e email definidos,
        acredito que nem a foto terá URL nulo, mas por via das dúvidas é melhor mostrar informações nulas na tela do que fechar o app
        na cara do usuário, uma vez que a autenticação serve apenas para personalizar a interface */
        return User(fUser.displayName ?: "null", fUser.email ?: "null", fUser.photoUrl?.toString() ?: "null")
    }

    override suspend fun logOff() {
      try {
              AuthUI.getInstance().signOut(context).await()
              FirebaseAuth.getInstance().signOut()
      } catch (ex: Exception) {
          Log.e("USUK", "UserRepositoryImpl.logOff: $ex")
      }
    }

    override fun deleteAccount() {
        TODO("Not yet implemented")
    }
}

