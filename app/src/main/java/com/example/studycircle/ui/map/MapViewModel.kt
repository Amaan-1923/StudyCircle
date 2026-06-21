package com.example.studycircle.ui.map

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycircle.data.repository.LocationRepository
import com.example.studycircle.domain.model.StudentLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MapUiState(
    val currentLocation: Location? = null,
    val nearbyStudents: List<StudentLocation> = emptyList(),
    val isLoading: Boolean = true,
    val isSharing: Boolean = false,
    val selectedSubject: String = "General",
    val error: String? = null
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocationRepository()
    private val fusedLocationClient = LocationServices
        .getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    val subjects = listOf(
        "General", "Math", "Physics",
        "Chemistry", "CS", "OS", "DBMS", "DSA"
    )

    init {
        loadNearbyStudents()
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                val cancellationToken = CancellationTokenSource()
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).await()
                _uiState.value = _uiState.value.copy(
                    currentLocation = location,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Could not get location: ${e.message}"
                )
            }
        }
    }

    fun shareMyLocation() {
        val location = _uiState.value.currentLocation ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSharing = true)
            val success = repository.shareLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                subject = _uiState.value.selectedSubject
            )
            _uiState.value = _uiState.value.copy(
                isSharing = false,
                error = if (!success) "Failed to share location" else null
            )
        }
    }

    fun stopSharing() {
        viewModelScope.launch {
            repository.removeLocation()
        }
    }

    fun selectSubject(subject: String) {
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
    }

    private fun loadNearbyStudents() {
        viewModelScope.launch {
            repository.getNearbyStudents()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
                .collect { students ->
                    _uiState.value = _uiState.value.copy(
                        nearbyStudents = students,
                        isLoading = false
                    )
                }
        }
    }

    fun getDistanceTo(student: StudentLocation): String {
        val location = _uiState.value.currentLocation ?: return "Unknown"
        val distance = repository.calculateDistance(
            location.latitude, location.longitude,
            student.latitude, student.longitude
        )
        return if (distance < 1.0) "${(distance * 1000).toInt()}m"
        else String.format("%.1fkm", distance)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repository.removeLocation()
        }
    }
}