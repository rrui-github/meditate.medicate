package com.example.medicate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicate.ui.theme.MedicateTheme
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedicateTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black // Pure black background for AMOLED
                ) { innerPadding ->
                    TimerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TimerScreen(modifier: Modifier = Modifier) {
    // 1. The State: Time starts at 0 and we remember if it's running
    val timeSpent = remember { mutableStateOf(0L) }
    val isRunning = remember { mutableStateOf(false) }

    // 2. The Heartbeat: Counts forward (adds 1 second)
    LaunchedEffect(isRunning.value) {
        while (isRunning.value) {
            delay(1000L)
            timeSpent.value += 1
        }
    }

    // 3. The View: AMOLED Inverted (White on Black)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 80.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // The Timer Circle (White border)
        Box(
            modifier = Modifier
                .size(280.dp)
                .border(BorderStroke(1.dp, Color.White), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(timeSpent.value),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 72.sp
                    ),
                    color = Color.White // White text
                )
                Text(
                    text = "session time",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White // White text
                )
            }
        }

        // The Buttons Row
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MedicateButton("pause") { isRunning.value = false }
                MedicateButton("start") { isRunning.value = true }
                MedicateButton("stop") { 
                    isRunning.value = false
                    timeSpent.value = 0 
                }
            }

            Spacer(modifier = Modifier.height(100.dp))

            // Logs Button
            MedicateButton("logs") {
                // Future feature: view meditation history
            }
        }
    }
}

@Composable
fun MedicateButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50), 
        border = BorderStroke(1.5.dp, Color.White), // White border
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), // White text
        modifier = Modifier.size(width = 110.dp, height = 55.dp)
    ) {
        Text(
            text = text, 
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

// Helper to format seconds into 00:00
fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun TimerPreview() {
    MedicateTheme {
        TimerScreen()
    }
}
