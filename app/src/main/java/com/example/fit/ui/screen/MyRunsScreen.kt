package com.example.fit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit.ui.viewmodel.MyRunsViewModel
import com.example.fit.ui.viewmodel.RunItem

@Composable
fun MyRunsScreen(
    viewModel: MyRunsViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToNewRun: () -> Unit,
    onRunClick: (RunItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var runIdToDelete by remember { mutableStateOf<Int?>(null) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFFE0E0E0), // Color gris claro para combinar con las cards
            title = {
                Text(
                    text = "¿Esta seguro que desea eliminar este recorrido?",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            confirmButton = {
                // Botón SÍ
                Button(
                    onClick = {
                        runIdToDelete?.let { viewModel.deleteRun(it) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sí", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("No", color = Color.White)
                }
            }
        )
    }

    MyRunsContent(
        runs = uiState.runs,
        isLoading = uiState.isLoading,
        onLogoutClick = {
            viewModel.onLogoutClicked()
            onNavigateToLogin()
        },
        onNewRunClick = {
            viewModel.onNewRunClicked()
            onNavigateToNewRun()
        },
        onRunClick = onRunClick,
        onDeleteClick = { id ->
            runIdToDelete = id
            showDeleteDialog = true
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRunsContent(
    runs: List<RunItem>,
    isLoading: Boolean,
    onLogoutClick: () -> Unit,
    onNewRunClick: () -> Unit,
    onRunClick: (RunItem) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Recorridos", color = Color.Black) },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Salir",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFE0E0E0)
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE0E0E0))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNewRunClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Iniciar Nuevo Recorrido", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(runs) { run ->
                    RunCard(
                        run = run,
                        onClick = onRunClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
fun RunCard(
    run: RunItem,
    onClick: (RunItem) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(run) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = run.usuario, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                IconButton(onClick = { onDeleteClick(run.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Tiempo: ${run.tiempo}", fontSize = 14.sp)
            Text(text = "Distancia: ${run.distancia}", fontSize = 14.sp)
            Text(text = "Velocidad Promedio: ${run.velocidad}", fontSize = 14.sp)
            Text(text = "Última Modificación ${run.fecha}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}