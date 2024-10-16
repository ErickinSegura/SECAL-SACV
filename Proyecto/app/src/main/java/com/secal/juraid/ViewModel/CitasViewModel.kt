package com.secal.juraid.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secal.juraid.Model.UserRepository
import com.secal.juraid.supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CitasViewModel : ViewModel() {
    private val _citasPasadas = MutableStateFlow<List<Cita>>(emptyList())
    val citasPasadas: StateFlow<List<Cita>> = _citasPasadas.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val citas: List<Cita>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadCitas()
        loadCitasPasadas()
    }

    fun loadCitas() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val fetchedCitas = getCitasFromDatabase()
                _uiState.value = UiState.Success(fetchedCitas)
                println("Citas cargadas exitosamente: ${fetchedCitas.size}")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al cargar las citas: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun loadCitasPasadas() {
        viewModelScope.launch {
            try {
                val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val citasPasadas = supabase.from("Citas")
                    .select() {
                        filter {
                            lt("fecha", currentDate)
                            and {
                                eq("estado_representacion", 0) // Solo citas pendientes
                            }
                        }
                    }
                    .decodeList<Cita>()

                _citasPasadas.value = citasPasadas
            } catch (e: Exception) {
                println("Error al cargar citas pasadas: ${e.message}")
            }
        }
    }

    fun representarCita(cita: Cita, abogado: String) {
        viewModelScope.launch {
            try {

                // Crear nuevo caso en la tabla Cases
                val newCase = CasesViewModel.CaseInsert(
                    nombre_abogado = abogado,
                    nombre_cliente = "${cita.nombre} ${cita.apellido}",
                    NUC = "Sin información",
                    carpeta_judicial = "Sin información",
                    carpeta_investigacion = "Sin información",
                    acceso_fv = "Sin información",
                    pass_fv = "Sin información",
                    fiscal_titular = "Sin información",
                    id_unidad_investigacion = null,
                    drive = "https://drive.google.com",
                    status = 1,

                )

                supabase.from("Cases")
                    .insert(newCase)
                    .decodeSingle<Case>()

                updateCitaEstado(cita.id, 1) // 1 para representada

            } catch (e: Exception) {
                println("Error al representar cita: ${e.message}")
                updateCitaEstado(cita.id, 1)
            }
        }
    }

    fun rechazarCita(citaId: Int) {
        viewModelScope.launch {
            try {
                updateCitaEstado(citaId, 2) // 2 para rechazada
            } catch (e: Exception) {
                println("Error al rechazar cita: ${e.message}")
            }
        }
    }

    fun cancelarCita(cita: Cita, motivo: String) {
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "estado_cita" to false,
                    "motivo_cancelacion" to motivo
                )

                val response = supabase.from("Citas")
                    .update(updates) {
                        filter { eq("id", cita.id) }
                    }

                if (response != null) {
                    println("Cita cancelada exitosamente")
                    loadCitas() // Recargar las citas después de la cancelación
                } else {
                    throw Exception("Error al cancelar la cita: respuesta nula")
                }
            } catch (e: Exception) {
                println("Error al cancelar cita: ${e.message}")
                _uiState.value = UiState.Error("Error al cancelar la cita: ${e.message}")
            }
        }
    }

    private suspend fun updateCitaEstado(citaId: Int, estado: Int) {
        try {
            val response = supabase.from("Citas")
                .update(mapOf("estado_representacion" to estado)) {
                    filter { eq("id", citaId) }
                }

            if (response != null) {
                println("Estado de cita actualizado exitosamente")
                loadCitasPasadas() // Recargar las citas pasadas para reflejar el cambio
            } else {
                println("No se pudo actualizar el estado de la cita")
            }
        } catch (e: Exception) {
            println("Error al actualizar estado de cita: ${e.message}")
        }
    }

    private suspend fun getCitasFromDatabase(): List<Cita> = withContext(Dispatchers.IO) {
        supabase.from("Citas")
            .select() {
                filter { eq("estado_cita", true) }
            }
            .decodeList<Cita>()
    }

    @Serializable
    data class Cita(
        val id: Int,
        val nombre: String? = null,
        val apellido: String? = null,
        val fecha: String? = null,
        val hora: String? = null,
        val id_region: Int? = null,
        val estado_cita: Boolean? = null,
        val id_situacion: Int? = null,
        val id_usuario: String? = null,
        val motivo_cancelacion: String? = null,
        var estado_representacion: Int? = null
    ) {
        companion object {
            private val regionesMap = mapOf(
                1 to "Apodaca",
                2 to "Escobedo",
                3 to "Guadalupe",
                4 to "Monterrey",
                5 to "San Nicolás de los Garza",
                6 to "San Pedro Garza García",
                7 to "Otro"
            )

            private val situacionesMap = mapOf(
                1 to "Víctima",
                2 to "Investigado"
            )

            fun getNombreRegion(id: Int?): String = regionesMap[id] ?: "Desconocido"
            fun getNombreSituacion(id: Int?): String = situacionesMap[id] ?: "Desconocido"
        }
    }
}

object ServiceLocator {
    private var supabaseClient: SupabaseClient? = null
    private var userRepository: UserRepository? = null

    fun provideSupabaseClient(): SupabaseClient {
        return supabaseClient ?: throw IllegalStateException("SupabaseClient not initialized")
    }

    fun provideUserRepository(scope: CoroutineScope): UserRepository {
        return userRepository ?: UserRepository(provideSupabaseClient(), scope).also {
            userRepository = it
        }
    }

    fun initialize(supabaseClient: SupabaseClient) {
        this.supabaseClient = supabaseClient
    }
}