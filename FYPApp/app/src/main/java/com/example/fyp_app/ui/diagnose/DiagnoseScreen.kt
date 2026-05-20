package com.example.fyp_app.ui.diagnose

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fyp_app.R
import com.example.fyp_app.ui.theme.AgroGreen
import com.example.fyp_app.utils.ImageProcessor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnoseScreen(
    onImageSelected: (Uri, String) -> Unit,
    onNavigateToCamera: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedCrop by remember { mutableStateOf<String?>(null) }
    val crops = listOf(
        "Mango" to stringResource(R.string.crop_tomato),
        "Orange" to stringResource(R.string.crop_orange),
        "Potato" to stringResource(R.string.crop_potato),
        "Corn" to stringResource(R.string.crop_corn),
        "Lemon" to stringResource(R.string.crop_rice),
        "Apple" to stringResource(R.string.crop_apple)
    )

    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    // --- GALLERY LOGIC ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && selectedCrop != null) {
            scope.launch {
                isProcessing = true
                try {
                    val processedUri = ImageProcessor.processLeafImage(context, uri)
                    onImageSelected(processedUri ?: uri, selectedCrop!!)
                } catch (_: Exception) {
                    Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            selectedCrop?.let { onNavigateToCamera(it) }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FBF9),
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // --- Header Section ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.diagnose_crop),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1B)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("🌿", fontSize = 22.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.diagnose_subtitle),
                                fontSize = 13.sp,
                                color = Color.Gray,
                                lineHeight = 18.sp
                            )
                        }
                        // Decorative element (Plant illustration placeholder)
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AgroGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌱", fontSize = 32.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- Select Your Crop Section ---
                    SectionHeader(icon = Icons.Default.Spa, title = stringResource(R.string.select_your_crop))

                    Spacer(Modifier.height(12.dp))

                    // Manual grid for crops
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val rows = crops.chunked(2)
                        rows.forEach { rowCrops ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowCrops.forEach { (id, name) ->
                                    CropCard(
                                        name = name,
                                        isSelected = selectedCrop == id,
                                        modifier = Modifier.weight(1f),
                                        onSelect = { selectedCrop = id }
                                    )
                                }
                                if (rowCrops.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Column {
                    Spacer(Modifier.height(32.dp))
                    // --- Upload Image Section ---
                    SectionHeader(icon = Icons.Default.PhotoCamera, title = stringResource(R.string.upload_image))

                    Spacer(Modifier.height(8.dp))

                    Text(
                        stringResource(R.string.upload_subtitle),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionButton(
                            title = stringResource(R.string.gallery),
                            subtitle = stringResource(R.string.pick_from_storage),
                            icon = Icons.Default.PhotoLibrary,
                            containerColor = Color.White,
                            contentColor = AgroGreen,
                            borderColor = AgroGreen.copy(alpha = 0.3f),
                            enabled = selectedCrop != null,
                            modifier = Modifier.weight(1f),
                            onClick = { galleryLauncher.launch("image/*") }
                        )
                        ActionButton(
                            title = stringResource(R.string.capture),
                            subtitle = stringResource(R.string.use_your_camera),
                            icon = Icons.Default.CameraAlt,
                            containerColor = AgroGreen,
                            contentColor = Color.White,
                            borderColor = Color.Transparent,
                            enabled = selectedCrop != null,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    selectedCrop?.let { onNavigateToCamera(it) }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        )
                    }
                }
            }

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AgroGreen)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AgroGreen, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF333333))
    }
}

@Composable
fun CropCard(
    name: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier
            .height(85.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        border = if (isSelected) BorderStroke(2.dp, AgroGreen) else BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9FBF9)),
                contentAlignment = Alignment.Center
            ) {
                Text(getEmojiForCrop(name), fontSize = 32.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1B1B1B)
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AgroGreen else Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) Color.White else Color.Gray
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(85.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) containerColor else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled && containerColor != Color.White) 4.dp else 1.dp
        ),
        border = if (enabled) BorderStroke(1.dp, borderColor) else BorderStroke(1.dp, Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (enabled) contentColor.copy(alpha = 0.12f)
                        else Color.Gray.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = if (enabled) contentColor else Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (enabled) {
                        if (containerColor == Color.White) Color(0xFF1B1B1B) else Color.White
                    } else Color.LightGray,
                    lineHeight = 18.sp
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = if (enabled) {
                        if (containerColor == Color.White) Color.Gray else Color.White.copy(alpha = 0.8f)
                    } else Color.LightGray.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

fun getEmojiForCrop(name: String): String {
    return when {
        name.contains("Mango", ignoreCase = true) || name.contains("آم") -> "🥭"
        name.contains("Orange", ignoreCase = true) || name.contains("مالٹا") -> "🍊"
        name.contains("Potato", ignoreCase = true) || name.contains("آلو") -> "🥔"
        name.contains("Corn", ignoreCase = true) || name.contains("مکئی") -> "🌽"
        name.contains("Lemon", ignoreCase = true) || name.contains("لیموں") -> "🍋"
        name.contains("Apple", ignoreCase = true) || name.contains("سیب") -> "🍎"
        else -> "🌿"
    }
}
