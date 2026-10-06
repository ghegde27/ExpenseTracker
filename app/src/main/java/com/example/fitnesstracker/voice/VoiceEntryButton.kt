package com.example.fitnesstracker.voice

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.data.local.ExpenseCategory
import com.example.fitnesstracker.data.local.PaymentMethod

@Composable
fun VoiceEntryButton(
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF1E3C72),
    onConfirmExpense: (amount: Long, category: ExpenseCategory, description: String, source: String) -> Unit,
    onConfirmIncome: (clientName: String, amount: Long, method: PaymentMethod, notes: String) -> Unit
) {
    val context = LocalContext.current
    var voiceState by remember { mutableStateOf<VoiceInputState>(VoiceInputState.Idle) }

    val recognizerManager = remember(context) {
        SpeechRecognizerManager(context) { newState ->
            voiceState = newState
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recognizerManager.stopAndDestroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            recognizerManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice commands", Toast.LENGTH_SHORT).show()
        }
    }

    val startVoice = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            recognizerManager.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        // Status indicator when listening/processing/error
        AnimatedVisibility(
            visible = voiceState is VoiceInputState.Listening || voiceState is VoiceInputState.Processing || voiceState is VoiceInputState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (voiceState) {
                    is VoiceInputState.Listening -> Color(0xFFFF3D00)
                    is VoiceInputState.Processing -> Color(0xFF00B0FF)
                    is VoiceInputState.Error -> Color(0xFFC62828)
                    else -> Color.Gray
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (voiceState is VoiceInputState.Listening || voiceState is VoiceInputState.Processing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = when (val state = voiceState) {
                            is VoiceInputState.Listening -> "🔴 Listening..."
                            is VoiceInputState.Processing -> "⏳ Processing..."
                            is VoiceInputState.Error -> state.message.take(25)
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
            }
        }

        // Voice FAB
        FloatingActionButton(
            onClick = { startVoice() },
            containerColor = containerColor,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Entry",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Show Confirmation Dialog when result is ready
    (voiceState as? VoiceInputState.Result)?.let { result ->
        VoiceConfirmationDialog(
            rawText = result.text,
            suggestion = result.suggestion,
            onDismiss = {
                recognizerManager.stopAndDestroy()
                voiceState = VoiceInputState.Idle
            },
            onConfirmExpense = { amount, category, description, source ->
                onConfirmExpense(amount, category, description, source)
                recognizerManager.stopAndDestroy()
                voiceState = VoiceInputState.Idle
            },
            onConfirmIncome = { clientName, amount, method, notes ->
                onConfirmIncome(clientName, amount, method, notes)
                recognizerManager.stopAndDestroy()
                voiceState = VoiceInputState.Idle
            }
        )
    }
}
