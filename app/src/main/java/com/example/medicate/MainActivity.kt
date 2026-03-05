package com.example.medicate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicate.ui.theme.MedicateTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Colors
val AmoledBlack = Color(0xFF000000)

// Data class to hold our meditation records
data class MeditationSession(
    val id: Long,
    val durationSeconds: Long,
    val timestamp: Long
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedicateTheme {
                var currentScreen by remember { mutableStateOf("timer") }
                val sessionLogs = remember { mutableStateListOf<MeditationSession>() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AmoledBlack
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (currentScreen == "timer") {
                            TimerScreen(
                                onNavigateToLogs = { currentScreen = "logs" },
                                onSaveSession = { duration ->
                                    if (duration > 0) {
                                        sessionLogs.add(0, MeditationSession(
                                            id = System.currentTimeMillis(),
                                            durationSeconds = duration,
                                            timestamp = System.currentTimeMillis()
                                        ))
                                    }
                                }
                            )
                        } else {
                            LogsScreen(
                                logs = sessionLogs,
                                onNavigateToTimer = { currentScreen = "timer" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerScreen(
    onNavigateToLogs: () -> Unit,
    onSaveSession: (Long) -> Unit
) {
    var timeSpent by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            timeSpent += 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .border(BorderStroke(1.dp, Color.White), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(timeSpent),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 72.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "session time",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MedicateButton("pause") { isRunning = false }
                MedicateButton("start") { isRunning = true }
                MedicateButton("stop") {
                    if (isRunning || timeSpent > 0) {
                        isRunning = false
                        onSaveSession(timeSpent)
                        timeSpent = 0
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))

            MedicateButton("logs") {
                onNavigateToLogs()
            }
        }
    }
}

@Composable
fun LogsScreen(
    logs: List<MeditationSession>,
    onNavigateToTimer: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("today") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab Row with inverted border logic
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(text = "today", isSelected = selectedTab == "today") { selectedTab = "today" }
            TabItem(text = "week", isSelected = selectedTab == "week") { selectedTab = "week" }
            TabItem(text = "month", isSelected = selectedTab == "month") { selectedTab = "month" }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(BorderStroke(1.dp, Color.White), RectangleShape)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                "today" -> TodayView(logs)
                "week" -> WeekView(logs)
                "month" -> MonthView(logs)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        MedicateButton("Meditate") {
            onNavigateToTimer()
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 90.dp, height = 45.dp)
            // If SELECTED -> No Border. If NOT SELECTED -> White Border.
            .then(
                if (!isSelected) {
                    Modifier.border(BorderStroke(1.dp, Color.White), RectangleShape)
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun TodayView(logs: List<MeditationSession>) {
    val todayLogs = logs.filter { isSameDay(it.timestamp, System.currentTimeMillis()) }
    
    if (todayLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "no sessions today", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(todayLogs) { session ->
                LogEntry(session)
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun WeekView(logs: List<MeditationSession>) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val weeklyData = remember(logs) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        (0..6).map { i ->
            val d = calendar.timeInMillis
            val totalSecs = logs.filter { isSameDay(it.timestamp, d) }.sumOf { it.durationSeconds }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            totalSecs / 60f
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Weekly Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(30.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val maxMin = (weeklyData.maxOrNull() ?: 0f).coerceAtLeast(10f)
            weeklyData.forEachIndexed { index, mins ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .fillMaxHeight(mins / maxMin)
                            .background(Color.White, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(days[index], color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Total this week: ${weeklyData.sum().toInt()} min",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MonthView(logs: List<MeditationSession>) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    val calendar = remember { Calendar.getInstance() }
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val emptyDays = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(emptyDays) { Spacer(Modifier.size(35.dp)) }
            items((1..daysInMonth).toList()) { day ->
                val dayCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, currentMonth)
                    set(Calendar.DAY_OF_MONTH, day)
                }
                val timestamp = dayCalendar.timeInMillis
                val hasLogs = logs.any { isSameDay(it.timestamp, timestamp) }
                
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .padding(2.dp)
                        .clickable { selectedDate = timestamp }
                        .border(
                            if (hasLogs) BorderStroke(1.5.dp, Color.White) else BorderStroke(0.dp, Color.Transparent),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (hasLogs) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        selectedDate?.let { ts ->
            val sessions = logs.filter { isSameDay(it.timestamp, ts) }
            val totalSecs = sessions.sumOf { it.durationSeconds }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date(ts)),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Time", color = Color.Gray)
                        Text(formatTime(totalSecs), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sessions", color = Color.Gray)
                        Text("${sessions.size}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntry(session: MeditationSession) {
    val dateText = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(session.timestamp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = dateText, color = Color.White, fontSize = 14.sp)
        Text(text = formatTime(session.durationSeconds), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MedicateButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, Color.White),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        modifier = Modifier.size(width = 120.dp, height = 55.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return sdf.format(Date(t1)) == sdf.format(Date(t2))
}

fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun LogsPreview() {
    MedicateTheme {
        LogsScreen(
            logs = listOf(
                MeditationSession(1, 600, System.currentTimeMillis()),
                MeditationSession(2, 300, System.currentTimeMillis() - 86400000)
            ),
            onNavigateToTimer = {}
        )
    }
}
