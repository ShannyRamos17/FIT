package com.example.fit.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val isLoading: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChanged(usuario: String) {
        _uiState.update { it.copy(usuario = usuario) }
    }

    fun onContrasenaChanged(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena) }
    }

    fun onLoginClicked() {

        println("Iniciando sesión con: ${_uiState.value.usuario}")
    }


    fun onRegisterClicked() {
        println("Navegar a registro")
    }
}