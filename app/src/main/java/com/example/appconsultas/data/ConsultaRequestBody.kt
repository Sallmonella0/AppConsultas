package com.example.appconsultas.data

import com.google.gson.annotations.SerializedName

data class ConsultaRequestBody(

    // 1. Usar o nome exato da API (MAIÚSCULAS)
    @SerializedName("IDMENSAGEM")

    // 2. Usar o tipo de dados correto (Int e não String)
    val idMensagem: Long? = null
)