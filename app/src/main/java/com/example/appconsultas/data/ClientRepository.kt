package com.example.appconsultas.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query.Direction

// Interface de Repository
interface ClientDataSource {
    val clientesMasterList: Flow<List<Cliente>>
    suspend fun getClientByEmail(email: String): Cliente?
    suspend fun updateClientLastLogin(username: String)
    suspend fun updateClientLastQuery(username: String, lastQueryTime: String?)
}

// Implementação do Repositório usando Firestore
class ClientRepository : ClientDataSource {

    private val db = Firebase.firestore
    private val clientesCollection = db.collection("clientes")

    val ADMIN_USERNAME = "admin"
    val ADMIN_PASSWORD = "admin123"

    /**
     * Retorna a lista de clientes usando um Flow de snapshots do Firestore (Realtime).
     */
    override val clientesMasterList: Flow<List<Cliente>> = clientesCollection
        .orderBy("nome", Direction.ASCENDING)
        .snapshots()
        .map { snapshot: QuerySnapshot ->
            snapshot.documents.mapNotNull { document ->
                val cliente = document.toObject<Cliente>()
                cliente?.apply { id = document.id }
            }
        }

    /**
     * Localiza um cliente pelo Email (usado na autenticação).
     */
    override suspend fun getClientByEmail(email: String): Cliente? {
        return clientesCollection
            .whereEqualTo("email", email.lowercase())
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject<Cliente>()
            ?.apply { id = this.username }
    }

    /**
     * Atualiza o campo lastLoginTime no Firestore.
     */
    override suspend fun updateClientLastLogin(username: String) {
        val clienteDocRef = clientesCollection.document(username)
        clienteDocRef.update("lastLoginTime", DateUtils.getCurrentFormattedTime()).await()
    }

    /**
     * Atualiza o campo lastQueryTime no Firestore.
     */
    override suspend fun updateClientLastQuery(username: String, lastQueryTime: String?) {
        val clienteDocRef = clientesCollection.document(username)
        clienteDocRef.update("lastQueryTime", lastQueryTime ?: "N/A").await()
    }
}

// Instância Singleton do Repositório para uso em outras classes
val ClientDataStore = ClientRepository()