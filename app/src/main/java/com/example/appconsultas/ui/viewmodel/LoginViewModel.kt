package com.example.appconsultas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _showSnackbar = MutableStateFlow(false)
    val showSnackbar: StateFlow<Boolean> = _showSnackbar

    fun hideSnackbar() {
        _showSnackbar.value = false
    }

    /**
     * Tenta efetuar o login com as credenciais fornecidas.
     */
    fun attemptLogin(
        username: String,
        password: String,
        adminCredentials: Pair<String, String>,
        clientCredentials: Pair<String, String>,
        onLoginSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (username == adminCredentials.first && password == adminCredentials.second) {
                // Login de Administrador
                onLoginSuccess("admin")
            } else if (username == clientCredentials.first && password == clientCredentials.second) {
                // Login de Cliente (Mock)
                onLoginSuccess("client")
            } else {
                // Credenciais inválidas
                _showSnackbar.value = true
            }
        }
    }
}