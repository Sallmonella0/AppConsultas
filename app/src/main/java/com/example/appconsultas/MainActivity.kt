package com.example.appconsultas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appconsultas.data.Cliente
import com.example.appconsultas.data.ClientDataStore
import com.example.appconsultas.ui.screen.AdminStatusScreen
import com.example.appconsultas.ui.screen.ConsultaScreen
import com.example.appconsultas.ui.screen.LoginScreen
import com.example.appconsultas.ui.theme.AppConsultasTheme
import com.example.appconsultas.ui.viewmodel.ConsultaViewModel
import com.example.appconsultas.ui.viewmodel.ConsultaViewModelFactory
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    private val dataStore = ClientDataStore

    // Referências a funções suspend do Repositório
    private val updateClientLastLogin: suspend (String) -> Unit = dataStore::updateClientLastLogin
    private val updateClientLastQuery: suspend (String, String?) -> Unit = dataStore::updateClientLastQuery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Estado do tema
            var darkTheme by remember { mutableStateOf(false) }

            // CORRIGIDO: Usa o estado local para carregar os clientes
            var masterClientList by remember { mutableStateOf<List<Cliente>>(emptyList()) }
            var isLoadingClients by remember { mutableStateOf(true) }

            // CORRIGIDO: Usa LaunchedEffect para chamar a função suspend getAllClients()
            LaunchedEffect(Unit) {
                // Chama a função suspend dentro de um coroutine
                masterClientList = dataStore.getAllClients()
                isLoadingClients = false
            }

            AppConsultasTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(), // Uso do Modifier corrigido
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    if (isLoadingClients) {
                        // Você pode adicionar um CircularProgressIndicator ou tela de carregamento aqui.
                    } else {
                        NavHost(navController = navController, startDestination = "login") {

                            // Rota de Login
                            composable("login") {
                                LoginScreen(
                                    navController = navController,
                                    onSuccessfulLogin = updateClientLastLogin
                                )
                            }

                            // ROTA ADMIN (Consulta - Acesso unificado)
                            composable("main/admin") {
                                // masterClientList é do tipo List<Cliente>, resolve ambiguidades
                                val clientesNaoAdmin = masterClientList.filter { cliente -> !cliente.isAdmin }

                                val factory = ConsultaViewModelFactory(
                                    clientes = clientesNaoAdmin,
                                    onUpdateClientUsage = updateClientLastQuery
                                )
                                val viewModel: ConsultaViewModel = viewModel(factory = factory)

                                val currentDarkTheme by viewModel.darkTheme.collectAsState()
                                darkTheme = currentDarkTheme

                                ConsultaScreen(
                                    viewModel = viewModel,
                                    navController = navController
                                )
                            }

                            // ROTA CLIENTE (Consulta)
                            composable(
                                route = "main/client/{username}",
                                arguments = listOf(
                                    navArgument("username") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val username = backStackEntry.arguments?.getString("username") ?: ""

                                // masterClientList é do tipo List<Cliente>, resolve ambiguidades
                                val clienteLogado = masterClientList.firstOrNull { it: Cliente -> it.username == username }

                                if (clienteLogado != null) {
                                    val factory = ConsultaViewModelFactory(
                                        clientes = listOf(clienteLogado),
                                        onUpdateClientUsage = updateClientLastQuery
                                    )
                                    val viewModel: ConsultaViewModel = viewModel(factory = factory)

                                    val currentDarkTheme by viewModel.darkTheme.collectAsState()
                                    darkTheme = currentDarkTheme

                                    ConsultaScreen(
                                        viewModel = viewModel,
                                        navController = navController
                                    )
                                } else {
                                    navController.popBackStack("login", inclusive = false)
                                }
                            }

                            // ROTA ADMIN STATUS
                            composable("adminStatus") {
                                // Filtra para a tela de Status
                                val clientesParaStatus = masterClientList.filter { cliente -> !cliente.isAdmin }

                                AdminStatusScreen(
                                    navController = navController,
                                    clientes = clientesParaStatus // Passa a lista filtrada diretamente
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// REMOVIDA A DECLARAÇÃO DUPLICADA DO CONSULTAVIEWMODELFACTORY AQUI
// (Se você criou o arquivo separado no passo 1, este arquivo está limpo.)