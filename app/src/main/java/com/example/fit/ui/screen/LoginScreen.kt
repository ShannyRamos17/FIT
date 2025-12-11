package com.example.fit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fit.ui.viewmodel.LoginViewModel
import com.example.fit.ui.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    // CAMBIO 1: Ahora esta función acepta un String (el nombre del usuario)
    onNavigateToHome: (String) -> Unit,
    initialUser: String? = null,
    initialPass: String? = null
) {
    val uiState by loginViewModel.uiState.collectAsState()

    // Si recibimos datos del registro, actualizamos el ViewModel
    LaunchedEffect(initialUser, initialPass) {
        if (!initialUser.isNullOrEmpty()) {
            loginViewModel.onUsuarioChanged(initialUser)
        }
        if (!initialPass.isNullOrEmpty()) {
            loginViewModel.onContrasenaChanged(initialPass)
        }
    }

    LoginContent(
        uiState = uiState,
        onUsuarioChange = { loginViewModel.onUsuarioChanged(it) },
        onContrasenaChange = { loginViewModel.onContrasenaChanged(it) },
        onLoginClick = {
            loginViewModel.onLoginClicked()
            // CAMBIO 2: Enviamos el usuario actual (uiState.usuario) al navegar
            onNavigateToHome(uiState.usuario)
        },
        onRegisterClick = {
            loginViewModel.onRegisterClicked()
            onNavigateToRegister()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onUsuarioChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Inicio de Sesión",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OutlinedTextField(
            value = uiState.usuario,
            onValueChange = onUsuarioChange,
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.contrasena,
            onValueChange = onContrasenaChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Iniciar Sesión", color = Color.White)
            }

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Registrarse", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginContent(
        uiState = LoginUiState(),
        onUsuarioChange = {},
        onContrasenaChange = {},
        onLoginClick = {},
        onRegisterClick = {}
    )
}