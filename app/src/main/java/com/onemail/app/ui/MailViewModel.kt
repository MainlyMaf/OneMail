package com.onemail.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onemail.app.data.AccountData
import com.onemail.app.data.MailRepository
import com.onemail.app.data.MailStore
import com.onemail.app.data.MessageDetail
import com.onemail.app.data.MessageSummary
import com.onemail.app.util.userMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class MailUiState(
    val account: AccountData? = null,
    val creating: Boolean = false,
    val deleting: Boolean = false,
    val messages: List<MessageSummary> = emptyList(),
    val loadingMessages: Boolean = false,
    val refreshing: Boolean = false
)

class MailViewModel(application: Application) : AndroidViewModel(application) {

    private val store = MailStore(application)
    private val repository = MailRepository()

    private val _state = MutableStateFlow(MailUiState(account = store.loadAccount()))
    val state: StateFlow<MailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var pollJob: Job? = null

    init {
        if (_state.value.account != null) {
            startPolling()
            refreshMessages(silent = true)
        }
    }

    fun notify(message: String) {
        _events.tryEmit(message)
    }

    fun createMailbox() {
        if (_state.value.creating) return
        viewModelScope.launch {
            _state.update { it.copy(creating = true) }
            try {
                val account = repository.createAccount()
                store.save(account)
                _state.update {
                    it.copy(
                        account = account,
                        creating = false,
                        messages = emptyList(),
                        loadingMessages = false,
                        refreshing = false
                    )
                }
                notify("Почта ${account.address} готова")
                startPolling()
                refreshMessages(silent = true)
            } catch (e: Exception) {
                _state.update { it.copy(creating = false) }
                notify(e.userMessage())
            }
        }
    }

    fun deleteMailbox() {
        val account = _state.value.account ?: return
        if (_state.value.deleting) return
        viewModelScope.launch {
            _state.update { it.copy(deleting = true) }
            try {
                withAuth { token -> repository.deleteAccount(token, account.id) }
            } catch (_: Exception) {
                // Сеть может быть недоступна — локальные данные всё равно очищаем.
            } finally {
                store.clear()
                stopPolling()
                _state.value = MailUiState()
                notify("Почта удалена")
            }
        }
    }

    fun refreshMessages(silent: Boolean = false, userInitiated: Boolean = false) {
        if (_state.value.account == null) return
        if (_state.value.loadingMessages) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMessages = true, refreshing = userInitiated) }
            try {
                val messages = withAuth { token -> repository.fetchMessages(token) }
                _state.update {
                    it.copy(
                        messages = messages,
                        loadingMessages = false,
                        refreshing = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMessages = false, refreshing = false) }
                if (!silent) notify(e.userMessage())
            }
        }
    }

    suspend fun loadMessage(id: String): MessageDetail = withAuth { token ->
        repository.fetchMessage(token, id)
    }

    private suspend fun <T> withAuth(block: suspend (String) -> T): T {
        val account = store.loadAccount()
            ?: throw IllegalStateException("Почта ещё не создана")
        return try {
            block(account.token)
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 403) {
                val freshToken = repository.obtainToken(account.address, account.password)
                store.saveToken(freshToken)
                block(freshToken)
            } else {
                throw e
            }
        }
    }

    private fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                if (_state.value.account == null) break
                try {
                    val messages = withAuth { token -> repository.fetchMessages(token) }
                    _state.update { it.copy(messages = messages) }
                } catch (_: Exception) {
                    // Тихо ждём следующую попытку.
                }
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 5_000L
    }
}
