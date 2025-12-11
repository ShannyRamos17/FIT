package com.example.fit

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fit.ui.screen.LoginScreen
import com.example.fit.ui.screen.MyRunsScreen
import com.example.fit.ui.screen.NewRunScreen
import com.example.fit.ui.screen.RegisterScreen
import com.example.fit.ui.screen.RunDetailScreen
import com.example.fit.ui.theme.FITTheme
import com.example.fit.ui.viewmodel.MyRunsViewModel
import com.example.fit.ui.viewmodel.RunItem
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FITTheme {
                val navController = rememberNavController()
                val gson = Gson()

                NavHost(navController = navController, startDestination = "login") {

                    // --- LOGIN ---
                    composable(
                        route = "login?user={user}&pass={pass}",
                        arguments = listOf(
                            navArgument("user") { defaultValue = "" },
                            navArgument("pass") { defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        val user = backStackEntry.arguments?.getString("user")
                        val pass = backStackEntry.arguments?.getString("pass")

                        LoginScreen(
                            initialUser = user,
                            initialPass = pass,
                            onNavigateToRegister = {
                                navController.navigate("register")
                            },
                            onNavigateToHome = { usuarioLogueado ->
                                val routeUser = if (usuarioLogueado.isBlank()) "Usuario" else usuarioLogueado
                                navController.navigate("home/$routeUser") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // --- REGISTRO ---
                    composable("register") {
                        RegisterScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onRegistrationSuccess = { nuevoUsuario, nuevaContrasena ->
                                val safeUser = nuevoUsuario.trim()
                                val safePass = nuevaContrasena.trim()

                                if (safeUser.isNotEmpty() && safePass.isNotEmpty()) {
                                    navController.navigate("login?user=$safeUser&pass=$safePass") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }

                    // --- HOME (MIS RECORRIDOS) ---
                    composable(
                        route = "home/{user}",
                        arguments = listOf(navArgument("user") { defaultValue = "Usuario" })
                    ) { backStackEntry ->

                        val currentUser = backStackEntry.arguments?.getString("user") ?: "Usuario"
                        val myRunsViewModel: MyRunsViewModel = viewModel()

                        LaunchedEffect(currentUser) {
                            myRunsViewModel.loadRuns(currentUser)
                        }

                        // RECIBIMOS DATOS DEL RECORRIDO
                        val result = backStackEntry.savedStateHandle.get<String>("new_run_data")

                        if (result != null) {
                            backStackEntry.savedStateHandle.remove<String>("new_run_data")
                            // Parseamos: "user|time|dist|speed|date|id"
                            val parts = result.split("|")

                            // Verificación flexible (mínimo 5 partes, ideal 6 con ID)
                            if (parts.size >= 5) {
                                val idRecibido = if (parts.size >= 6) parts[5].toIntOrNull() ?: 0 else 0

                                val runResult = RunItem(
                                    id = idRecibido,
                                    usuario = parts[0],
                                    tiempo = parts[1],
                                    distancia = parts[2],
                                    velocidad = parts[3],
                                    fecha = parts[4]
                                )
                                // Función inteligente: Crea si ID=0, Actualiza si ID>0
                                myRunsViewModel.saveOrUpdateRun(runResult)
                            }
                        }

                        MyRunsScreen(
                            viewModel = myRunsViewModel,
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToNewRun = {
                                navController.navigate("new_run/$currentUser")
                            },
                            onRunClick = { runItem ->
                                val runJson = gson.toJson(runItem)
                                val encodedJson = Uri.encode(runJson)
                                navController.navigate("run_detail/$encodedJson")
                            },
                        )
                    }

                    // --- NUEVO RECORRIDO ---
                    composable(
                        route = "new_run/{user}?runData={runData}",
                        arguments = listOf(
                            navArgument("user") { defaultValue = "Usuario" },
                            navArgument("runData") {
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->

                        val currentUser = backStackEntry.arguments?.getString("user") ?: "Usuario"
                        val runDataJson = backStackEntry.arguments?.getString("runData")

                        val runToContinue = if (runDataJson != null) {
                            gson.fromJson(runDataJson, RunItem::class.java)
                        } else null

                        NewRunScreen(
                            currentUser = currentUser,
                            runToContinue = runToContinue,
                            onRunFinished = { runItem ->
                                val runString = "${runItem.usuario}|${runItem.tiempo}|${runItem.distancia}|${runItem.velocidad}|${runItem.fecha}|${runItem.id}"

                                // --- CORRECCIÓN CLAVE AQUÍ ---
                                // En lugar de usar previousBackStackEntry, buscamos el HOME específicamente
                                // Esto asegura que los datos lleguen al Home, aunque vengamos de "Detalles"
                                try {
                                    val homeEntry = navController.getBackStackEntry("home/$currentUser")
                                    homeEntry.savedStateHandle["new_run_data"] = runString

                                    // Regresamos directamente al Home, cerrando lo que haya en medio (Detalles)
                                    navController.popBackStack("home/$currentUser", inclusive = false)
                                } catch (e: Exception) {
                                    // Si por alguna razón falla, usamos navegación segura
                                    navController.navigate("home/$currentUser") {
                                        popUpTo("home/$currentUser") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // --- DETALLE DEL RECORRIDO ---
                    composable(
                        route = "run_detail/{runJson}",
                        arguments = listOf(navArgument("runJson") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val runJson = backStackEntry.arguments?.getString("runJson")
                        val runItem = gson.fromJson(runJson, RunItem::class.java)

                        RunDetailScreen(
                            run = runItem,
                            onBackClick = { navController.popBackStack() },
                            onContinueClick = { item ->
                                val jsonToSend = Uri.encode(gson.toJson(item))
                                val user = item.usuario
                                navController.navigate("new_run/$user?runData=$jsonToSend")
                            }
                        )
                    }
                }
            }
        }
    }
}