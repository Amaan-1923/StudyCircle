package com.example.studycircle.ui.map

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studycircle.domain.model.StudentLocation
import com.example.studycircle.ui.theme.GradientEnd
import com.example.studycircle.ui.theme.GradientStart
import com.example.studycircle.ui.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSharingLocation by remember { mutableStateOf(false) }

    val locationPermission = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Request permission and get location when granted
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            viewModel.getCurrentLocation()
        }
    }

    // Default camera position (Mumbai)
    val defaultLocation = LatLng(19.0760, 72.8777)
    val currentLatLng = uiState.currentLocation?.let {
        LatLng(it.latitude, it.longitude)
    } ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 13f)
    }

    // Move camera when location updates
    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude), 14f
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "🗺️ Nearby Students",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.nearbyStudents.size} students studying nearby",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }

        // Permission request
        if (!locationPermission.status.isGranted) {
            LocationPermissionCard(
                onRequestPermission = { locationPermission.launchPermissionRequest() }
            )
        } else {
            // Google Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapType = MapType.NORMAL
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = true
                    )
                ) {
                    // Show nearby students as markers
                    uiState.nearbyStudents.forEach { student ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    student.latitude,
                                    student.longitude
                                )
                            ),
                            title = student.name,
                            snippet = "Studying: ${student.subject}"
                        )
                    }
                }

                // Share location FAB
                FloatingActionButton(
                    onClick = {
                        if (!isSharingLocation) {
                            isSharingLocation = true
                            viewModel.shareMyLocation()
                        } else {
                            isSharingLocation = false
                            viewModel.stopSharing()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = if (isSharingLocation)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (isSharingLocation)
                            Icons.Filled.LocationOff
                        else Icons.Filled.LocationOn,
                        contentDescription = "Share Location",
                        tint = Color.White
                    )
                }
            }

            // Subject selector
            if (!isSharingLocation) {
                LazyRow(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.subjects) { subject ->
                        val isSelected = subject == uiState.selectedSubject
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectSubject(subject) },
                            label = {
                                Text(
                                    text = subject,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected)
                                        FontWeight.SemiBold
                                    else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Nearby students list
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.nearbyStudents.isEmpty()) {
                    item {
                        EmptyNearbyState()
                    }
                } else {
                    item {
                        Text(
                            text = "Nearby Students",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(uiState.nearbyStudents) { student ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            NearbyStudentCard(
                                student = student,
                                distance = viewModel.getDistanceTo(student)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionCard(
    onRequestPermission: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📍", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Location Permission Needed",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Allow location access to see and connect with students studying near you",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Grant Permission",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NearbyStudentCard(
    student: StudentLocation,
    distance: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "📚 ${student.subject}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "📍 $distance",
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyNearbyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🔍", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No students nearby yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Share your location to let others find you!",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}