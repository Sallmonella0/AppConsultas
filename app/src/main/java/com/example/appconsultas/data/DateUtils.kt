package com.example.appconsultas.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    // Função mock para formatação de data/hora da API
    fun formatarDataHora(dataHora: String): String {
        return dataHora
    }

    // Retorna a data e hora atual formatada
    fun getCurrentFormattedTime(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
        return formatter.format(Date())
    }
}