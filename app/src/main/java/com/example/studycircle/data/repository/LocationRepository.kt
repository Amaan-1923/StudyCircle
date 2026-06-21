package com.example.studycircle.data.repository

import com.example.studycircle.domain.model.StudentLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class LocationRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val locationsRef = database.getReference("locations")

    // Share current user's location
    suspend fun shareLocation(
        latitude: Double,
        longitude: Double,
        subject: String
    ): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            val location = StudentLocation(
                uid = currentUser.uid,
                name = currentUser.displayName
                    ?: currentUser.email?.substringBefore("@")
                    ?: "Anonymous",
                subject = subject,
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis()
            )
            locationsRef.child(currentUser.uid).setValue(location).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get all nearby students in real time
    fun getNearbyStudents(): Flow<List<StudentLocation>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: ""
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val students = snapshot.children.mapNotNull { child ->
                    child.getValue(StudentLocation::class.java)
                }.filter { it.uid != currentUserId } // exclude self
                trySend(students)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        locationsRef.addValueEventListener(listener)
        awaitClose { locationsRef.removeEventListener(listener) }
    }

    // Remove location when user goes offline
    suspend fun removeLocation() {
        val currentUserId = auth.currentUser?.uid ?: return
        locationsRef.child(currentUserId).removeValue().await()
    }

    // Calculate distance between two coordinates in km
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}