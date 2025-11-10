package com.example.appconsultas.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appconsultas.ui.viewmodel.ConsultaViewModel

@Composable
fun AppDrawerContent(
    viewModel: ConsultaViewModel,
    onCloseDrawer: () -> Unit
) {
    val clientes by viewModel.clientes.collectAsState()
    val clienteSelecionado by viewModel.clienteSelecionado.collectAsState()
    val darkTheme by viewModel.darkTheme.collectAsState()

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
                text = "App Consultas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Clientes",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(clientes) { cliente ->
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    label = { Text(cliente.nome) },
                    selected = cliente.id == clienteSelecionado?.id,
                    onClick = {
                        viewModel.onClienteSelecionado(cliente)
                        onCloseDrawer()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

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
        Spacer(modifier = Modifier.height(16.dp))
    }
}