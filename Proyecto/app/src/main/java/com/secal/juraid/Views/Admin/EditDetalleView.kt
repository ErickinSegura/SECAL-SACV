package com.secal.juraid.Views.Admin

import AlumnosViewModel
import CaseDetailViewModel
import Student
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.secal.juraid.BottomBar
import com.secal.juraid.Routes
import com.secal.juraid.TitlesView
import com.secal.juraid.TopBar
import com.secal.juraid.ViewModel.CasesViewModel
import com.secal.juraid.ViewModel.unitInvestigation
import kotlinx.coroutines.launch

@Composable
fun EditDetalleView(navController: NavController, viewModel: CaseDetailViewModel, caseId: Int) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
        topBar = { TopBar() },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TitlesView(title = "Edita la Información de Caso")
            Spacer(modifier = Modifier.height(16.dp))
            EditCard(navController, viewModel, caseId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCard(navController: NavController, viewModel: CaseDetailViewModel, caseId: Int) {
    val casesViewModel: CasesViewModel = viewModel()
    val caseDetail by viewModel.caseDetail.collectAsState()
    val hyperlinks by viewModel.hyperlinks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val assignedStudents by casesViewModel.assignedCases.collectAsState()
    val scope = rememberCoroutineScope()

    // Estado para la lista de todos los estudiantes
    val alumnosViewModel: AlumnosViewModel = viewModel()
    val allStudents by alumnosViewModel.students.collectAsState()

    // Estados para el caso
    var nuc by remember { mutableStateOf("") }
    var carpetaJudicial by remember { mutableStateOf("") }
    var carpetaInvestigacion by remember { mutableStateOf("") }
    var acceso_fv by remember { mutableStateOf("") }
    var pass_fv by remember { mutableStateOf("") }
    var id_unidad by remember { mutableStateOf("") }
    var fiscalTitular by remember { mutableStateOf("") }
    var drive by remember { mutableStateOf("") }

    var selectedUnidadInvestigacion by remember { mutableStateOf<unitInvestigation?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Estados para diálogos de hipervínculos
    var showAddHyperlinkDialog by remember { mutableStateOf(false) }
    var newHyperlinkText by remember { mutableStateOf("") }
    var newHyperlinkLink by remember { mutableStateOf("") }
    var showEditHyperlinkDialog by remember { mutableStateOf(false) }
    var editingHyperlink by remember { mutableStateOf<CaseDetailViewModel.Hiperlink?>(null) }
    var editHyperlinkText by remember { mutableStateOf("") }
    var editHyperlinkLink by remember { mutableStateOf("") }
    var showDeleteHyperlinkDialog by remember { mutableStateOf(false) }
    var deletingHyperlink by remember { mutableStateOf<CaseDetailViewModel.Hiperlink?>(null) }

    // Estados para estudiantes
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showDeleteStudentDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    val unitInvestigations by casesViewModel.unitInvestigations.collectAsState()

    var isSubmitting by remember { mutableStateOf(false) }



    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        alumnosViewModel.loadAllData()
    }

    LaunchedEffect(caseId) {
        viewModel.loadCaseDetail(caseId)
    }

    LaunchedEffect(caseDetail) {
        caseDetail?.let { case ->
            nuc = case.NUC
            carpetaJudicial = case.carpeta_judicial
            carpetaInvestigacion = case.carpeta_investigacion
            fiscalTitular = case.fiscal_titular
            drive = case.drive
            acceso_fv = case.acceso_fv
            pass_fv = case.pass_fv
            id_unidad = case.id_unidad_investigacion.toString()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            // Sección de Hipervínculos
            Text(
                "General",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column (modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = nuc,
                        onValueChange = { nuc = it },
                        label = { Text("NUC") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fiscalTitular,
                        onValueChange = { fiscalTitular = it },
                        label = { Text("Fiscal Titular") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedUnidadInvestigacion?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad de Investigación") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),

                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            unitInvestigations.forEach { unidad ->
                                DropdownMenuItem(
                                    text = { Text(unidad.nombre) },
                                    onClick = {
                                        selectedUnidadInvestigacion = unidad
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = carpetaJudicial,
                        onValueChange = { carpetaJudicial = it },
                        label = { Text("Carpeta Judicial") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = carpetaInvestigacion,
                        onValueChange = { carpetaInvestigacion = it },
                        label = { Text("Carpeta de Investigación") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = acceso_fv,
                        onValueChange = { acceso_fv = it },
                        label = { Text("Acceso Fiscalía Virtual") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pass_fv,
                        onValueChange = { pass_fv = it },
                        label = { Text("Contraseña Fiscalía Virtual") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),

                        )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Hipervínculos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = drive,
                        onValueChange = { drive = it },
                        label = { Text("Drive URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),

                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    hyperlinks.forEach { hyperlink ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(hyperlink.texto, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                editingHyperlink = hyperlink
                                editHyperlinkText = hyperlink.texto
                                editHyperlinkLink = hyperlink.link
                                showEditHyperlinkDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar hipervínculo")
                            }
                            IconButton(onClick = {
                                deletingHyperlink = hyperlink
                                showDeleteHyperlinkDialog = true
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar hipervínculo"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Button(
                        onClick = { showAddHyperlinkDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir Hipervínculo")
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {

            Text(
                "Estudiantes Asignados",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    assignedStudents.filter { it.id_Caso == caseId }.forEach { relation ->
                        val student = allStudents.find { it.id == relation.id_alumno }
                        student?.let { currentStudent ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${currentStudent.name} ${currentStudent.first_last_name} ${currentStudent.second_last_name}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = currentStudent.email,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                IconButton(onClick = {
                                    studentToDelete = currentStudent
                                    showDeleteStudentDialog = true
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar estudiante"
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showAddStudentDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir Estudiante")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar Cambios
            Button(
                onClick = {
                    scope.launch {
                        try {
                            viewModel.updateCase(
                                caseId,
                                nuc,
                                carpetaJudicial,
                                carpetaInvestigacion,
                                acceso_fv,
                                pass_fv,
                                fiscalTitular,
                                selectedUnidadInvestigacion?.id,
                                drive
                            )
                            navController.navigate(Routes.casosVw)
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                Text("Guardar cambios")
            }
            }
        }
    }

    // Diálogo para añadir hipervínculo
    if (showAddHyperlinkDialog) {
        HyperlinkDialog(
            title = "Añadir Hipervínculo",
            text = newHyperlinkText,
            link = newHyperlinkLink,
            onTextChange = { newHyperlinkText = it },
            onLinkChange = { newHyperlinkLink = it },
            isLoading = isLoading,
            onDismiss = {
                showAddHyperlinkDialog = false
                newHyperlinkText = ""
                newHyperlinkLink = ""
            },
            onConfirm = {
                scope.launch {
                    viewModel.addHyperlink(caseId, newHyperlinkText, newHyperlinkLink)
                    viewModel.loadCaseDetail(caseId) // Recargar los detalles del caso
                    showAddHyperlinkDialog = false
                    newHyperlinkText = ""
                    newHyperlinkLink = ""
                }
            }
        )
    }

    // Diálogo para editar hipervínculo
    if (showEditHyperlinkDialog) {
        HyperlinkDialog(
            title = "Editar Hipervínculo",
            text = editHyperlinkText,
            link = editHyperlinkLink,
            onTextChange = { editHyperlinkText = it },
            onLinkChange = { editHyperlinkLink = it },
            isLoading = isLoading,
            onDismiss = {
                showEditHyperlinkDialog = false
                editingHyperlink = null
            },
            onConfirm = {
                scope.launch {
                    editingHyperlink?.let { hyperlink ->
                        viewModel.updateHyperlink(
                            hyperlink.id,
                            editHyperlinkText,
                            editHyperlinkLink
                        )
                        viewModel.loadCaseDetail(caseId) // Recargar los detalles del caso
                    }
                    showEditHyperlinkDialog = false
                    editingHyperlink = null
                }
            }
        )
    }

    // Diálogo para eliminar hipervínculo
    if (showDeleteHyperlinkDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteHyperlinkDialog = false },
            title = { Text("Eliminar hipervínculo") },
            text = { Text("¿Estás seguro de que deseas eliminar este hipervínculo?", color = MaterialTheme.colorScheme.onSecondaryContainer) },
            confirmButton = {
                Button(onClick = {
                    deletingHyperlink?.let { hyperlink ->
                        scope.launch {
                            viewModel.deleteHyperlink(hyperlink.id)
                            viewModel.loadCaseDetail(caseId) // Recargar los detalles del caso
                        }
                    }
                    showDeleteHyperlinkDialog = false
                    deletingHyperlink = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteHyperlinkDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }

    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Añadir Estudiante") },
            text = {
                Column {
                    // Lista desplegable de estudiantes disponibles
                    val availableStudents = allStudents.filter { student ->
                        assignedStudents.none { it.id_Caso == caseId && it.id_alumno == student.id }
                    }

                    if (availableStudents.isEmpty()) {
                        Text("No hay estudiantes disponibles para asignar", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            items(availableStudents) { student ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedStudent = student
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (selectedStudent?.id == student.id)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    Color.Transparent
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedStudent?.id == student.id)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${student.name} ${student.first_last_name} ${student.second_last_name}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = student.email,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedStudent?.let { student ->
                            scope.launch {
                                casesViewModel.assignCaseToStudent(student.id, nuc)
                                showAddStudentDialog = false
                                selectedStudent = null
                            }
                        }
                    },
                    enabled = selectedStudent != null
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showAddStudentDialog = false
                    selectedStudent = null
                }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }

    // Diálogo para eliminar estudiante
    if (showDeleteStudentDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteStudentDialog = false },
            title = { Text("Eliminar Estudiante") },
            text = {
                studentToDelete?.let { student ->
                    Column {
                        Text("¿Estás seguro de que deseas eliminar a este estudiante del caso?")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${student.name} ${student.first_last_name} ${student.second_last_name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            student.email,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    studentToDelete?.let { student ->
                        scope.launch {
                            casesViewModel.unassignCaseFromStudent(student.id, caseId)
                            showDeleteStudentDialog = false
                            studentToDelete = null
                        }
                    }
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteStudentDialog = false
                    studentToDelete = null
                }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
fun HyperlinkDialog(
    title: String,
    text: String,
    link: String,
    onTextChange: (String) -> Unit,
    onLinkChange: (String) -> Unit,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text("Texto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = link,
                    onValueChange = onLinkChange,
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        cursorColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    )
}
