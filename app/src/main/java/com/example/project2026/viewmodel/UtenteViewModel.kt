package com.example.project2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.Utente
import com.example.project2026.utility.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class UtenteViewModel(application: Application) : AndroidViewModel(application) {

    private val utenteDao = AppDatabase.getDatabase(application).utenteDao()
    val sessionManager = SessionManager(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    /**
     * Tentativo di login: cerca l'utente nel DB e salva la sessione
     */
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Compila tutti i campi")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val utente = utenteDao.login(username.trim(), password.trim())
            if (utente != null) {
                sessionManager.idUtente = utente.id
                sessionManager.usernameUtente = utente.username
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Credenziali non valide")
            }
        }
    }

    /**
     * Registrazione nuovo utente
     */
    fun registra(username: String, password: String, confermaPassword: String) {
        when {
            username.isBlank() || password.isBlank() -> {
                _authState.value = AuthState.Error("Compila tutti i campi")
                return
            }
            username.trim().length < 3 -> {
                _authState.value = AuthState.Error("L'username deve avere almeno 3 caratteri")
                return
            }
            password.trim().length < 6 -> {
                _authState.value = AuthState.Error("La password deve avere almeno 6 caratteri")
                return
            }
            password != confermaPassword -> {
                _authState.value = AuthState.Error("Le password non coincidono")
                return
            }
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val id = utenteDao.registra(Utente(username = username.trim(), password = password.trim()))
                sessionManager.idUtente = id.toInt()
                sessionManager.usernameUtente = username.trim()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Username già esistente")
            }
        }
    }

    /**
     * Esegue il logout cancellando la sessione
     */
    fun logout() {
        sessionManager.logout()
        _authState.value = AuthState.Idle
    }

    /**
     * Resetta lo stato (ad es. dopo aver gestito un errore)
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
