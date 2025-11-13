package com.example.appconsultas.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
// IMPORTAÇÕES CORRIGIDAS E COMPLETAS (garantidas pela dependência 'material-icons-extended')
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Plagiarism
// FIM DAS IMPORTAÇÕES DE ÍCONES
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appconsultas.data.ConsultaRecord
import com.example.appconsultas.data.DateUtils
import com.example.appconsultas.ui.viewmodel.ConsultaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.Arrangement
import com.example.appconsultas.data.Cliente
import com.example.appconsultas.ui.viewmodel.Coluna
import com.example.appconsultas.ui.viewmodel.ColunaFiltro
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConsultaScreen(
    viewModel: ConsultaViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    // Coletar estados do ViewModel
    val registos by viewModel.registosFinais.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val registoSelecionado by viewModel.registoSelecionado.collectAsState()
    val userType by viewModel.userType.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val clienteSelecionado by viewModel.clienteSelecionado.collectAsState()

    val context = LocalContext.current
    var showExportMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val csvFileSaverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(viewModel.gerarConteudoCSV().toByteArray())
                }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Erro ao guardar CSV") }
            }
        }
    }
    val xmlFileSaverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/xml")) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(viewModel.gerarConteudoXML().toByteArray())
                }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Erro ao guardar XML") }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Consultas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir Menu")
                    }
                },
                actions = {
                    MenuDeOrdenacao(viewModel = viewModel)
                }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showExportMenu = true }
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Exportar Dados")
                }
                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = { showExportMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Exportar para CSV") },
                        onClick = {
                            showExportMenu = false
                            val timestamp = System.currentTimeMillis()
                            csvFileSaverLauncher.launch("consulta_$timestamp.csv")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Exportar para XML") },
                        onClick = {
                            showExportMenu = false
                            val timestamp = System.currentTimeMillis()
                            xmlFileSaverLauncher.launch("consulta_$timestamp.xml")
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }

            // --- Seleção de Cliente (Apenas para Admin) ---
            if (userType == "admin" && clientes.isNotEmpty()) {
                SelecaoDeCliente(
                    clientes = clientes,
                    clienteSelecionado = clienteSelecionado,
                    onClienteSelecionadoChange = viewModel::onClienteSelecionado
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            // ---------------------------------------------

            ControlesDaConsulta(viewModel, isLoading)
            Spacer(modifier = Modifier.height(16.dp))

            Crossfade(targetState = when {
                isLoading && registos.isEmpty() -> "LOADING"
                registos.isEmpty() -> "EMPTY"
                else -> "LIST"
            }, label = "StateCrossfade") { state ->
                when (state) {
                    "LOADING" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    "EMPTY" -> {
                        EstadoVazio(onRefreshClick = { viewModel.carregarDadosIniciais() })
                    }
                    "LIST" -> {
                        ListaDeRegistosEmCartoes(
                            registos = registos,
                            onRegistoClick = { viewModel.onRegistoClicked(it) }
                        )
                    }
                }
            }
        }

        registoSelecionado?.let { record ->
            DetailsDialog(
                record = record,
                onDismiss = { viewModel.onDetailsDialogDismiss() },
                snackbarHostState = snackbarHostState,
                scope = scope
            )
        }
    }
}

// --- Funções Auxiliares (Com Correções Estruturais) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecaoDeCliente(
    clientes: List<Cliente>,
    clienteSelecionado: Cliente?,
    onClienteSelecionadoChange: (Cliente) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedCliente = clienteSelecionado ?: Cliente(id = "all", nome = "TODOS OS CLIENTES", username = "", password = "", email = "")

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = selectedCliente.nome,
                onValueChange = {},
                readOnly = true,
                label = { Text("Cliente Ativo (Admin)") },
                leadingIcon = { Icon(Icons.Default.Group, contentDescription = "Clientes") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Opção "Todos os Clientes"
                DropdownMenuItem(
                    text = { Text("TODOS OS CLIENTES") },
                    onClick = {
                        clientes.firstOrNull()?.let { onClienteSelecionadoChange(it) }
                        expanded = false
                    }
                )
                // Opções de Clientes específicos
                clientes.forEach { cliente ->
                    DropdownMenuItem(
                        text = { Text(cliente.nome) },
                        onClick = {
                            onClienteSelecionadoChange(cliente)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlesDaConsulta(viewModel: ConsultaViewModel, isLoading: Boolean) {
    val textoIdConsulta by viewModel.textoIdConsulta.collectAsState()
    val textoDoFiltro by viewModel.textoDoFiltro.collectAsState()
    val colunaFiltro by viewModel.colunaFiltroSelecionada.collectAsState()

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = textoIdConsulta,
                onValueChange = { viewModel.onTextoIdChange(it) },
                label = { Text("Consultar por ID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.consultarPorId() },
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Search, contentDescription = "Consultar por ID")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = textoDoFiltro,
                onValueChange = { viewModel.onTextoDoFiltroChange(it) },
                label = { Text("Filtrar na lista...") },
                leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Ícone de filtro") },
                trailingIcon = {
                    if (textoDoFiltro.isNotEmpty()) {
                        IconButton(onClick = { viewModel.limparFiltros() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar filtro")
                        }
                    }
                },
                modifier = Modifier.weight(2f),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.width(8.dp))
            FiltroColunaDropDown(
                selected = colunaFiltro,
                onSelectedChange = { viewModel.onColunaFiltroChange(it) },
                modifier = Modifier.weight(1.5f),
                enabled = !isLoading
            )
        }
    }
}

@Composable
fun MenuDeOrdenacao(viewModel: ConsultaViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val colunaOrdenacao by viewModel.colunaOrdenacao.collectAsState()
    val ordemDescendente by viewModel.ordemDescendente.collectAsState()

    val colunasParaOrdenar = listOf(
        Coluna.DATA_HORA,
        Coluna.PLACA,
        Coluna.ID_MENSAGEM,
        Coluna.TRACK_ID,
        Coluna.LATITUDE,
        Coluna.LONGITUDE
    )

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Sort, contentDescription = "Ordenar por", tint = MaterialTheme.colorScheme.onPrimary)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            colunasParaOrdenar.forEach { coluna ->
                DropdownMenuItem(
                    text = { Text(coluna.displayName) },
                    onClick = {
                        viewModel.onOrdenarPor(coluna)
                        expanded = false
                    },
                    leadingIcon = {
                        if (colunaOrdenacao == coluna) {
                            Icon(
                                imageVector = if (ordemDescendente) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = "Ordem atual"
                            )
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ListaDeRegistosEmCartoes(
    registos: List<ConsultaRecord>,
    onRegistoClick: (ConsultaRecord) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(registos) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(registos, key = { it.idMensagem }) { registo ->
                RegistoCard(
                    registo = registo,
                    onClick = { onRegistoClick(registo) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistoCard(registo: ConsultaRecord, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Plagiarism,
                contentDescription = "Ícone de Registo",
                modifier = Modifier.padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = registo.placa ?: "Sem Placa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TrackID: ${registo.trackId?.toString() ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = DateUtils.formatarDataHora(registo.dataHora),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EstadoVazio(onRefreshClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nenhum Resultado", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "A sua busca não encontrou registos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRefreshClick) {
            Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tentar Novamente")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsDialog(
    record: ConsultaRecord,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    // Lista de detalhes fora do Composable lambda 'text'
    val detailsList = remember(record) {
        listOf(
            "Data/Hora" to DateUtils.formatarDataHora(record.dataHora),
            "IDMENSAGEM" to (record.idMensagem.toString()),
            "Latitude" to (record.latitude?.toString() ?: "N/A"),
            "Longitude" to (record.longitude?.toString() ?: "N/A"),
            "Placa" to (record.placa ?: "N/A"),
            "TrackID" to (record.trackId?.toString() ?: "N/A")
        )
    }

    val detailsString = remember(record) {
        detailsList.joinToString("\n") { (key, value) -> "$key: $value" }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = "Detalhes") },
        title = { Text("Detalhes do Registo") },
        text = {
            // CORREÇÃO: Coluna dentro do Composable lambda 'text'
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                detailsList.forEach { (key, value) ->
                    Row {
                        Text("$key:", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                        Text(value)
                    }
                }
            }
        },
        confirmButton = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalArrangement = Arrangement.Center // Uso correto de Arrangement.Vertical
            ) {
                if (record.latitude != null && record.longitude != null) {
                    TextButton(
                        onClick = {
                            val gmmIntentUri = Uri.parse("geo:${record.latitude},${record.longitude}?q=${record.latitude},${record.longitude}(Registo)")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                            }
                        }
                    ) {
                        Text("Ver no Mapa")
                    }
                }

                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(detailsString))
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Detalhes copiados!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar Detalhes",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Copiar")
                }

                Button(onClick = onDismiss) {
                    Text("Fechar")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltroColunaDropDown(
    selected: ColunaFiltro,
    onSelectedChange: (ColunaFiltro) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val items = ColunaFiltro.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Coluna") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            enabled = enabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    onClick = {
                        onSelectedChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}