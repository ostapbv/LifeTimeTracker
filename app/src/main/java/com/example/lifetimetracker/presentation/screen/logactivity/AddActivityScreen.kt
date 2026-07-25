package com.example.lifetimetracker.presentation.screen.logactivity

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lifetimetracker.domain.model.Category
import com.example.lifetimetracker.presentation.system.VoiceRecognizerHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddActivityViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val durationMinutes by viewModel.durationMinutes.collectAsState()
    val note by viewModel.note.collectAsState()
    val isVoiceMode by viewModel.isVoiceMode.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val voiceHelper = remember { VoiceRecognizerHelper(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            isListening = true
            viewModel.setVoiceMode(true)
            voiceError = null
            voiceHelper.startListening(
                onResult = { text ->
                    isListening = false
                    viewModel.processVoiceInput(text)
                },
                onError = { error ->
                    isListening = false
                    voiceError = error
                },
                onPartialResult = { partial ->
                    viewModel.onNoteChanged(partial)
                }
            )
        } else {
            voiceError = "Microphone permission required"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Activity") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        if (isListening) {
                            voiceHelper.stopListening()
                            isListening = false
                        } else {
                            isListening = true
                            viewModel.setVoiceMode(true)
                            voiceError = null
                            viewModel.onNoteChanged("")
                            voiceHelper.startListening(
                                onResult = { text ->
                                    isListening = false
                                    viewModel.processVoiceInput(text)
                                },
                                onError = { error ->
                                    isListening = false
                                    voiceError = error
                                },
                                onPartialResult = { partial ->
                                    viewModel.onNoteChanged(partial)
                                }
                            )
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                containerColor = if (isListening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Input")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isListening) {
                Text(
                    text = "Listening...",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (voiceError != null) {
                Text(
                    text = voiceError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Select Category",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.onCategorySelected(category)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = durationMinutes,
                onValueChange = {
                    viewModel.setVoiceMode(false)
                    viewModel.onDurationChanged(it)
                },
                label = { Text("Duration (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = {
                    viewModel.setVoiceMode(false)
                    viewModel.onNoteChanged(it)
                },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveActivity(onSuccess = onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCategory != null && durationMinutes.isNotBlank() && !isListening
            ) {
                Text("Save Activity")
            }
        }
    }
}
