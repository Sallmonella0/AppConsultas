package com.example.appconsultas.ui.viewmodel

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appconsultas.data.ApiService
import com.example.appconsultas.data.Cliente
import com.example.appconsultas.data.ConsultaRecord
import com.example.appconsultas.data.ConsultaRequestBody
import com.example.appconsultas.data.RetrofitClient
import com.example.appconsultas.data.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Enums (Sem alterações)
enum class Coluna(val displayName: String) {
    DATA_HORA("Data/Hora"),
    PLACA("Placa"),
    ID_MENSAGEM("ID Mensagem"),
    TRACK_ID("Track ID"),
    LATITUDE("Latitude"),
    LONGITUDE("Longitude")
}
enum class ColunaFiltro {
    TODAS,
    PLACA,
    TRACK_ID
}

class ConsultaViewModel(
    clientes: List<Cliente>,
    private val onUpdateClientUsage: suspend (String, String?) -> Unit
) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    // --- StateFlows ---
    private val _clientes = MutableStateFlow(clientes)
    val clientes: StateFlow<List<Cliente>> = _clientes.asStateFlow()

    private val _clienteSelecionado = MutableStateFlow<Cliente?>(clientes.firstOrNull())
    val clienteSelecionado: StateFlow<Cliente?> = _clienteSelecionado.asStateFlow()

    // MODIFICAÇÃO: Determina o tipo de usuário
    val userType: StateFlow<String> = MutableStateFlow(
        if (_clienteSelecionado.value?.isAdmin == true || clientes.size > 1) "admin" else "client"
    ).asStateFlow()

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _registosBase = MutableStateFlow<List<ConsultaRecord>>(emptyList())
    private val _registoSelecionado = MutableStateFlow<ConsultaRecord?>(null)
    val registoSelecionado: StateFlow<ConsultaRecord?> = _registoSelecionado.asStateFlow()
    private val _textoIdConsulta = MutableStateFlow("")
    val textoIdConsulta: StateFlow<String> = _textoIdConsulta.asStateFlow()
    private val _textoDoFiltro = MutableStateFlow("")
    val textoDoFiltro: StateFlow<String> = _textoDoFiltro.asStateFlow()
    private val _colunaOrdenacao = MutableStateFlow(Coluna.DATA_HORA)
    val colunaOrdenacao: StateFlow<Coluna> = _colunaOrdenacao.asStateFlow()
    private val _ordemDescendente = MutableStateFlow(true)
    val ordemDescendente: StateFlow<Boolean> = _ordemDescendente.asStateFlow()
    private val _colunaFiltroSelecionada = MutableStateFlow(ColunaFiltro.TODAS)
    val colunaFiltroSelecionada: StateFlow<ColunaFiltro> = _colunaFiltroSelecionada.asStateFlow()


    // --- Flow Combinado ---
    val registosFinais: StateFlow<List<ConsultaRecord>> = combine(
        _registosBase,
        _textoDoFiltro,
        _colunaFiltroSelecionada,
        _colunaOrdenacao,
        _ordemDescendente,
        _clienteSelecionado
    ) { args ->

        @Suppress("UNCHECKED_CAST")
        val registos: List<ConsultaRecord> = args[0] as List<ConsultaRecord>
        val filtro: String = args[1] as String
        val colunaFiltro: ColunaFiltro = args[2] as ColunaFiltro
        val colunaOrd: Coluna = args[3] as Coluna
        val descendente: Boolean = args[4] as Boolean
        val cliente: Cliente? = args[5] as Cliente?

        val registosFiltrados = if (filtro.isBlank()) {
            registos
        } else {
            registos.filter { registo: ConsultaRecord ->
                when (colunaFiltro) {
                    ColunaFiltro.TODAS -> (registo.placa?.contains(filtro, ignoreCase = true) == true) ||
                            (registo.trackId?.toString()?.contains(filtro, ignoreCase = true) == true)
                    ColunaFiltro.PLACA -> registo.placa?.contains(filtro, ignoreCase = true) == true
                    ColunaFiltro.TRACK_ID -> registo.trackId?.toString()?.contains(filtro, ignoreCase = true) == true
                }
            }
        }

        val comparador: Comparator<ConsultaRecord> = when (colunaOrd) {
            Coluna.DATA_HORA -> compareBy(nullsLast(), ConsultaRecord::dataHora)
            Coluna.PLACA -> compareBy(nullsLast(), ConsultaRecord::placa)
            Coluna.ID_MENSAGEM -> compareBy(ConsultaRecord::idMensagem)
            Coluna.TRACK_ID -> compareBy(nullsLast(), ConsultaRecord::trackId)
            Coluna.LATITUDE -> compareBy(nullsLast(), ConsultaRecord::latitude)
            Coluna.LONGITUDE -> compareBy(nullsLast(), ConsultaRecord::longitude)
        }

        if (descendente) {
            registosFiltrados.sortedWith(comparador.reversed())
        } else {
            registosFiltrados.sortedWith(comparador)
        }

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Inicialização ---
    init {
        _clientes.value = clientes
        // O administrador não tem dados de API para carregar no init,
        // mas o cliente de API sim, se quiser carregar imediatamente.
        _clienteSelecionado.value?.let { cliente ->
            if (clientes.size == 1 && !cliente.isAdmin) {
                carregarDadosIniciais()
            }
        }
    }

    // Atualiza o status da última consulta no Firestore/Repository
    private fun updateLastQueryTime() {
        clienteSelecionado.value?.let { cliente ->
            val currentTime = DateUtils.getCurrentFormattedTime()
            viewModelScope.launch {
                onUpdateClientUsage(cliente.username, currentTime)
            }
        }
    }

    // --- Funções Auth ---
    private fun gerarAuthHeader(cliente: Cliente): String {
        val credenciais = "${cliente.username}:${cliente.apiPassword}"
        val dadosCodificados = Base64.encodeToString(credenciais.toByteArray(), Base64.NO_WRAP)
        return "Basic $dadosCodificados"
    }

    fun logout() {
        Firebase.auth.signOut()
        limparTudo()
    }

    fun setLoggedIn() {} // Função mantida vazia, pois a lógica de login está no LoginScreen

    // --- Ações da API ---
    fun carregarDadosIniciais() {
        val cliente = _clienteSelecionado.value ?: return

        // Verifica se o cliente selecionado é um Admin e o impede de chamar a API com credenciais inválidas.
        if (cliente.isAdmin) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val authToken = gerarAuthHeader(cliente)
                val requestBody = ConsultaRequestBody(idMensagem = 0L)

                _registosBase.value = apiService.consultarDados(
                    authHeader = authToken,
                    requestBody = requestBody
                )
                updateLastQueryTime() // Chamada após sucesso
            } catch (e: Exception) {
                _registosBase.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun consultarPorId() {
        val cliente = _clienteSelecionado.value ?: return
        val id = _textoIdConsulta.value

        // Verifica se o cliente selecionado é um Admin e o impede de chamar a API com credenciais inválidas.
        if (cliente.isAdmin) {
            return
        }

        if (id.isBlank()) {
            carregarDadosIniciais()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val authToken = gerarAuthHeader(cliente)
                val requestBody = ConsultaRequestBody(idMensagem = id.toLongOrNull())

                _registosBase.value = apiService.consultarDados(
                    authHeader = authToken,
                    requestBody = requestBody
                )
                updateLastQueryTime() // Chamada após sucesso
            } catch (e: Exception) {
                _registosBase.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Ações da UI ---
    fun onClienteSelecionado(cliente: Cliente) {
        if (_clienteSelecionado.value?.id == cliente.id) return
        _clienteSelecionado.value = cliente
        limparTudo()
        carregarDadosIniciais()
    }

    fun toggleTheme() {
        _darkTheme.value = !_darkTheme.value
    }

    fun onTextoIdChange(novoTexto: String) {
        _textoIdConsulta.value = novoTexto
    }

    fun onTextoDoFiltroChange(novoTexto: String) {
        _textoDoFiltro.value = novoTexto
    }

    fun onColunaFiltroChange(novaColuna: ColunaFiltro) {
        _colunaFiltroSelecionada.value = novaColuna
    }

    fun limparFiltros() {
        _textoDoFiltro.value = ""
        _colunaFiltroSelecionada.value = ColunaFiltro.TODAS
    }

    private fun limparTudo() {
        _textoIdConsulta.value = ""
        _textoDoFiltro.value = ""
        _registosBase.value = emptyList()
        _colunaFiltroSelecionada.value = ColunaFiltro.TODAS
        _colunaOrdenacao.value = Coluna.DATA_HORA
        _ordemDescendente.value = true
    }

    fun onOrdenarPor(coluna: Coluna) {
        if (_colunaOrdenacao.value == coluna) {
            _ordemDescendente.value = !_ordemDescendente.value
        } else {
            _colunaOrdenacao.value = coluna
            _ordemDescendente.value = true
        }
    }

    fun onRegistoClicked(record: ConsultaRecord) {
        _registoSelecionado.value = record
    }

    fun onDetailsDialogDismiss() {
        _registoSelecionado.value = null
    }

    // --- Funções de Exportação ---
    fun gerarConteudoCSV(): String {
        val header = "IDMENSAGEM,DATAHORA,PLACA,TRACKID,LATITUDE,LONGITUDE\n"
        return header + registosFinais.value.joinToString("\n") {
            "${it.idMensagem},${DateUtils.formatarDataHora(it.dataHora)},${it.placa ?: ""},${it.trackId ?: ""},${it.latitude ?: ""},${it.longitude ?: ""}"
        }
    }

    fun gerarConteudoXML(): String {
        val builder = StringBuilder()
        builder.append("<Consultas>\n")
        registosFinais.value.forEach {
            builder.append("  <Registo>\n")
            builder.append("    <IDMENSAGEM>${it.idMensagem}</IDMENSAGEM>\n")
            builder.append("    <DATAHORA>${DateUtils.formatarDataHora(it.dataHora)}</DATAHORA>\n")
            builder.append("    <PLACA>${it.placa ?: ""}</PLACA>\n")
            builder.append("    <TRACKID>${it.trackId ?: ""}</TRACKID>\n")
            builder.append("    <LATITUDE>${it.latitude ?: ""}</LATITUDE>\n")
            builder.append("    <LONGITUDE>${it.longitude ?: ""}</LONGITUDE>\n")
            builder.append("  </Registo>\n")
        }
        builder.append("</Consultas>")
        return builder.toString()
    }
}