package com.example.appconsultas.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

// Mapeamento para Firestore requer construtor vazio e anotações
@IgnoreExtraProperties
data class Cliente(
    @get:Exclude var id: String = "", // Usado como ID do documento/cliente
    var nome: String = "",
    var username: String = "", // Credencial da API (Auth Header)
    var password: String = "", // Hash da API (Auth Header)
    var email: String = "",    // Credencial de Login do Usuário
    var lastLoginTime: String = "N/A",
    var lastQueryTime: String = "N/A"
) {
    // Construtor vazio necessário para o Firebase Firestore
    constructor() : this("", "", "", "", "")
}