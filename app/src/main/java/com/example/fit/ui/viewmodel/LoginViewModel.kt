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
    // StateFlow para manejar el estado de forma reactiva
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Eventos para actualizar el usuario
    fun onUsuarioChanged(usuario: String) {
        _uiState.update { it.copy(usuario = usuario) }
    }

    // Eventos para actualizar la contraseña
    fun onContrasenaChanged(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena) }
    }

    // Lógica al presionar Iniciar Sesión
    fun onLoginClicked() {
        // Aquí iría la lógica de autenticación
        println("Iniciando sesión con: ${_uiState.value.usuario}")
    }

    // Lógica al presionar Registrarse
    fun onRegisterClicked() {
        println("Navegar a registro")
    }
}