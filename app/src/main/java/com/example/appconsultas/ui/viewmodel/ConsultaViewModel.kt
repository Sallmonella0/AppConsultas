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

class ConsultaViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    // --- StateFlows (Sem alterações) ---
    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes.asStateFlow()
    private val _clienteSelecionado = MutableStateFlow<Cliente?>(null)
    val clienteSelecionado: StateFlow<Cliente?> = _clienteSelecionado.asStateFlow()
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


    // --- Flow Combinado (Sem alterações, 'compareBy' funciona com Long) ---
    val registosFinais: StateFlow<List<ConsultaRecord>> = combine(
        _registosBase, _textoDoFiltro, _colunaFiltroSelecionada, _colunaOrdenacao, _ordemDescendente
    ) { registos, filtro, colunaFiltro, colunaOrd, descendente ->

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

        // 'compareBy { it.idMensagem }' funciona perfeitamente com Long
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


    // --- Inicialização (Sem alterações) ---
    init {
        carregarClientesMock()
        _clientes.value.firstOrNull()?.let { primeiroCliente ->
            _clienteSelecionado.value = primeiroCliente
            carregarDadosIniciais()
        }
    }

    // --- Funções Mock e Auth (Sem alterações) ---
    private fun carregarClientesMock() {
        _clientes.value = listOf(
            Cliente(
                id = "1",
                nome = "VIP",
                username = "vip",
                password = "83114d8fc3164de4e85b4e6ee8a04bbd"
            ),
            Cliente(
                id = "2",
                nome = "CKL",
                username = "ckl",
                password = "d4f864e8421eb0bf07384a1ae831ab7b"
            ),
            Cliente(
                id = "3",
                nome = "Reverselog",
                username = "reverselog",
                password = "dbbc6fa7bdaa7c9092a2b2560594ec55"
            ),
            Cliente(
                id = "4",
                nome = "Rodoleve",
                username = "rodoleve",
                password = "d3a0b71c95972adf17822e30362680f8"
            ),
            Cliente(
                id = "5",
                nome = "ServidorGxBeloog",
                username = "servidorGxBeloog",
                password = "0330265cbb2bf452ae54226c43b3d081"
            ),
            Cliente(
                id = "6",
                nome = "Gallotti",
                username = "gallotti",
                password = "0cb9bcfb3bd24a8ad373bb1c005e25c0"
            ),
            Cliente(
                id = "7",
                nome = "Transgires",
                username = "transgires",
                password = "18833edf8866b7f280266ecee733a43d"
            ),
            Cliente(
                id = "8",
                nome = "Agregamais",
                username = "agregamais",
                password = "eabfe1ffdb963ed3656da4ed91f7b37a"
            )

        )
    }

    private fun gerarAuthHeader(cliente: Cliente): String {
        val credenciais = "${cliente.username}:${cliente.password}"
        val dadosCodificados = Base64.encodeToString(credenciais.toByteArray(), Base64.NO_WRAP)
        return "Basic $dadosCodificados"
    }


    // --- Ações da API (ATUALIZADAS) ---
    fun carregarDadosIniciais() {
        val cliente = _clienteSelecionado.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val authToken = gerarAuthHeader(cliente)

                // --- CORREÇÃO AQUI ---
                // Enviando 0L (como um Long)
                val requestBody = ConsultaRequestBody(idMensagem = 0L)

                _registosBase.value = apiService.consultarDados(
                    authHeader = authToken,
                    requestBody = requestBody
                )
            } catch (e: Exception) {
                _registosBase.value = emptyList()
                e.printStackTrace() // Imprime o erro no Logcat
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

                // --- CORREÇÃO AQUI ---
                // Convertendo o texto do campo para Long? (Número Longo ou Nulo)
                val requestBody = ConsultaRequestBody(idMensagem = id.toLongOrNull())

                _registosBase.value = apiService.consultarDados(
                    authHeader = authToken,
                    requestBody = requestBody
                )
            } catch (e: Exception) {
                _registosBase.value = emptyList()
                e.printStackTrace() // Imprime o erro no Logcat
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Ações da UI (Sem alterações) ---
    fun onClienteSelecionado(cliente: Cliente) {
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

    // --- Ações do Dialog (Sem alterações) ---
    fun onRegistoClicked(record: ConsultaRecord) {
        _registoSelecionado.value = record
    }

    fun onDetailsDialogDismiss() {
        _registoSelecionado.value = null
    }

    // --- Funções de Exportação (Sem alterações) ---
    fun gerarConteudoCSV(): String {
        val header = "IDMENSAGEM,DATAHORA,PLACA,TRACKID,LATITUDE,LONGITUDE\n"
        return header + registosFinais.value.joinToString("\n") {
            "${it.idMensagem},${it.dataHora},${it.placa ?: ""},${it.trackId ?: ""},${it.latitude ?: ""},${it.longitude ?: ""}"
        }
    }

    fun gerarConteudoXML(): String {
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
            builder.append("  </S_Registo>\n") // <-- Bug! Corrigir
        }
        builder.append("</Consultas>")
        return builder.toString()
    }
}