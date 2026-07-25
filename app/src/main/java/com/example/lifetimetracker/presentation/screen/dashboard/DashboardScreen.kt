package com.example.lifetimetracker.presentation.screen.dashboard

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lifetimetracker.domain.model.ActivityLog
import com.example.lifetimetracker.domain.model.Category
import com.example.lifetimetracker.domain.model.UsageAggregate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddActivityClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val currentDate by viewModel.currentDate.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val todayUsageAggregates by viewModel.todayUsageAggregates.collectAsState()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Do nothing on result for now */ }

    LaunchedEffect(Unit) {
        viewModel.checkUsagePermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddActivityClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!hasUsagePermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Usage Access Required", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Grant permission to automatically track phone screen time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onErrorContainer, contentColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text("Open Settings")
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val limitedCategories = categories.filter { it.dailyLimitMinutes != null }
                if (limitedCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Daily Limits",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(limitedCategories) { category ->
                        val limit = category.dailyLimitMinutes!!
                        val manualSum = todayLogs.filter { it.categoryId == category.id }.sumOf { it.durationMinutes }
                        val autoSum = todayUsageAggregates.filter { it.categoryId == category.id }.sumOf { it.foregroundMinutes }
                        val totalMinutes = manualSum + autoSum
                        
                        LimitProgressItem(category = category, totalMinutes = totalMinutes, limit = limit)
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }


                item {
                    Text(
                        text = "Today's Activities",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (todayLogs.isEmpty() && todayUsageAggregates.isEmpty()) {
                    item {
                        Text("No activities logged today yet.")
                    }
                }

                items(todayLogs) { log ->
                    val category = categories.find { it.id == log.categoryId }
                    ActivityLogItem(log = log, category = category)
                }

                if (todayUsageAggregates.isNotEmpty()) {
                    item {
                        Text(
                            text = "Auto Tracked Apps",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    // Group by category to show sum
                    val grouped = todayUsageAggregates.groupBy { it.categoryId }
                    val sortedGroups = grouped.entries.sortedByDescending { entry -> entry.value.sumOf { it.foregroundMinutes } }

                    items(sortedGroups) { (categoryId, usages) ->
                        val category = categories.find { it.id == categoryId }
                        val totalMinutes = usages.sumOf { it.foregroundMinutes }
                        val topApps = usages.sortedByDescending { it.foregroundMinutes }.take(3).mapNotNull { it.appLabel ?: it.packageName }.joinToString(", ")
                        
                        UsageAggregateItem(category = category, totalMinutes = totalMinutes, topApps = topApps)
                    }
                }
            }
        }
    }
}

@Composable
fun LimitProgressItem(category: Category, totalMinutes: Int, limit: Int) {
    val progress = (totalMinutes.toFloat() / limit).coerceIn(0f, 1f)
    
    val progressColor = when {
        progress >= 1f -> MaterialTheme.colorScheme.error
        progress >= 0.8f -> Color(0xFFFFA500) // Orange
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(12.dp).background(
                            color = Color(android.graphics.Color.parseColor(category.colorHex)),
                            shape = MaterialTheme.shapes.small
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "$totalMinutes / $limit min", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun ActivityLogItem(log: ActivityLog, category: Category?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(16.dp).background(
                    color = category?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Gray,
                    shape = MaterialTheme.shapes.small
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category?.name ?: "Unknown", style = MaterialTheme.typography.bodyLarge)
                if (!log.note.isNullOrBlank()) {
                    Text(text = log.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(text = "${log.durationMinutes} min", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun UsageAggregateItem(category: Category?, totalMinutes: Int, topApps: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(16.dp).background(
                    color = category?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Gray,
                    shape = MaterialTheme.shapes.small
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category?.name ?: "Unknown Apps", style = MaterialTheme.typography.bodyLarge)
                Text(text = topApps, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "$totalMinutes min", style = MaterialTheme.typography.titleMedium)
        }
    }
}
