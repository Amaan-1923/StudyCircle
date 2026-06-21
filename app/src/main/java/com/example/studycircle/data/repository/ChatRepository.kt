package com.example.studycircle.data.repository

import com.example.studycircle.domain.model.ChatRoom
import com.example.studycircle.domain.model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val roomsRef = database.getReference("chatRooms")
    private val messagesRef = database.getReference("messages")

    // Get all chat rooms
    fun getChatRooms(): Flow<List<ChatRoom>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children.mapNotNull { child ->
                    child.getValue(ChatRoom::class.java)?.copy(id = child.key ?: "")
                }
                trySend(rooms)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        roomsRef.addValueEventListener(listener)
        awaitClose { roomsRef.removeEventListener(listener) }
    }

    // Get messages for a room in real time
    fun getMessages(roomId: String): Flow<List<Message>> = callbackFlow {
        val ref = messagesRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { child ->
                    child.getValue(Message::class.java)?.copy(id = child.key ?: "")
                }
                trySend(messages)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Send a message
    suspend fun sendMessage(roomId: String, message: Message): Boolean {
        return try {
            val msgRef = messagesRef.child(roomId).push()
            msgRef.setValue(message.copy(id = msgRef.key ?: "")).await()

            // Update room last message
            roomsRef.child(roomId).child("lastMessage").setValue(message.text).await()
            roomsRef.child(roomId).child("lastMessageTime")
                .setValue(message.timestamp).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Create default chat rooms if they don't exist
    suspend fun initializeDefaultRooms() {
        val snapshot = roomsRef.get().await()
        if (!snapshot.exists()) {
            val defaultRooms = listOf(
                ChatRoom(
                    id = "general",
                    name = "General",
                    description = "General study discussion",
                    subject = "General",
                    emoji = "👋",
                    memberCount = 0
                ),
                ChatRoom(
                    id = "dsa",
                    name = "DSA Study Group",
                    description = "Data Structures & Algorithms",
                    subject = "DSA",
                    emoji = "🌳",
                    memberCount = 0
                ),
                ChatRoom(
                    id = "dbms",
                    name = "DBMS Discussion",
                    description = "Database Management Systems",
                    subject = "DBMS",
                    emoji = "🗄️",
                    memberCount = 0
                ),
                ChatRoom(
                    id = "os",
                    name = "OS Discussion",
                    description = "Operating Systems",
                    subject = "OS",
                    emoji = "⚙️",
                    memberCount = 0
                ),
                ChatRoom(
                    id = "math",
                    name = "Mathematics",
                    description = "Math problems and solutions",
                    subject = "Math",
                    emoji = "📐",
                    memberCount = 0
                )
            )
            defaultRooms.forEach { room ->
                roomsRef.child(room.id).setValue(room).await()
            }
        }
    }
}