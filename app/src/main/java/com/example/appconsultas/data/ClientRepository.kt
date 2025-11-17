package com.example.appconsultas.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface ClientDataSource {
    suspend fun getClientByUsername(username: String): Cliente?
    suspend fun getClientByEmail(email: String): Cliente?
    suspend fun getAllClients(): List<Cliente>
    suspend fun updateClientLastLogin(username: String)
    suspend fun updateClientLastQuery(username: String, time: String?)
}

object ClientDataStore : ClientDataSource {
    private val repository = ClientRepository()

    override suspend fun getClientByUsername(username: String): Cliente? = repository.getClientByUsername(username)
    override suspend fun getClientByEmail(email: String): Cliente? = repository.getClientByEmail(email)
    override suspend fun getAllClients(): List<Cliente> = repository.getAllClients()
    override suspend fun updateClientLastLogin(username: String) = repository.updateClientLastLogin(username)
    override suspend fun updateClientLastQuery(username: String, time: String?) = repository.updateClientLastQuery(username, time)
}

class ClientRepository : ClientDataSource {

    private val db = Firebase.firestore
    private val clientesCollection = db.collection("clientes")

    // Credenciais de Admin hardcoded removidas

    override suspend fun getClientByUsername(username: String): Cliente? {
        return try {
            clientesCollection.document(username)
                .get()
                .await()
                .toObject(Cliente::class.java)?.apply {
                    this.id = username
                }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getClientByEmail(email: String): Cliente? {
        return try {
            // Usa o email para buscar o perfil no Firestore
            clientesCollection.whereEqualTo("email", email.lowercase())
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject(Cliente::class.java)?.apply {
                    this.id = this.username // Assume username é o ID do documento
                }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllClients(): List<Cliente> {
        return try {
            clientesCollection.get().await().documents.mapNotNull { document ->
                document.toObject(Cliente::class.java)?.apply {
                    this.id = this.username
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateClientLastLogin(username: String) {
        val currentTime = DateUtils.getCurrentFormattedTime()
        try {
            clientesCollection.document(username)
                .update("lastLoginTime", currentTime)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateClientLastQuery(username: String, time: String?) {
        try {
            clientesCollection.document(username)
                .update("lastQueryTime", time ?: "N/A")
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}