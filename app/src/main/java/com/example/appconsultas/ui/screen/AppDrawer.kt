package com.example.appconsultas.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
// IMPORTAÇÕES CORRIGIDAS
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
// FIM DAS IMPORTAÇÕES CORRIGIDAS
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appconsultas.ui.viewmodel.ConsultaViewModel
import kotlinx.coroutines.launch

@Composable
fun AppDrawerContent(
    viewModel: ConsultaViewModel,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAdminStatus: () -> Unit
) {
    val clientes by viewModel.clientes.collectAsState()
    val clienteSelecionado by viewModel.clienteSelecionado.collectAsState()
    val darkTheme by viewModel.darkTheme.collectAsState()
    val userType by viewModel.userType.collectAsState()

    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = if (clientes.size > 1) "App Consultas - Admin" else clienteSelecionado?.nome ?: "App Consultas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (userType == "admin" && clientes.size > 1) {
            Text(
                "Trocar de Cliente",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(clientes.filter { it.username != "admin" }, key = { it.id }) { cliente ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onClienteSelecionado(cliente)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cliente.nome,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (cliente.id == clienteSelecionado?.id) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selecionado",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Item: Status do Administrador
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAdminStatus)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Status do Administrador")
                Spacer(modifier = Modifier.padding(start = 16.dp))
                Text(
                    text = "Status de Clientes",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        // Toggle Tema
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Tema",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(start = 16.dp))
                Text(text = "Modo Escuro", style = MaterialTheme.typography.bodyLarge)
            }
            Switch(
                checked = darkTheme,
                onCheckedChange = { viewModel.toggleTheme() }
            )
        }

        // Botão de Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLogout)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Sair", tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.padding(start = 16.dp))
            Text(
                text = "Sair (Logout)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
} 