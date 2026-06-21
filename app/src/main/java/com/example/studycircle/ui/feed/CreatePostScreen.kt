package com.example.studycircle.ui.feed

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studycircle.domain.model.Post
import com.example.studycircle.ui.auth.AuthViewModel
import com.example.studycircle.ui.theme.GradientEnd
import com.example.studycircle.ui.theme.GradientStart
import com.example.studycircle.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit = {},
    onBack: () -> Unit = {},
    feedViewModel: FeedViewModel = viewModel()
) {
    var content by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }
    var contentError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val subjects = listOf("Math", "Physics", "Chemistry", "CS", "OS", "DBMS", "DSA", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Post",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Post button in top bar
                    IconButton(
                        onClick = {
                            var hasError = false
                            if (selectedSubject.isEmpty()) {
                                subjectError = true
                                hasError = true
                            }
                            if (content.isBlank()) {
                                contentError = true
                                hasError = true
                            }
                            if (!hasError) {
                                isPosting = true
                                val post = Post(
                                    authorId = currentUser?.uid ?: "",
                                    authorName = currentUser?.displayName
                                        ?: currentUser?.email?.substringBefore("@")
                                        ?: "Anonymous",
                                    subject = selectedSubject,
                                    content = content.trim(),
                                    timestamp = System.currentTimeMillis()
                                )
                                feedViewModel.createPost(
                                    post = post,
                                    onSuccess = {
                                        isPosting = false
                                        Toast.makeText(context, "Post shared! 🎉", Toast.LENGTH_SHORT).show()
                                        onPostCreated()
                                    },
                                    onError = {
                                        isPosting = false
                                        Toast.makeText(context, "Failed to post. Try again.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        enabled = !isPosting
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Post",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Subject selection label
            Text(
                text = "Select Subject",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (subjectError) {
                Text(
                    text = "Please select a subject",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject chips grid
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                subjects.forEach { subject ->
                    val isSelected = subject == selectedSubject
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedSubject = subject
                            subjectError = false
                        },
                        label = {
                            Text(
                                text = subject,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
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

            Spacer(modifier = Modifier.height(24.dp))

            // Content input
            Text(
                text = "What's on your mind?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    contentError = false
                },
                placeholder = {
                    Text(
                        text = "Ask a question, share notes, or start a study discussion...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                isError = contentError,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (contentError) {
                            Text(
                                text = "Please write something",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            text = "${content.length}/500",
                            color = if (content.length > 450) MaterialTheme.colorScheme.error else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tips card
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 Tips for a great post",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TipItem("Be specific about what you need help with")
                        TipItem("Mention your college/university if relevant")
                        TipItem("Share resources if you have them")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Post button at bottom
            Button(
                onClick = {
                    var hasError = false
                    if (selectedSubject.isEmpty()) {
                        subjectError = true
                        hasError = true
                    }
                    if (content.isBlank()) {
                        contentError = true
                        hasError = true
                    }
                    if (!hasError) {
                        isPosting = true
                        val post = Post(
                            authorId = currentUser?.uid ?: "",
                            authorName = currentUser?.displayName
                                ?: currentUser?.email?.substringBefore("@")
                                ?: "Anonymous",
                            subject = selectedSubject,
                            content = content.trim(),
                            timestamp = System.currentTimeMillis()
                        )
                        feedViewModel.createPost(
                            post = post,
                            onSuccess = {
                                isPosting = false
                                Toast.makeText(context, "Post shared! 🎉", Toast.LENGTH_SHORT).show()
                                onPostCreated()
                            },
                            onError = {
                                isPosting = false
                                Toast.makeText(context, "Failed to post. Try again.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                enabled = !isPosting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Post",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "•", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 6.dp))
        Text(text = text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}