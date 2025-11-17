package com.example.appconsultas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appconsultas.data.Cliente

/**
 * Factory personalizada para instanciar o ConsultaViewModel com argumentos.
 */
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