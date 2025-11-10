package com.example.appconsultas.data

data class Cliente(
    val id: String,       // Um ID local (ex: "1")
    val nome: String,     // O nome para o menu (ex: "Cliente VIP")
    val username: String, // O username para a API (ex: "vip")
    val password: String  // A senha/hash da API (ex: "83114d8f...")
)