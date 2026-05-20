package com.example.fyp_app.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fyp_app.R
import com.example.fyp_app.ui.theme.AgroGreen
import com.example.fyp_app.ui.theme.DarkGreen

data class FAQItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(onNavigateBack: () -> Unit) {
    val faqList = listOf(
        FAQItem(
            "What are the common symptoms of Mango Anthracnose?",
            "Common symptoms include dark, irregular spots on leaves, flowers, and fruits that turn brown and enlarge over time. In humid conditions, pinkish spore masses may appear on infected areas. It can severely reduce fruit quality and yield if not managed."
        ),
        FAQItem(
            "How can I prevent Potato Scab?",
            "To prevent Potato Scab, use scab-resistant varieties, maintain a soil pH below 5.2, and ensure adequate soil moisture during the first 6-8 weeks of tuber development. Avoid using fresh manure in potato fields."
        ),
        FAQItem(
            "What causes Citrus Canker and how is it spread?",
            "Citrus Canker is caused by the bacterium Xanthomonas citri. It spreads through wind-blown rain, overhead irrigation, and contaminated garden tools or clothing. It causes lesions on leaves, stems, and fruit."
        ),
        FAQItem(
            "How do I identify Corn Rust in my field?",
            "Corn rust is characterized by small, reddish-brown pustules that develop on both the upper and lower leaf surfaces. As the plant matures, these pustules can turn black. Severe infections can lead to leaf yellowing and reduced yields."
        ),
        FAQItem(
            "What is Lemon Canker and how can it be managed?",
            "Lemon Canker is caused by the bacterium Xanthomonas citri subsp. citri. It produces raised, corky lesions on leaves, stems, and fruit. Management includes using disease-free planting material, applying copper-based bactericides, and removing and destroying infected plant parts promptly."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & FAQ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DarkGreen,
                    navigationIconContentColor = DarkGreen
                )
            )
        },
        containerColor = Color(0xFFF8FAF8)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.common_diseases_faq),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    stringResource(R.string.faq_subtitle),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(faqList) { faq ->
                FAQCard(faq)
            }
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = faq.question,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E2C)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Show less" else "Show more",
                    tint = AgroGreen
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
