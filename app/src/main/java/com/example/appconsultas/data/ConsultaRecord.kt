package com.example.appconsultas.data

import com.google.gson.annotations.SerializedName

data class ConsultaRecord(
    // --- CORREÇÃO AQUI ---
    @SerializedName("IDMENSAGEM")
    val idMensagem: Long, // Tinha de ser Long, não Int

    @SerializedName("DATAHORA")
    val dataHora: String,

    @SerializedName("PLACA")
    val placa: String?,

    @SerializedName("TRACKID")
    val trackId: Int?, // Int deve ser suficiente aqui, mas se também falhar, mude para Long

    @SerializedName("LATITUDE")
    val latitude: Double?,

    @SerializedName("LONGITUDE")
    val longitude: Double?
)