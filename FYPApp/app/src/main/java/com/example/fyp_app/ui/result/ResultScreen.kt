package com.example.fyp_app.ui.result

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.example.fyp_app.R
import com.example.fyp_app.ui.theme.AgroGreen
import com.example.fyp_app.viewmodels.DiagnosisViewModel
import com.example.fyp_app.viewmodels.UiState

val OffWhite = Color(0xFFF9FBF9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: DiagnosisViewModel,
    imageUri: String,
    cropType: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState

    // Trigger the diagnosis as soon as the screen opens
    LaunchedEffect(Unit) {
        val decodedUriString = Uri.decode(imageUri)
        val uri = decodedUriString.toUri()
        viewModel.diagnoseImage(context, uri, cropType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diagnosis_result), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = OffWhite
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(0.dp))

            // IMAGE
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = Uri.decode(imageUri).toUri(),
                    contentDescription = "Scanned Image",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }

            // CONTENT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (uiState) {
                    is UiState.Loading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AgroGreen, strokeWidth = 4.dp)
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.analyzing_image), color = Color.Gray)
                        }
                    }

                    is UiState.Success -> {
                        val displayCropName = when (cropType.lowercase()) {
                            "mango" -> stringResource(R.string.crop_tomato)
                            "orange" -> stringResource(R.string.crop_orange)
                            "potato" -> stringResource(R.string.crop_potato)
                            "corn" -> stringResource(R.string.crop_corn)
                            "lemon" -> stringResource(R.string.crop_rice)
                            "apple" -> stringResource(R.string.crop_apple)
                            else -> cropType
                        }
                        ResultCard(
                            title = uiState.translatedPrediction ?: uiState.data.prediction?.replace("_", " ") ?: "Unknown",
                            confidence = uiState.data.confidence ?: 0.0,
                            cropType = displayCropName,
                            treatmentFromApi = uiState.treatment,
                            isHealthy = uiState.data.prediction?.contains("healthy", ignoreCase = true) == true
                        )
                    }

                    is UiState.Error -> {
                        val errorMessage = if (uiState.message.contains("400") || 
                            uiState.message.contains("failed", ignoreCase = true) ||
                            uiState.message.contains("clear", ignoreCase = true)) {
                            stringResource(R.string.upload_clear_image)
                        } else {
                            uiState.message
                        }
                        InfoCard(
                            title = stringResource(R.string.analysis_failed),
                            message = errorMessage,
                            containerColor = Color(0xFFFFF0F0),
                            contentColor = Color.Red
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ResultCard(title: String, confidence: Double, cropType: String, treatmentFromApi: String? = null, isHealthy: Boolean = false) {
    val treatment = treatmentFromApi ?: if (isHealthy) {
        stringResource(R.string.healthy_leaf)
    } else {
        ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = AgroGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = cropType.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = AgroGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.treatment_advice),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AgroGreen,
                letterSpacing = 0.5.sp
            )

            if (treatmentFromApi == null && !isHealthy) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AgroGreen,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = stringResource(R.string.fetching_advice),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                Text(
                    text = formatTreatmentText(treatment),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.confidence_score), color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.1f%%", confidence * 100),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (confidence > 0.7) AgroGreen else Color.DarkGray,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun formatTreatmentText(text: String): AnnotatedString {
    val cleanText = text.replace("**", "").replace("#", "")
    val headings = listOf(
        "CHEMICAL SUGGESTIONS:",
        "FERTILIZER RECOMMENDATIONS:",
        "ACTION PLAN:",
        "کیمیائی تجاویز:",
        "کھاد کی سفارشات:",
        "عملی منصوبہ:",
        "Chemical:",
        "Action:",
        "Fertilizer:"
    )

    return buildAnnotatedString {
        val sortedHeadings = headings.filter { cleanText.contains(it, ignoreCase = true) }
            .sortedBy { cleanText.indexOf(it, ignoreCase = true) }

        var lastIndex = 0
        sortedHeadings.forEach { heading ->
            val index = cleanText.indexOf(heading, lastIndex, ignoreCase = true)
            if (index != -1) {
                append(cleanText.substring(lastIndex, index))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                    append(cleanText.substring(index, index + heading.length))
                }
                lastIndex = index + heading.length
            }
        }
        append(cleanText.substring(lastIndex))
    }
}

@Composable
fun InfoCard(title: String, message: String, containerColor: Color, contentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, color = contentColor, fontSize = 18.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                color = Color.DarkGray.copy(alpha = 0.8f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}


