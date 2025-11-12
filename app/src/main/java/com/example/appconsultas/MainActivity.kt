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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appconsultas.data.Cliente
import com.example.appconsultas.ui.screen.AppDrawerContent
import com.example.appconsultas.ui.screen.ConsultaScreen
import com.example.appconsultas.ui.screen.LoginScreen
import com.example.appconsultas.ui.theme.AppConsultasTheme
import com.example.appconsultas.ui.viewmodel.ConsultaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // A LISTA MESTRE (com senhas REAIS) vive aqui, em segurança.
    private val clientesMasterList = listOf(
        Cliente(id = "vip", nome = "VIP", username = "vip", password = "83114d8fc3164de4e85b4e6ee8a04bbd"),
        Cliente(id = "ckl", nome = "CKL", username = "ckl", password = "d4f864e8421eb0bf07384a1ae831ab7b"),
        Cliente(id = "reverselog", nome = "ReverseLog", username = "reverselog", password = "dbbc6fa7bdaa7c9092a2b2560594ec55"),
        Cliente(id = "rodoleve", nome = "Rodoleve", username = "rodoleve", password = "d3a0b71c95972adf17822e30362680f8"),
        Cliente(id = "servidorGxBeloog", nome = "Servidor Gx", username = "servidorGxBeloog", password = "0330265cbb2bf452ae54226c43b3d081"),
        Cliente(id = "gallotti", nome = "Gallotti", username = "gallotti", password = "0cb9bcfb3bd24a8ad373bb1c005e25c0"),
        Cliente(id = "transgires", nome = "Transgires", username = "transgires", password = "18833edf8866b7f280266ecee733a43d"),
        Cliente(id = "agregamais", nome = "AgregaMais", username = "agregamais", password = "eabfe1ffdb963ed3656da4ed91f7b37a")
    )

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

                    NavHost(navController = navController, startDestination = "login") {

                        // Rota de Login
                        composable("login") {
                            LoginScreen(navController = navController)
                        }

                        // ROTA ADMIN: Carrega o ViewModel com TODOS os clientes
                        composable("main/admin") {
                            val factory = ConsultaViewModelFactory(clientesMasterList)
                            val viewModel: ConsultaViewModel = viewModel(factory = factory)

                            val currentDarkTheme by viewModel.darkTheme.collectAsState()
                            darkTheme.value = currentDarkTheme

                            MainScreen(viewModel = viewModel)
                        }

                        // ROTA CLIENTE: Carrega o ViewModel com APENAS UM cliente
                        composable(
                            route = "main/client/{username}",
                            arguments = listOf(
                                navArgument("username") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: ""
                            val clienteLogado = clientesMasterList.firstOrNull { it.username == username }

                            if (clienteLogado != null) {
                                val factory = ConsultaViewModelFactory(listOf(clienteLogado)) // Lista com 1 item
                                val viewModel: ConsultaViewModel = viewModel(factory = factory)

                                val currentDarkTheme by viewModel.darkTheme.collectAsState()
                                darkTheme.value = currentDarkTheme

                                MainScreen(viewModel = viewModel)
                            } else {
                                // Se o cliente não for encontrado, volta ao login
                                navController.popBackStack("login", inclusive = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

// O Composable da tela principal, para evitar duplicação
@Composable
fun MainScreen(viewModel: ConsultaViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                viewModel = viewModel,
                onCloseDrawer = {
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


// O Factory agora aceita uma LISTA de clientes
class ConsultaViewModelFactory(private val clientes: List<Cliente>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConsultaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConsultaViewModel(clientes) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}