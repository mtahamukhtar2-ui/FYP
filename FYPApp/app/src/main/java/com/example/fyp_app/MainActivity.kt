package com.example.fyp_app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.fyp_app.ui.assistant.AssistantScreen
import com.example.fyp_app.ui.auth.LoginScreen
import com.example.fyp_app.ui.auth.SignUpScreen
import com.example.fyp_app.ui.camera.CameraScreen
import com.example.fyp_app.ui.diagnose.DiagnoseScreen
import com.example.fyp_app.ui.home.HomeScreen
import com.example.fyp_app.ui.news.NewsScreen
import com.example.fyp_app.ui.profile.EditProfileScreen
import com.example.fyp_app.ui.profile.FAQScreen
import com.example.fyp_app.ui.profile.ProfileScreen
import com.example.fyp_app.ui.result.ResultScreen
import com.example.fyp_app.ui.theme.AgroGreen
import com.example.fyp_app.ui.theme.DarkGreen
import com.example.fyp_app.ui.theme.FYPAppTheme
import com.example.fyp_app.utils.ImageProcessor
import com.example.fyp_app.utils.LanguageManager
import com.example.fyp_app.viewmodels.DiagnosisViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    private val diagnosisViewModel: DiagnosisViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val language = LanguageManager.getSavedLanguage(newBase)
        val context = LanguageManager.setLocale(newBase, language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FYPAppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    AppNavigation(viewModel = diagnosisViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: DiagnosisViewModel) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // --- Google Sign-In setup ---
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1093809165808-7p5ab0ec0fs7evkebnc22aqldpv8l50q.apps.googleusercontent.com") // ← replace with your Web Client ID
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            // Save to Firestore only if it's a new user
                            val docRef = db.collection("users").document(user.uid)
                            docRef.get().addOnSuccessListener { doc ->
                                if (!doc.exists()) {
                                    docRef.set(
                                        hashMapOf(
                                            "fullName" to (user.displayName ?: ""),
                                            "email" to (user.email ?: "")
                                        )
                                    )
                                }
                                navController.navigate("home_screen") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Personalization states ---
    var userName by remember { mutableStateOf("User") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isNewUser by remember { mutableStateOf(false) }

    DisposableEffect(auth.currentUser) {
        val user = auth.currentUser
        if (user != null) {
            isNewUser = user.metadata?.let {
                it.creationTimestamp == it.lastSignInTimestamp
            } ?: true

            val docRef = db.collection("users").document(user.uid)
            val registration = docRef.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val nameFromDb = snapshot.getString("fullName")
                    userName = if (!nameFromDb.isNullOrEmpty()) nameFromDb
                    else user.displayName ?: "User"

                    val imageUrl = snapshot.getString("profileImageUrl")
                    val remoteUri = if (!imageUrl.isNullOrEmpty()) imageUrl.toUri() else null
                    
                    // Only update if remoteUri is not null, or if we don't have a local "content" URI
                    // This prevents the local selected image from being cleared before upload finishes
                    if (remoteUri != null || profileImageUri?.toString()?.startsWith("content") != true) {
                        profileImageUri = remoteUri
                    }
                } else {
                    userName = user.displayName ?: "User"
                    profileImageUri = null
                }
            }
            onDispose { registration.remove() }
        } else {
            userName = "User"
            profileImageUri = null
            onDispose { }
        }
    }

    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val navigationRoutes = listOf("home_screen", "diagnose_screen", "assistant_screen", "profile_screen")
    val startDestination = if (auth.currentUser != null) "home_screen" else "login_screen"

    navController.addOnDestinationChangedListener { _, destination, _ ->
        val index = navigationRoutes.indexOf(destination.route)
        if (index >= 0) selectedItemIndex = index
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val shouldShowBottomBar =
                currentRoute !in listOf("login_screen", "signup_screen", "assistant_screen") &&
                        currentRoute?.startsWith("result_screen") != true

            if (shouldShowBottomBar) {
                BottomNavBar(
                    selectedIndex = selectedItemIndex,
                    onItemSelected = { index ->
                        navController.navigate(navigationRoutes[index]) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login_screen") {
                LoginScreen(
                    onLoginClick = { email, password ->
                        auth.signInWithEmailAndPassword(email.trim(), password.trim())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("home_screen") {
                                        popUpTo("login_screen") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                    },
                    onSignUpClick = { navController.navigate("signup_screen") },
                    onGoogleSignInClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                )
            }

            composable("signup_screen") {
                SignUpScreen(
                    onSignUpClick = { name, email, password ->
                        auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        db.collection("users").document(userId)
                                            .set(hashMapOf("fullName" to name, "email" to email))
                                            .addOnSuccessListener {
                                                navController.navigate("home_screen") {
                                                    popUpTo("signup_screen") { inclusive = true }
                                                }
                                            }
                                    }
                                }
                            }
                    },
                    onLoginClick = { navController.popBackStack() },
                    onGoogleSignInClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                )
            }

            composable("home_screen") {
                HomeScreen(
                    userName = userName,
                    isNewUser = isNewUser,
                    onNavigateToDiagnose = { navController.navigate("diagnose_screen") },
                    onNavigateToAssistant = { navController.navigate("assistant_screen") },
                    onNavigateToNews = { navController.navigate("news_screen") }
                )
            }

            composable("news_screen") {
                NewsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("diagnose_screen") {
                DiagnoseScreen(
                    onImageSelected = { uri, cropType ->
                        val encodedUri = URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.name())
                        navController.navigate("result_screen/$encodedUri/$cropType")
                    },
                    onNavigateToCamera = { cropType ->
                        navController.navigate("camera_screen/$cropType")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "camera_screen/{cropType}",
                arguments = listOf(navArgument("cropType") { type = NavType.StringType })
            ) { backStackEntry ->
                val cropType = backStackEntry.arguments?.getString("cropType") ?: ""
                val scope = rememberCoroutineScope()
                var isProcessing by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize()) {
                    CameraScreen(
                        cropType = cropType,
                        onImageCaptured = { uri ->
                            scope.launch {
                                isProcessing = true
                                try {
                                    val processedUri = ImageProcessor.processLeafImage(context, uri)
                                    val finalUri = processedUri ?: uri
                                    val encodedUri = URLEncoder.encode(
                                        finalUri.toString(),
                                        StandardCharsets.UTF_8.name()
                                    )
                                    navController.navigate("result_screen/$encodedUri/$cropType") {
                                        popUpTo("camera_screen/$cropType") { inclusive = true }
                                    }
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )

                    if (isProcessing) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AgroGreen)
                        }
                    }
                }
            }

            composable("assistant_screen") {
                AssistantScreen(
                    userName = userName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("profile_screen") {
                val userEmail = auth.currentUser?.email ?: "No Email"
                ProfileScreen(
                    name = userName,
                    email = userEmail,
                    profileImageUri = profileImageUri,
                    onEditProfile = { navController.navigate("edit_profile_screen") },
                    onNavigateToFAQ = { navController.navigate("faq_screen") },
                    onLogout = {
                        auth.signOut()
                        googleSignInClient.signOut()
                        navController.navigate("login_screen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("faq_screen") {
                FAQScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("edit_profile_screen") {
                EditProfileScreen(
                    currentName = userName,
                    currentImageUri = profileImageUri,
                    onImageSelected = { uri -> profileImageUri = uri },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "result_screen/{imageUri}/{cropType}",
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType },
                    navArgument("cropType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                val cropType = backStackEntry.arguments?.getString("cropType") ?: ""
                ResultScreen(
                    viewModel = viewModel,
                    imageUri = imageUri,
                    cropType = cropType,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf(
        stringResource(R.string.home),
        stringResource(R.string.diagnose),
        stringResource(R.string.assistant),
        stringResource(R.string.profile)
    )
    val selectedIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.CropFree,
        Icons.Filled.Chat,
        Icons.Filled.Person
    )
    val unselectedIcons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.CropFree,
        Icons.Outlined.Chat,
        Icons.Outlined.Person
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                label = { Text(text = item, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                icon = {
                    Icon(
                        imageVector = if (selectedIndex == index) selectedIcons[index]
                        else unselectedIcons[index],
                        contentDescription = item,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkGreen,
                    selectedTextColor = DarkGreen,
                    indicatorColor = Color(0xFFE8F5E9),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}