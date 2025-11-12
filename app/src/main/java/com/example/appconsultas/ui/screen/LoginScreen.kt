package com.example.appconsultas.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // 1. Conta de Administrador
    val adminUser = "admin" to "admin123"

    // 2. Lógica de login temporário para CLIENTES
    // Os nomes de utilizador são os mesmos, mas as senhas são username + 123
    val clientesMockLogin = listOf(
        "vip" to "vip123",
        "ckl" to "ckl123",
        "reverselog" to "reverselog123",
        "rodoleve" to "rodoleve123",
        "servidorGxBeloog" to "servidorGxBeloog123",
        "gallotti" to "gallotti123",
        "transgires" to "transgires123",
        "agregamais" to "agregamais123"
    )

    fun handleLogin() {
        val userLower = username.lowercase()

        // Tenta fazer login como Admin
        if (userLower == adminUser.first && password == adminUser.second) {
            error = null
            // Navega para a rota de admin
            navController.navigate("main/admin") {
                popUpTo("login") { inclusive = true }
            }
            return
        }

        // Tenta fazer login como Cliente
        val clienteEncontrado = clientesMockLogin.firstOrNull {
            userLower == it.first && password == it.second
        }

        if (clienteEncontrado != null) {
            error = null
            // Navega para a rota de cliente, passando o username
            navController.navigate("main/client/${clienteEncontrado.first}") {
                popUpTo("login") { inclusive = true }
            }
            return
        }

        // Se falhar
        error = "Utilizador ou senha inválidos."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("App Consultas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Utilizador") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { handleLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}