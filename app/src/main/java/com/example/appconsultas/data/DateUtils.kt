package com.example.appconsultas.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {
    private val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun formatarDataHora(dataHoraString: String?): String {
        if (dataHoraString.isNullOrEmpty()) {
            return "Data Inválida"
        }
        return try {
            val dateTime = LocalDateTime.parse(dataHoraString, inputFormatter)
            dateTime.format(outputFormatter)
        } catch (e: DateTimeParseException) {
            dataHoraString
        }
    }
}