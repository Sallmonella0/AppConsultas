package com.example.appconsultas.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Cliente(
    @get:Exclude var id: String = "",
    var nome: String = "",
    var username: String = "",
    var apiPassword: String = "",
    var email: String = "",
    var lastLoginTime: String = "N/A",
    var lastQueryTime: String = "N/A",
    var isAdmin: Boolean = false
) {
    // Construtor vazio
    constructor() : this("", "", "", "", "", "", "", false)
}