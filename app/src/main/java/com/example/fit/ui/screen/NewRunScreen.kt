package com.example.fit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit.ui.viewmodel.NewRunViewModel
import com.example.fit.ui.viewmodel.RunItem

@Composable
fun NewRunScreen(
    currentUser: String,
    runToContinue: RunItem? = null, // <--- CAMBIO 1: Recibimos el recorrido opcional
    viewModel: NewRunViewModel = viewModel(),
    onRunFinished: (RunItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val formattedTime = viewModel.formatTime(uiState.tiempoSegundos)

    // CAMBIO 2: Si recibimos un recorrido para continuar, inicializamos el ViewModel
    LaunchedEffect(runToContinue) {
        if (runToContinue != null) {
            viewModel.initializeWithRun(runToContinue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        // --- SECCIÓN CENTRAL: CRONÓMETRO E INFO ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.isPaused) "Recorrido PAUSADO" else "Recorrido en progreso...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dato extra para ver que el sensor funciona
            Text(
                text = "Distancia: ${String.format("%.2f", uiState.distanciaMetros)} m",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        // --- SECCIÓN INFERIOR: BOTONES ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón Pausar / Reanudar
            Button(
                onClick = { viewModel.onPauseClicked() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (uiState.isPaused) "Reanudar" else "Pausar recorrido",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botón Detener
            Button(
                onClick = {
                    // CAMBIO 3: Calculamos el ID. Si es 0 es nuevo, si tiene ID es actualización.
                    val originalId = runToContinue?.id ?: 0

                    viewModel.onStopClicked(currentUser, originalId) { resultado ->
                        onRunFinished(resultado)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Detener recorrido", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}