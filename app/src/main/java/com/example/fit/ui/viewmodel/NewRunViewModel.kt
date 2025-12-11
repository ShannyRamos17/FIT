package com.example.fit.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

// Estado de la pantalla
data class NewRunUiState(
    val tiempoSegundos: Long = 0,
    val distanciaMetros: Double = 0.0,
    val pasos: Int = 0,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
)

// Usamos AndroidViewModel para tener acceso al Contexto (necesario para sensores)
class NewRunViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow(NewRunUiState())
    val uiState: StateFlow<NewRunUiState> = _uiState.asStateFlow()

    // Variables para el cronómetro
    private var timerJob: Job? = null

    // Variables para el sensor
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastMagnitude = 0.0

    init {
        startRun()
    }

    // --- NUEVO: Inicializar con datos existentes (Para "Continuar Recorrido") ---
    fun initializeWithRun(run: RunItem) {
        // 1. Convertir "HH:MM:SS" a segundos totales
        val parts = run.tiempo.split(":")
        val segundosTotales = if (parts.size == 3) {
            try {
                (parts[0].toLong() * 3600) + (parts[1].toLong() * 60) + parts[2].toLong()
            } catch (e: Exception) { 0L }
        } else 0L

        // 2. Convertir "12.50 metros" a Double (12.50)
        // Quitamos texto y reemplazamos coma por punto por si acaso
        val distanciaString = run.distancia.replace(" metros", "").replace(",", ".")
        val distanciaLimpia = distanciaString.toDoubleOrNull() ?: 0.0

        // 3. Actualizamos el estado inicial
        _uiState.update {
            it.copy(
                tiempoSegundos = segundosTotales,
                distanciaMetros = distanciaLimpia,
                pasos = (distanciaLimpia / 0.75).toInt(), // Recalculamos pasos aproximados
                isPaused = true // Iniciamos PAUSADO para que el usuario decida cuándo seguir
            )
        }
    }

    private fun startRun() {
        startTimer()
        startSensor()
    }

    // --- LÓGICA DEL CRONÓMETRO ---
    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Esperar 1 segundo
                if (!_uiState.value.isPaused) {
                    _uiState.update { it.copy(tiempoSegundos = it.tiempoSegundos + 1) }
                }
            }
        }
    }

    // --- LÓGICA DEL SENSOR ---
    private fun startSensor() {
        // Registramos el listener del acelerómetro
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopSensor() {
        sensorManager.unregisterListener(this)
    }

    // Se llama cada vez que el sensor detecta movimiento
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || _uiState.value.isPaused) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculamos la magnitud del movimiento
            val magnitude = sqrt((x * x + y * y + z * z).toDouble())
            val delta = magnitude - lastMagnitude
            lastMagnitude = magnitude

            // Detección simple de "paso" o movimiento fuerte (threshold > 2)
            if (delta > 2) {
                _uiState.update {
                    val nuevosPasos = it.pasos + 1
                    // Asumimos 0.75 metros por paso aprox.
                    val nuevaDistancia = nuevosPasos * 0.75
                    it.copy(pasos = nuevosPasos, distanciaMetros = nuevaDistancia)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No necesario para este ejemplo
    }

    // --- ACCIONES DE USUARIO ---

    fun onPauseClicked() {
        _uiState.update { currentState ->
            val newState = !currentState.isPaused
            if (newState) {
                stopSensor() // Pausar sensor
            } else {
                startSensor() // Reanudar sensor
            }
            currentState.copy(isPaused = newState)
        }
    }

    // CAMBIO IMPORTANTE: Recibimos usuarioActual Y el ID original (si existe)
    fun onStopClicked(usuarioActual: String, originalId: Int = 0, onFinish: (RunItem) -> Unit) {
        timerJob?.cancel()
        stopSensor()

        // Preparamos el objeto con los resultados finales
        val segundos = _uiState.value.tiempoSegundos
        val distancia = _uiState.value.distanciaMetros

        // Calcular velocidad (km/h)
        val velocidad = if (segundos > 0) (distancia / 1000) / (segundos / 3600.0) else 0.0

        // Formatear fecha actual
        val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val resultado = RunItem(
            id = originalId, // <--- AQUÍ USAMOS EL ID ORIGINAL (0 si es nuevo, >0 si es update)
            usuario = usuarioActual,
            tiempo = formatTime(segundos),
            distancia = String.format("%.2f metros", distancia),
            velocidad = String.format("%.2f km/h", velocidad),
            fecha = fechaHoy
        )

        onFinish(resultado)
    }

    // Función auxiliar para mostrar "HH:MM:SS"
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    // Limpieza al cerrar
    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }
}