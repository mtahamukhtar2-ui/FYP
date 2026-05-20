package com.example.fyp_app.ui.profile

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fyp_app.ui.theme.AgroGreen
import com.example.fyp_app.ui.theme.DarkGreen
import com.example.fyp_app.utils.ImageProcessor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    currentImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(currentImageUri) }
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            onImageSelected(uri)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DarkGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkGreen)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAF8)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color(0xFFA5D6A7),
                    border = BorderStroke(2.dp, AgroGreen)
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (name.isNotEmpty()) name.take(1).uppercase() else "",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = AgroGreen,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgroGreen,
                    focusedLabelColor = AgroGreen
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isSaving = true
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        scope.launch {
                            try {
                                val updates = mutableMapOf<String, Any>(
                                    "fullName" to name.trim()
                                )
                                
                                // Upload to Firebase Storage if image was changed
                                if (selectedImageUri != null && selectedImageUri != currentImageUri) {
                                    val uriString = selectedImageUri.toString()
                                    if (!uriString.startsWith("http")) {
                                        val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
                                        
                                        // Use InputStream for more reliable upload from content URIs
                                        val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                                        if (inputStream != null) {
                                            Log.d("EditProfile", "Starting upload for user: $userId")
                                            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                                                .setContentType("image/jpeg")
                                                .build()
                                            
                                            storageRef.putStream(inputStream, metadata).await()
                                            inputStream.close()
                                            
                                            Log.d("EditProfile", "Upload successful, fetching download URL")
                                            // Get the download URL after upload completes
                                            val downloadUrl = storageRef.downloadUrl.await()
                                            Log.d("EditProfile", "Got download URL: $downloadUrl")
                                            updates["profileImageUrl"] = downloadUrl.toString()
                                        } else {
                                            throw Exception("Could not open image file from device")
                                        }
                                    }
                                }

                                db.collection("users").document(userId)
                                    .set(updates, SetOptions.merge())
                                    .await()

                                isSaving = false
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            } catch (e: Exception) {
                                isSaving = false
                                Toast.makeText(context, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
