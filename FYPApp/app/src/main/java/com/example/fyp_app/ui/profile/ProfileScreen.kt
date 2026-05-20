package com.example.fyp_app.ui.profile

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fyp_app.R
import com.example.fyp_app.ui.theme.AgroGreen
import com.example.fyp_app.ui.theme.DarkGreen
import com.example.fyp_app.utils.LanguageManager

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    profileImageUri: Uri? = null,
    onEditProfile: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onLogout: () -> Unit
) {
    var showLanguageOptions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val OffWhite = Color(0xFFF8FAF8)

    Box(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        // --- Background Decorations ---
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp),
            tint = AgroGreen.copy(alpha = 0.05f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(45.dp))
            
            // Custom Header
            Text(
                text = stringResource(R.string.my_profile),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = stringResource(R.string.manage_profile_subtitle),
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Slightly smaller Profile Header
            ProfileHeader(name = name, email = email, profileImageUri = profileImageUri, onEditClick = onEditProfile)

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(icon = Icons.Default.Eco, title = stringResource(R.string.account_settings))
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    ProfileActionTile(
                        icon = Icons.Outlined.Person,
                        title = stringResource(R.string.edit_profile),
                        subtitle = stringResource(R.string.edit_profile_subtitle),
                        iconColor = AgroGreen
                    ) { onEditProfile() }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                    
                    ProfileActionTile(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.change_language),
                        subtitle = stringResource(R.string.language_subtitle),
                        iconColor = Color(0xFF1976D2)
                    ) {
                        showLanguageOptions = !showLanguageOptions
                    }

                    if (showLanguageOptions) {
                        Column(modifier = Modifier.padding(bottom = 6.dp)) {
                            LanguageOptionTile(stringResource(R.string.english), "en", context)
                            LanguageOptionTile(stringResource(R.string.urdu), "ur", context)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(icon = Icons.Default.HeadsetMic, title = stringResource(R.string.support))
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    ProfileActionTile(
                        icon = Icons.Outlined.HelpOutline,
                        title = stringResource(R.string.help_faq),
                        subtitle = stringResource(R.string.faq_subtitle_profile),
                        iconColor = Color(0xFF388E3C)
                    ) { onNavigateToFAQ() }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                    
                    ProfileActionTile(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        title = stringResource(R.string.sign_out),
                        subtitle = stringResource(R.string.signout_subtitle),
                        iconColor = Color.Red,
                        isDanger = true
                    ) { onLogout() }
                }
            }
            
            // This spacer ensures there's a gap at the bottom
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(AgroGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AgroGreen, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
    }
}

@Composable
fun ProfileHeader(name: String, email: String, profileImageUri: Uri?, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFA5D6A7)),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (name.isNotEmpty()) name.take(1).uppercase() else "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifEmpty { "User Name" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E2C)
                )
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF1F8E9), CircleShape)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = AgroGreen)
            }
        }
    }
}

@Composable
fun ProfileActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDanger) Color.Red else Color(0xFF2C3E2C)
            )
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun LanguageOptionTile(title: String, langCode: String, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                LanguageManager.saveLanguage(context, langCode)
                LanguageManager.setLocale(context, langCode)
                LanguageManager.restartActivity(context as Activity)
            }
            .padding(vertical = 8.dp, horizontal = 70.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Translate, null, tint = AgroGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontSize = 13.sp, color = Color.DarkGray)
    }
}
