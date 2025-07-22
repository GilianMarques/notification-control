package dev.gmarques.controledenotificacoes.presentation.ui.fragments.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.controledenotificacoes.BuildConfig
import dev.gmarques.controledenotificacoes.data.local.room.RoomDatabase
import dev.gmarques.controledenotificacoes.domain.model.User
import dev.gmarques.controledenotificacoes.domain.usecase.preferences.ClearPreferencesUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.LogOffUserUseCase
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationServiceManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 09 de maio de 2025 as 10:12.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val roomDatabase: RoomDatabase,
    private val logOffUserUseCase: LogOffUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val clearPreferencesUseCase: ClearPreferencesUseCase,
) : ViewModel() {

    private val _eventsChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow: Flow<Event> get() = _eventsChannel.receiveAsFlow()

    fun getUser(): User {
        return getUserUseCase() ?: error("usuario nao pode ser nulo aqui")
    }

    fun performLogOff() = viewModelScope.launch(IO) {

        logOffUserUseCase()
        if (!BuildConfig.DEBUG) {
            roomDatabase.clearAllTables()
            clearPreferencesUseCase()
        }
        NotificationServiceManager.stopSelf()
        _eventsChannel.trySend(Event.LogoffDone)
    }

}

/**
 * Representa os eventos (consumo unico) que podem ser disparados para a UI
 */
sealed class Event {
    class PreferencesCleaned(val success: Boolean) : Event()
    object LogoffDone : Event()
}