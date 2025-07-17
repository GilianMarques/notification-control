package dev.gmarques.controledenotificacoes.presentation.ui.fragments.login

import android.content.Context
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.IdpResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.domain.model.User
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.text.MessageFormat
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar o estado do processo de login
 * e fornecer informações relevantes para a interface de usuário.
 *
 * Criado por Gilian Marques
 * Em domingo, 27 de abril de 2025 às 19:32.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getUserUseCase: GetUserUseCase,
) : ViewModel() {

    private val _statesFlow = MutableStateFlow<State>(State.Idle)
    val statesFlow: StateFlow<State> get() = _statesFlow

    private val _eventsChannel = Channel<LoginEvent>(Channel.BUFFERED)
    val eventsFlow: Flow<LoginEvent> get() = _eventsChannel.receiveAsFlow()


    fun getGuestUser(): User {
        return getUserUseCase() ?: error("deveria retornar o usuario convidado aqui, e nao nulo.")
    }

    fun notifyLoginSuccess() {
        _eventsChannel.trySend(LoginEvent.Success(getGuestUser()))
    }

    fun startLoginFlow(asGuest: Boolean) {
        _statesFlow.tryEmit(State.LoginIn)
        _eventsChannel.trySend(LoginEvent.StartFlow(asGuest))
    }

    /**
     * Notifica a interface de usuário que o processo de login falhou.
     * O evento dispara a vibração de erro uma unica vez, e o estado seta a mensagem de erro na tela de maneira persistente
     * até que o estado mude
     *
     * @param idpResponse A resposta do provedor de identidade, contendo informações sobre o erro.
     */
    fun notifyLoginFailed(idpResponse: IdpResponse?) {
        _statesFlow.tryEmit(State.Error(idpResponse.toErrorMessage()))
        _eventsChannel.trySend(LoginEvent.Error)
    }

    /**
     * Converte um objeto IdpResponse em uma mensagem de erro legível.
     */
    fun IdpResponse?.toErrorMessage(): String {
        if (this == null) return context.getString(R.string.Voce_cancelou_o_login)

        return when (error?.errorCode) {

            com.firebase.ui.auth.ErrorCodes.NO_NETWORK -> context.getString(R.string.voce_n_o_est_conectado_internet)
            com.firebase.ui.auth.ErrorCodes.DEVELOPER_ERROR -> context.getString(
                R.string.Erro_de_desenvolvimento_c_digo, error?.errorCode, error?.message
            )

            com.firebase.ui.auth.ErrorCodes.PROVIDER_ERROR -> context.getString(
                R.string.Erro_no_provedor_c_digo, error?.errorCode, error?.message
            )

            else -> MessageFormat.format(
                context.getString(R.string.O_login_falhou_c_digo_0_mensagem_1), error?.errorCode, error?.message
            )
        }
    }
}


sealed class LoginEvent {
    data class StartFlow(val asGuest: Boolean) : LoginEvent()
    data class Success(val user: User) : LoginEvent()
    object Error : LoginEvent()

}

/**
 * Representa os estados da interface
 */
sealed class State {
    object Idle : State()
    data class Error(val message: String) : State()
    object LoginIn : State()
}

