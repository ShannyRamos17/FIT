package com.example.fit.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegisterUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = ""
)

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(nuevoUsuario: String) {
        _uiState.update { it.copy(usuario = nuevoUsuario) }
    }

    fun onContrasenaChange(nuevaContrasena: String) {
        _uiState.update { it.copy(contrasena = nuevaContrasena) }
    }

    fun onConfirmarContrasenaChange(nuevaConfirmacion: String) {
        _uiState.update { it.copy(confirmarContrasena = nuevaConfirmacion) }
    }

    fun onRegistrarClick() {
        // Aquí iría la validación (ej. si las contraseñas coinciden) y el registro
        println("Registrando usuario: ${_uiState.value.usuario}")
    }

    fun onCancelarClick() {
        println("Cancelando registro... Volver al inicio")
    }
}