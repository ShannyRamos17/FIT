package com.example.fit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit.data.remote.RetrofitClient
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El modelo de datos
data class RunItem(
    val id: Int = 0,

    @SerializedName("usuario")
    val usuario: String,

    @SerializedName("tiempo")
    val tiempo: String,

    @SerializedName("distancia")
    val distancia: String,

    @SerializedName("velocidad")
    val velocidad: String,

    @SerializedName("fecha")
    val fecha: String
)

data class MyRunsUiState(
    val runs: List<RunItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MyRunsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyRunsUiState())
    val uiState: StateFlow<MyRunsUiState> = _uiState.asStateFlow()


    fun loadRuns(usuario: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val remoteRuns = RetrofitClient.apiService.getRuns(usuario)
                // Invertimos la lista para ver los más nuevos arriba (reversed)
                _uiState.update { it.copy(runs = remoteRuns.reversed(), isLoading = false) }
            } catch (e: Exception) {
                println("Error API: ${e.message}")
                _uiState.update { it.copy(error = "No se pudieron cargar los datos", isLoading = false) }
            }
        }
    }


    fun saveOrUpdateRun(item: RunItem) {
        viewModelScope.launch {

            if (item.id == 0) {

                val listaActual = _uiState.value.runs
                _uiState.update { it.copy(runs = listOf(item) + listaActual) }

                try {
                    RetrofitClient.apiService.createRun(item)
                    println("Nuevo recorrido creado")
                } catch (e: Exception) {
                    println("Error creando: ${e.message}")
                }
            } else {


                val listaActualizada = _uiState.value.runs.map {
                    if (it.id == item.id) item else it
                }
                _uiState.update { it.copy(runs = listaActualizada) }

                try {
                    // 2. Enviamos la actualización a la API
                    RetrofitClient.apiService.updateRun(item.id, item)
                    println("Recorrido actualizado correctamente (ID: ${item.id})")
                } catch (e: Exception) {
                    println("Error actualizando: ${e.message}")
                }
            }
        }
    }


    fun deleteRun(runId: Int) {
        viewModelScope.launch {
            try {

                val listaActualizada = _uiState.value.runs.filter { it.id != runId }
                _uiState.update { it.copy(runs = listaActualizada) }

                RetrofitClient.apiService.deleteRun(runId)
            } catch (e: Exception) {
                println("Error eliminando: ${e.message}")
            }
        }
    }

    fun onLogoutClicked() {
        _uiState.update { MyRunsUiState() }
    }

    fun onNewRunClicked() {
    }

    fun onViewOthersClicked() {
    }
}