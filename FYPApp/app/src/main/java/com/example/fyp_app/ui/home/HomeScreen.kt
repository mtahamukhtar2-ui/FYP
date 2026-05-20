package com.example.fyp_app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fyp_app.R
import com.example.fyp_app.ui.theme.AgroGreen
import com.example.fyp_app.ui.theme.DarkGreen
import com.example.fyp_app.utils.LanguageManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.*

val OffWhite = Color(0xFFF8FAF8)

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    userName: String,
    isNewUser: Boolean,
    onNavigateToDiagnose: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToNews: () -> Unit,
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context.applicationContext ?: context) }

    val fetchLocationAndWeather = {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    weatherViewModel.fetchWeather(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        lastLoc?.let { weatherViewModel.fetchWeather(it.latitude, it.longitude) }
                    }
                }
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchLocationAndWeather()
        }
    }

    LaunchedEffect(Unit) {
        val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndWeather()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Scaffold(
        containerColor = OffWhite
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // --- Background Decorations ---
            Image(
                painter = painterResource(id = R.drawable.top_leaves),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp),
                contentScale = ContentScale.Fit,
                alpha = 0.6f
            )

            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = 80.dp),
                tint = AgroGreen.copy(alpha = 0.12f)
            )

            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (-40).dp, y = 40.dp),
                tint = AgroGreen.copy(alpha = 0.08f)
            )
            
            Icon(
                imageVector = Icons.Default.Grass,
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 10.dp),
                tint = AgroGreen.copy(alpha = 0.08f)
            )

            // --- Main Content ---
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Section
                Box(modifier = Modifier.fillMaxWidth().height(135.dp)) {
                    AsyncImage(
                        model = "https://img.freepik.com/free-photo/vibrant-green-field-with-scenic-landscape_23-2148842600.jpg",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.8f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, OffWhite.copy(alpha = 0.5f), OffWhite),
                                    startY = 135f
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 0.dp)
                    ) {
                        val calendar = Calendar.getInstance()
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val greeting = when (hour) {
                            in 0..11 -> stringResource(R.string.good_morning)
                            in 12..16 -> stringResource(R.string.good_afternoon)
                            else -> stringResource(R.string.good_evening)
                        }

                        Text(text = "$greeting $userName", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(R.string.welcome_back_text))
                                withStyle(style = SpanStyle(fontSize = 20.sp)) { append("👋") }
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        Text(
                            text = stringResource(R.string.home_subtitle),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Weather Card
                WeatherCard(weatherViewModel)
                
                Spacer(modifier = Modifier.height(10.dp))

                // Quick Actions Section
                QuickActionsSection(
                    onNavigateToDiagnose,
                    onNavigateToAssistant,
                    onNavigateToNews,
                    onChangeLanguage = { showLanguageDialog = true }
                )

                Spacer(modifier = Modifier.weight(1f))
                
                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Banners
                CropHealthOverviewBanner(onClick = onNavigateToDiagnose)
                
                // Remove extra space at the very end
            }

            if (showLanguageDialog) {
                LanguageSelectionDialog(onDismiss = { showLanguageDialog = false })
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                stringResource(R.string.change_language), 
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            ) 
        },
        text = {
            Column {
                LanguageDialogOption(stringResource(R.string.english), "en", context)
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                LanguageDialogOption(stringResource(R.string.urdu), "ur", context)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel), color = AgroGreen)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun LanguageDialogOption(title: String, langCode: String, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                LanguageManager.saveLanguage(context, langCode)
                LanguageManager.setLocale(context, langCode)
                LanguageManager.restartActivity(context as android.app.Activity)
            }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AgroGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Translate, null, tint = AgroGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            title, 
            fontSize = 16.sp, 
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C3E2C)
        )
    }
}

@Composable
fun WeatherCard(vm: WeatherViewModel) {
    val (weatherIcon, iconColor) = when {
        vm.condition.contains("Cloud", ignoreCase = true) -> Icons.Default.Cloud to Color.White
        vm.condition.contains("Rain", ignoreCase = true) ||
                vm.condition.contains("Drizzle", ignoreCase = true) ||
                vm.condition.contains("Thunderstorm", ignoreCase = true) -> Icons.Default.Thunderstorm to Color(0xFF90CAF9)
        else -> Icons.Default.WbSunny to Color(0xFFFFD600)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.1f)) {
                Icon(weatherIcon, null, tint = iconColor, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(vm.temp, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("  |  ", color = Color.White.copy(0.5f), fontSize = 18.sp)
                    Text(vm.condition, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(12.dp))
                    Text(text = " " + stringResource(R.string.location_label, vm.city), color = Color.White.copy(0.7f), fontSize = 12.sp)
                }
            }
            VerticalDivider(modifier = Modifier.height(70.dp).width(1.dp), color = Color.White.copy(0.2f))
            Column(modifier = Modifier.weight(0.9f).padding(start = 20.dp), verticalArrangement = Arrangement.Center) {
                WeatherDetailItem(Icons.Default.WaterDrop, vm.humidity, stringResource(R.string.humidity))
                Spacer(modifier = Modifier.height(6.dp))
                WeatherDetailItem(Icons.Default.Air, vm.windSpeed, stringResource(R.string.wind))
            }
        }
    }
}

@Composable
fun WeatherDetailItem(icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(0.85f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun QuickActionsSection(
    onNavigateToDiagnose: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToNews: () -> Unit,
    onChangeLanguage: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ElectricBolt, null, tint = AgroGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.quick_actions), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E2C))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(
                title = stringResource(R.string.scan_crop),
                subtitle = stringResource(R.string.detect_disease),
                icon = Icons.Outlined.CameraAlt,
                iconColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToDiagnose
            )
            ActionCard(
                title = stringResource(R.string.agrobot),
                subtitle = stringResource(R.string.ai_assistant),
                icon = Icons.Outlined.ChatBubbleOutline,
                iconColor = Color(0xFF1976D2),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAssistant
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(
                title = stringResource(R.string.agro_news),
                subtitle = stringResource(R.string.latest_updates),
                icon = Icons.Outlined.Newspaper,
                iconColor = Color(0xFFE64A19),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToNews
            )
            ActionCard(
                title = stringResource(R.string.change_language),
                subtitle = stringResource(R.string.language_subtitle),
                icon = Icons.Outlined.Language,
                iconColor = Color(0xFF1976D2),
                modifier = Modifier.weight(1f),
                onClick = onChangeLanguage
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column {
                Box(modifier = Modifier.size(32.dp).background(iconColor.copy(0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(subtitle, fontSize = 10.sp, color = Color.Gray, lineHeight = 12.sp)
            }
            Box(modifier = Modifier.size(24.dp).background(Color(0xFFF1F8E9), CircleShape).align(Alignment.BottomEnd), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = iconColor, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun CropHealthOverviewBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEDC8)), // More prominent light green
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, AgroGreen.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Eco, null, tint = AgroGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.crop_health_overview), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(stringResource(R.string.check_health_status), fontSize = 11.sp, color = Color.Gray)
                Text(stringResource(R.string.check_now), fontSize = 12.sp, color = AgroGreen, fontWeight = FontWeight.Bold)
            }
            Image(painter = painterResource(R.drawable.plant_pot), contentDescription = null, modifier = Modifier.size(85.dp), contentScale = ContentScale.Fit)
        }
    }
}


