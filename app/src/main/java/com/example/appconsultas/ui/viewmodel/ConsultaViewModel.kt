package com.example.appconsultas.ui.viewmodel

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appconsultas.data.ApiService
import com.example.appconsultas.data.Cliente
import com.example.appconsultas.data.ConsultaRecord
import com.example.appconsultas.data.ConsultaRequestBody
import com.example.appconsultas.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

// O ViewModel agora RECEBE UMA LISTA de clientes (pode ser 1 ou todos)
class ConsultaViewModel(clientes: List<Cliente>) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    // --- StateFlows ---
    // A lista de clientes que o VM pode ver
    private val _clientes = MutableStateFlow(clientes)
    val clientes: StateFlow<List<Cliente>> = _clientes.asStateFlow()

    // O cliente selecionado (o primeiro da lista por defeito)
    private val _clienteSelecionado = MutableStateFlow<Cliente?>(clientes.firstOrNull())
    val clienteSelecionado: StateFlow<Cliente?> = _clienteSelecionado.asStateFlow()

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()
    // ... (O resto dos StateFlows não muda)
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


    // --- Flow Combinado (Sem alterações) ---
    val registosFinais: StateFlow<List<ConsultaRecord>> = combine(
        _registosBase, _textoDoFiltro, _colunaFiltroSelecionada, _colunaOrdenacao, _ordemDescendente
    ) {
        // ... (lógica de filtro e ordenação não muda)
            registos, filtro, colunaFiltro, colunaOrd, descendente ->

        val registosFiltrados = if (filtro.isBlank()) {
            registos
        } else {
            registos.filter { registo ->
                when (colunaFiltro) {
                    ColunaFiltro.TODAS -> (registo.placa?.contains(filtro, ignoreCase = true) == true) ||
                            (registo.trackId?.toString()?.contains(filtro, ignoreCase = true) == true)
                    ColunaFiltro.PLACA -> registo.placa?.contains(filtro, ignoreCase = true) == true
                    ColunaFiltro.TRACK_ID -> registo.trackId?.toString()?.contains(filtro, ignoreCase = true) == true
                }
            }
        }

        val comparador: Comparator<ConsultaRecord> = when (colunaOrd) {
            Coluna.DATA_HORA -> compareBy { it.dataHora }
            Coluna.PLACA -> compareBy { it.placa }
            Coluna.ID_MENSAGEM -> compareBy { it.idMensagem }
            Coluna.TRACK_ID -> compareBy { it.trackId }
            Coluna.LATITUDE -> compareBy { it.latitude }
            Coluna.LONGITUDE -> compareBy { it.longitude }
        }

        if (descendente) {
            registosFiltrados.sortedWith(comparador.reversed())
        } else {
            registosFiltrados.sortedWith(comparador)
        }

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Inicialização ---
    init {
        // Carrega os dados para o cliente que fez o login
        carregarDadosIniciais()
    }

    // --- Funções Mock e Auth (Sem alterações) ---
    private fun gerarAuthHeader(cliente: Cliente): String {
        val credenciais = "${cliente.username}:${cliente.password}"
        val dadosCodificados = Base64.encodeToString(credenciais.toByteArray(), Base64.NO_WRAP)
        return "Basic $dadosCodificados"
    }


    // --- Ações da API (Sem alterações) ---
    fun carregarDadosIniciais() {
        val cliente = _clienteSelecionado.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val authToken = gerarAuthHeader(cliente)
                val requestBody = ConsultaRequestBody(idMensagem = 0L)

                _registosBase.value = apiService.consultarDados(
                    authHeader = authToken,
                    requestBody = requestBody
                )
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
            } catch (e: Exception) {
                _registosBase.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Ações da UI ---

    // A FUNÇÃO DE TROCA DE CLIENTE ESTÁ DE VOLTA
    fun onClienteSelecionado(cliente: Cliente) {
        if (_clienteSelecionado.value?.id == cliente.id) return
        _clienteSelecionado.value = cliente
        limparTudo() // Limpa os filtros
        carregarDadosIniciais() // Carrega os dados do novo cliente
    }

    // ... (O resto das funções não muda)
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

    // --- Funções de Exportação (Sem alterações) ---
    fun gerarConteudoCSV(): String {
        // ... (código idêntico)
        val header = "IDMENSAGEM,DATAHORA,PLACA,TRACKID,LATITUDE,LONGITUDE\n"
        return header + registosFinais.value.joinToString("\n") {
            "${it.idMensagem},${it.dataHora},${it.placa ?: ""},${it.trackId ?: ""},${it.latitude ?: ""},${it.longitude ?: ""}"
        }
    }

    fun gerarConteudoXML(): String {
        // ... (código idêntico)
        val builder = StringBuilder()
        builder.append("<Consultas>\n")
        registosFinais.value.forEach {
            builder.append("  <Registo>\n")
            builder.append("    <IDMENSAGEM>${it.idMensagem}</IDMENSAGEM>\n")
            builder.append("    <DATAHORA>${it.dataHora}</DATAHORA>\n")
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