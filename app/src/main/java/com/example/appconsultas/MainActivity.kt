package com.example.appconsultas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
// CORREÇÃO: Import essencial para observar Flow em Compose
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavController
import androidx.navigation.navOptions
import androidx.compose.runtime.setValue
import com.example.appconsultas.data.Cliente
import com.example.appconsultas.data.ClientDataStore // Repositório
import com.example.appconsultas.ui.screen.AppDrawerContent
import com.example.appconsultas.ui.screen.ConsultaScreen
import com.example.appconsultas.ui.screen.LoginScreen
import com.example.appconsultas.ui.screen.AdminStatusScreen
import com.example.appconsultas.ui.theme.AppConsultasTheme
import com.example.appconsultas.ui.viewmodel.ConsultaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dataStore = ClientDataStore

    private val ADMIN_USERNAME = dataStore.ADMIN_USERNAME
    private val ADMIN_PASSWORD = dataStore.ADMIN_PASSWORD

    // Referências a funções suspend do Repositório
    private val updateClientLastLogin: suspend (String) -> Unit = dataStore::updateClientLastLogin
    private val updateClientLastQuery: suspend (String, String?) -> Unit = dataStore::updateClientLastQuery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme = remember { mutableStateOf(false) }

            AppConsultasTheme(darkTheme = darkTheme.value, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Observa o Flow do Repositório em tempo real
                    val masterClientList by dataStore.clientesMasterList.collectAsStateWithLifecycle(initialValue = emptyList())

                    NavHost(navController = navController, startDestination = "login") {

                        // Rota de Login
                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                adminCredentials = ADMIN_USERNAME to ADMIN_PASSWORD,
                                clientesMasterList = masterClientList,
                                onSuccessfulLogin = updateClientLastLogin
                            )
                        }

                        // ROTA ADMIN
                        composable("main/admin") {
                            val factory = ConsultaViewModelFactory(
                                clientes = masterClientList,
                                onUpdateClientUsage = updateClientLastQuery
                            )
                            val viewModel: ConsultaViewModel = viewModel(factory = factory)

                            val currentDarkTheme by viewModel.darkTheme.collectAsState()
                            darkTheme.value = currentDarkTheme

                            MainScreen(
                                viewModel = viewModel,
                                navController = navController,
                                onNavigateToAdminStatus = { navController.navigate("adminStatus") }
                            )
                        }

                        // ROTA CLIENTE
                        composable(
                            route = "main/client/{username}",
                            arguments = listOf(
                                navArgument("username") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: ""

                            // Tipo explícito na lambda para evitar ambiguidade
                            val clienteLogado = masterClientList.firstOrNull { it: Cliente -> it.username == username }

                            if (clienteLogado != null) {
                                val factory = ConsultaViewModelFactory(
                                    clientes = listOf(clienteLogado),
                                    onUpdateClientUsage = updateClientLastQuery
                                )
                                val viewModel: ConsultaViewModel = viewModel(factory = factory)

                                val currentDarkTheme by viewModel.darkTheme.collectAsState()
                                darkTheme.value = currentDarkTheme

                                MainScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    onNavigateToAdminStatus = {}
                                )
                            } else {
                                navController.popBackStack("login", inclusive = false)
                            }
                        }

                        // ROTA ADMIN STATUS
                        composable("adminStatus") {
                            val factory = ConsultaViewModelFactory(
                                clientes = masterClientList,
                                onUpdateClientUsage = { _, _ -> }
                            )
                            val viewModel: ConsultaViewModel = viewModel(factory = factory)
                            AdminStatusScreen(viewModel = viewModel, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

// O Composable da tela principal
@Composable
fun MainScreen(
    viewModel: ConsultaViewModel,
    navController: NavController,
    onNavigateToAdminStatus: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                viewModel = viewModel,
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login", navOptions {
                        popUpTo("main/admin") { inclusive = true }
                    })
                },
                onNavigateToAdminStatus = {
                    onNavigateToAdminStatus()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        ConsultaScreen(
            viewModel = viewModel,
            drawerState = drawerState,
            scope = scope
        )
    }
}

// CORREÇÃO: O Factory aceita uma função suspend
class ConsultaViewModelFactory(
    private val clientes: List<Cliente>,
    private val onUpdateClientUsage: suspend (String, String?) -> Unit
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConsultaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConsultaViewModel(clientes, onUpdateClientUsage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}