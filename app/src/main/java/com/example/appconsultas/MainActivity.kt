package com.example.appconsultas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.appconsultas.ui.screen.AppDrawerContent
import com.example.appconsultas.ui.screen.ConsultaScreen
import com.example.appconsultas.ui.theme.AppConsultasTheme
import com.example.appconsultas.ui.viewmodel.ConsultaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ConsultaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme by viewModel.darkTheme.collectAsState()

            AppConsultasTheme(darkTheme = darkTheme, dynamicColor = false) {

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppDrawerContent(
                                viewModel = viewModel,
                                onCloseDrawer = {
                                    scope.launch {
                                        drawerState.close()
                                    }
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
            }
        }
    }
}