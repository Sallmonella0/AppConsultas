package com.example.appconsultas.data

import com.google.gson.annotations.SerializedName

data class ConsultaRecord(
    @SerializedName("IDMENSAGEM")
    val idMensagem: Long,

    @SerializedName("DATAHORA")
    val dataHora: String,

    @SerializedName("PLACA")
    val placa: String?,

    @SerializedName("TRACKID")
    val trackId: Int?,

    @SerializedName("LATITUDE")
    val latitude: Double?,

    @SerializedName("LONGITUDE")
    val longitude: Double?
)