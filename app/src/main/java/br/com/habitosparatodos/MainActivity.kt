package br.com.habitosparatodos

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// =====================================================
// CORES DO APLICATIVO
// =====================================================

private val Purple = Color(0xFFC16BFF)
private val Mint = Color(0xFF72F2C2)
private val CompletedDayGreen = 0xFF176B45L
private val NeonPink = Color(0xFFFF62C8)
private val NeonCyan = Color(0xFF5DDCFF)
private val Ink = Color(0xFF281B32)
private val LightBackground = Color(0xFFF5EFFA)
private val DarkBackground = Color(0xFF100B18)
private val DarkSurface = Color(0xFF1D1428)
private val LightSurface = Color(0xFFFFFBFF)

private val dayColors: List<Pair<String, Long?>> = listOf(
    "Sem cor" to null,
    "Rosa neon" to 0xFF4A193D,
    "Vermelho" to 0xFF4A2028,
    "Laranja" to 0xFF4A2B19,
    "Amarelo" to 0xFF4A3B18,
    "Verde neon" to 0xFF173B2C,
    "Turquesa" to 0xFF153B3A,
    "Azul neon" to 0xFF172C4A,
    "Roxo neon" to 0xFF33204A,
    "Lilás" to 0xFF3C244A,
    "Cinza" to 0xFF302B38
)

// =====================================================
// EMOJIS DISPONÍVEIS
// =====================================================

private val availableEmojis = listOf(
    "dm_ghost_happy", "dm_ghost_love", "dm_ghost_sad", "dm_ghost_cry",
    "dm_ghost_angry", "dm_ghost_dizzy", "dm_ghost_shock", "dm_black_cat",
    "dm_skull", "dm_stitched_heart", "dm_heart", "dm_broken_heart",
    "dm_star", "dm_moon", "dm_sun", "dm_cloud_rain", "dm_storm", "dm_cloud",
    "dm_rainbow", "dm_check", "dm_cross", "dm_dumbbell", "dm_kettlebell",
    "dm_ghost_run", "dm_skull_lift", "dm_shaker", "dm_sneaker", "dm_ghost_yoga",
    "dm_skeleton", "dm_heartbeat", "dm_measure", "dm_strong_ghost", "dm_books",
    "dm_phone", "dm_bike", "dm_trophy", "dm_graduate_skull", "dm_lightbulb",
    "dm_scroll", "dm_quill", "dm_hourglass", "dm_brain", "dm_target", "dm_water",
    "dm_apple", "dm_drink", "dm_plant", "dm_lotus", "dm_potion", "dm_groot",
    "dm_sleep_cat", "dm_roll", "dm_bed", "dm_health", "dm_shield", "dm_popcorn",
    "dm_gamepad", "dm_headphones", "dm_music", "dm_movie", "dm_camera", "dm_art",
    "dm_plane", "dm_suitcase", "dm_mountain", "dm_fire", "dm_map", "dm_crown",
    "dm_medal", "dm_alert_medal", "dm_award", "dm_ghost_celebrate", "dm_trophy_gold",
    "dm_pumpkin", "dm_gift", "dm_comet", "dm_gold_star", "dm_party", "dm_fireworks",
    "dm_success", "dm_cancel", "dm_warning", "dm_help", "dm_info", "dm_settings",
    "dm_home", "dm_calendar", "dm_clock", "dm_trash", "dm_share", "dm_bookmark",
    "dm_more"
)

private val calendarCategories = listOf("Geral", "Treino", "Estudos", "Leitura", "Saúde")

private val workoutEmojis = setOf(
    "🏋️", "💪", "🏃", "🚴", "🏊", "⚽", "🥊", "🧘", "🤸", "🏃‍♂️", "🚶", "🧗"
)

// Fallback visual: se uma imagem DARKMOOD não for encontrada no drawable,
// mostra um emoji correspondente em vez de escrever o nome "dm_...".
private val emojiFallbacks = mapOf(
    "dm_ghost_happy" to "👻", "dm_ghost_love" to "🥰",
    "dm_ghost_sad" to "😔", "dm_ghost_cry" to "😭",
    "dm_ghost_angry" to "😠", "dm_ghost_dizzy" to "😵",
    "dm_ghost_shock" to "😲", "dm_black_cat" to "🐈‍⬛",
    "dm_skull" to "💀", "dm_stitched_heart" to "❤️‍🩹",
    "dm_heart" to "❤️", "dm_broken_heart" to "💔",
    "dm_star" to "⭐", "dm_moon" to "🌙", "dm_sun" to "☀️",
    "dm_cloud_rain" to "🌧️", "dm_storm" to "⛈️", "dm_cloud" to "☁️",
    "dm_rainbow" to "🌈", "dm_check" to "✔️", "dm_cross" to "❌",
    "dm_dumbbell" to "🏋️", "dm_kettlebell" to "🏋️‍♂️",
    "dm_ghost_run" to "🏃", "dm_skull_lift" to "💪",
    "dm_shaker" to "🥤", "dm_sneaker" to "👟", "dm_ghost_yoga" to "🧘",
    "dm_skeleton" to "🦴", "dm_heartbeat" to "💓", "dm_measure" to "📏",
    "dm_strong_ghost" to "💪", "dm_books" to "📚", "dm_phone" to "📱",
    "dm_bike" to "🚴", "dm_trophy" to "🏆", "dm_graduate_skull" to "🎓",
    "dm_lightbulb" to "💡", "dm_scroll" to "📜", "dm_quill" to "🪶",
    "dm_hourglass" to "⌛", "dm_brain" to "🧠", "dm_target" to "🎯",
    "dm_water" to "💧", "dm_apple" to "🍎", "dm_drink" to "🥤",
    "dm_plant" to "🪴", "dm_lotus" to "🪷", "dm_potion" to "🧪",
    "dm_groot" to "🌳", "dm_sleep_cat" to "😴", "dm_roll" to "🧻",
    "dm_bed" to "🛏️", "dm_health" to "🩺", "dm_shield" to "🛡️",
    "dm_popcorn" to "🍿", "dm_gamepad" to "🎮", "dm_headphones" to "🎧",
    "dm_music" to "🎵", "dm_movie" to "🎬", "dm_camera" to "📷",
    "dm_art" to "🎨", "dm_plane" to "✈️", "dm_suitcase" to "🧳",
    "dm_mountain" to "⛰️", "dm_fire" to "🔥", "dm_map" to "🗺️",
    "dm_crown" to "👑", "dm_medal" to "🏅", "dm_alert_medal" to "🎖️",
    "dm_award" to "🏆", "dm_ghost_celebrate" to "🥳",
    "dm_trophy_gold" to "🏆", "dm_pumpkin" to "🎃", "dm_gift" to "🎁",
    "dm_comet" to "☄️", "dm_gold_star" to "🌟", "dm_party" to "🎉",
    "dm_fireworks" to "🎆", "dm_success" to "✅", "dm_cancel" to "🚫",
    "dm_warning" to "⚠️", "dm_help" to "❓", "dm_info" to "ℹ️",
    "dm_settings" to "⚙️", "dm_home" to "🏠", "dm_calendar" to "🗓️",
    "dm_clock" to "🕒", "dm_trash" to "🗑️", "dm_share" to "🔗",
    "dm_bookmark" to "🔖", "dm_more" to "•••"
)

// =====================================================
// MODELO DE REGISTRO
// =====================================================

data class DayRecord(
    val color: Long? = null,
    val emojis: List<String> = emptyList(),
    val note: String = ""
) {
    fun isEmpty(): Boolean = color == null && emojis.isEmpty() && note.isBlank()
}

// =====================================================
// ACTIVITY
// =====================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setContent {
            HabitosApp()
        }
    }
}

// =====================================================
// APLICATIVO PRINCIPAL
// =====================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitosApp() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("habitos_local", 0) }
    val today = remember { LocalDate.now() }

    var selectedDate by remember { mutableStateOf(today) }
    var displayedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", true)) }
    var rpgTheme by remember { mutableStateOf(prefs.getString("rpg_theme", "Arcano") ?: "Arcano") }
    val accent = when (rpgTheme) {
        "Floresta" -> Color(0xFF61D69B)
        "Aurora" -> Color(0xFFFF83C8)
        else -> Color(0xFFC16BFF)
    }
    var currentTab by remember { mutableIntStateOf(0) }
    var selectedCalendar by remember { mutableStateOf("Geral") }
    var showDayDetails by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var dayNote by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<Long?>(null) }
    var selectedEmojis by remember { mutableStateOf(setOf<String>()) }

    var records by remember {
        mutableStateOf(
            loadRecords(
                prefs.getString("day_records_v3_Geral", null)
                    ?: prefs.getString("day_records_v3", null)
            )
        )
    }

    val background = if (dark) DarkBackground else LightBackground
    val cardColor = if (dark) DarkSurface else LightSurface
    val textColor = if (dark) Color(0xFFFFF5FF) else Ink

    fun persistRecords(updated: Map<String, DayRecord>) {
        records = updated
        prefs.edit()
            .putString("day_records_v3_${selectedCalendar}", saveRecords(updated))
            .apply()
    }

    // Um toque alterna a conclusão. Registros e cores personalizados são preservados.
    fun markDayCompleted(date: LocalDate) {
        selectedDate = date
        val key = date.toString()
        val currentRecord = records[key] ?: DayRecord()
        val isCompleted = "✔️" in currentRecord.emojis

        val updatedRecord = if (isCompleted) {
            currentRecord.copy(
                color = if (currentRecord.color == CompletedDayGreen) null else currentRecord.color,
                emojis = currentRecord.emojis.filterNot { it == "✔️" }
            )
        } else {
            currentRecord.copy(
                color = currentRecord.color ?: CompletedDayGreen,
                emojis = (currentRecord.emojis + "✔️").distinct()
            )
        }

        val updatedRecords = if (updatedRecord.isEmpty()) {
            records - key
        } else {
            records + (key to updatedRecord)
        }

        persistRecords(updatedRecords)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun openDay(date: LocalDate) {
        selectedDate = date
        val record = records[date.toString()] ?: DayRecord()
        dayNote = record.note
        selectedColor = record.color
        selectedEmojis = record.emojis.toSet()
        showDayDetails = true
    }

    fun saveSelectedDay() {
        val key = selectedDate.toString()

        // Se o usuário retirar a conclusão no editor, remove também o verde automático.
        val colorToSave = if (
            "✔️" !in selectedEmojis && selectedColor == CompletedDayGreen
        ) null else selectedColor

        val record = DayRecord(
            color = colorToSave,
            emojis = selectedEmojis.toList(),
            note = dayNote.trim()
        )

        val updated = if (record.isEmpty()) {
            records - key
        } else {
            records + (key to record)
        }

        persistRecords(updated)
        showDayDetails = false
    }

    MaterialTheme(
        colorScheme = if (dark) {
            darkColorScheme(
                primary = accent,
                secondary = Mint,
                tertiary = NeonPink,
                background = background,
                surface = cardColor,
                onPrimary = Color(0xFF1B1024),
                onSecondary = Color(0xFF10251E)
            )
        } else {
            lightColorScheme(
                primary = accent,
                secondary = Mint,
                tertiary = NeonPink,
                background = background,
                surface = cardColor,
                onPrimary = Color(0xFF1B1024),
                onSecondary = Color(0xFF10251E)
            )
        }
    ) {
        Scaffold(
            containerColor = background,
            bottomBar = {
                NavigationBar(containerColor = cardColor) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendário") },
                        label = { Text("Calendário") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Purple,
                            selectedTextColor = Purple,
                            indicatorColor = Purple.copy(alpha = 0.18f),
                            unselectedIconColor = textColor.copy(alpha = 0.58f),
                            unselectedTextColor = textColor.copy(alpha = 0.58f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.Insights, contentDescription = "Estatísticas") },
                        label = { Text("Estatísticas") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Purple,
                            selectedTextColor = Purple,
                            indicatorColor = Purple.copy(alpha = 0.18f),
                            unselectedIconColor = textColor.copy(alpha = 0.58f),
                            unselectedTextColor = textColor.copy(alpha = 0.58f)
                        )
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                selectedCalendar,
                                color = textColor,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Registre os momentos do seu dia.",
                                color = textColor.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                dark = !dark
                                prefs.edit().putBoolean("dark", dark).apply()
                            }) {
                                Icon(
                                    imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (dark) "Ativar modo claro" else "Ativar modo escuro",
                                    tint = textColor
                                )
                            }
                            IconButton(onClick = { showSettings = true }) {
                                Text("⚙️", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }

                if (currentTab == 0) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    Purple.copy(alpha = if (dark) 0.24f else 0.16f),
                                    RoundedCornerShape(24.dp)
                                )
                                .pointerInput(displayedMonth) {
                                    var dragDistance = 0f
                                    detectHorizontalDragGestures(
                                        onHorizontalDrag = { _, amount -> dragDistance += amount },
                                        onDragEnd = {
                                            if (dragDistance > 70f) displayedMonth = displayedMonth.minusMonths(1)
                                            else if (dragDistance < -70f) displayedMonth = displayedMonth.plusMonths(1)
                                            dragDistance = 0f
                                        }
                                    )
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        displayedMonth.format(
                                            DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))
                                        ).replaceFirstChar { it.uppercase() },
                                        color = textColor,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        TextButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                                            Text("‹", style = MaterialTheme.typography.headlineMedium)
                                        }
                                        TextButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                                            Text("›", style = MaterialTheme.typography.headlineMedium)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM").forEach { weekday ->
                                        Text(
                                            text = weekday,
                                            modifier = Modifier.weight(1f),
                                            color = textColor.copy(alpha = 0.55f),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                val offset = displayedMonth.atDay(1).dayOfWeek.value - 1
                                val totalDaysInMonth = displayedMonth.lengthOfMonth()
                                val totalCells = ((offset + totalDaysInMonth + 6) / 7) * 7

                                for (week in 0 until totalCells / 7) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        for (column in 0..6) {
                                            val day = week * 7 + column - offset + 1
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(0.82f)
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (day in 1..totalDaysInMonth) {
                                                    val date = displayedMonth.atDay(day)
                                                    val key = date.toString()
                                                    val record = records[key]
                                                    val isSelected = date == selectedDate
                                                    val isCompleted = record?.emojis?.contains("✔️") == true

                                                    // A cor personalizada tem prioridade; o verde é usado
                                                    // quando a marcação foi automática e não há outra cor.
                                                    val dayBackground = when {
                                                        record?.color != null -> Color(record.color)
                                                        isCompleted -> Color(CompletedDayGreen)
                                                        isSelected -> Purple.copy(alpha = if (dark) 0.20f else 0.12f)
                                                        else -> Color.Transparent
                                                    }

                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape)
                                                            .background(dayBackground)
                                                            .then(
                                                                if (isSelected) {
                                                                    Modifier.border(
                                                                        2.dp,
                                                                        if (isCompleted) Mint else Purple,
                                                                        CircleShape
                                                                    )
                                                                } else Modifier
                                                            )
                                                            .clickable { selectedDate = date }
                                                            .padding(2.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = day.toString(),
                                                            color = if (isCompleted) Color.White else textColor,
                                                            fontWeight = if (
                                                                date == today || isSelected || isCompleted
                                                            ) FontWeight.Bold else FontWeight.Normal,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )

                                                        val emojiPreview = record?.emojis?.let { emojis ->
                                                            if ("✔️" in emojis) {
                                                                listOf("✔️") + emojis.filterNot { it == "✔️" }.take(1)
                                                            } else {
                                                                emojis.take(2)
                                                            }
                                                        }.orEmpty()

                                                        if (emojiPreview.isNotEmpty()) {
                                                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                                                emojiPreview.forEach { token ->
                                                                    DarkMoodEmoji(token, Modifier.size(17.dp))
                                                                }
                                                            }
                                                        } else if (record?.note?.isNotBlank() == true) {
                                                            Text("📝", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = textColor.copy(alpha = 0.1f))
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Selecione uma data e use o painel abaixo para concluir ou editar seu registro.",
                                    color = textColor.copy(alpha = 0.65f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text(
                                "Meus calendários",
                                color = textColor,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                calendarCategories.forEach { category ->
                                    val selected = selectedCalendar == category
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            if (selectedCalendar != category) {
                                                selectedCalendar = category
                                                records = loadRecords(
                                                    prefs.getString("day_records_v3_${category}", null)
                                                        ?: if (category == "Geral") {                                                            prefs.getString("day_records_v3", null)
                                                        } else null
                                                )
                                                selectedDate = today
                                            }
                                        },
                                        label = { Text(category) },
                                        leadingIcon = if (selected) {
                                            { Text("✦", color = Purple) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Purple.copy(alpha = if (dark) 0.22f else 0.13f),
                                            selectedLabelColor = textColor
                                        )
                                    )
                                }
                            }
                            Text(
                                "Cada categoria mantém seus registros separados.",
                                color = textColor.copy(alpha = 0.58f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    item {
                        val completedDays = records.values.count { "✔️" in it.emojis }
                        val totalXp = completedDays * 20
                        val currentLevel = totalXp / 100 + 1
                        val xpInLevel = totalXp % 100
                        XpProgressCard(
                            level = currentLevel,
                            totalXp = totalXp,
                            xpInLevel = xpInLevel,
                            completedDays = completedDays,
                            cardColor = cardColor,
                            textColor = textColor,
                            accent = accent
                        )
                    }

                    item {
                        val record = records[selectedDate.toString()]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    Purple.copy(alpha = if (dark) 0.22f else 0.14f),
                                    RoundedCornerShape(22.dp)
                                )
                                .clickable { openDay(selectedDate) },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                            color = textColor,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            when {
                                                selectedDate == today -> "Hoje"
                                                selectedDate.isBefore(today) -> "Dia anterior"
                                                else -> "Dia futuro"
                                            },
                                            color = textColor.copy(alpha = 0.6f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    TextButton(onClick = { openDay(selectedDate) }) {
                                        Text("Editar ✏️", color = accent, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                if (record == null || record.isEmpty()) {
                                    Text("Nenhum registro neste dia.", color = textColor.copy(alpha = 0.72f))
                                    Text("Adicione emojis, uma cor ou uma anotação para guardar esse momento.", color = textColor.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
                                } else {
                                    if (record.emojis.isNotEmpty()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            record.emojis.forEach { token ->
                                                DarkMoodEmoji(token, Modifier.size(38.dp))
                                            }
                                        }
                                    }
                                    if (record.note.isNotBlank()) {
                                        Text(record.note, color = textColor, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (record.color != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                Modifier
                                                    .size(14.dp)
                                                    .background(Color(record.color), CircleShape)
                                                    .border(1.dp, textColor.copy(alpha = 0.2f), CircleShape)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Dia identificado por cor",
                                                color = textColor.copy(alpha = 0.65f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val completedDates = records.filterValues { "✔️" in it.emojis }.keys.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.sorted()
                    val completedDays = completedDates.size
                    val totalXp = completedDays * 20
                    val totalDays = records.values.count { !it.isEmpty() }
                    val noteDays = records.values.count { it.note.isNotBlank() }
                    val positiveMarks = records.values.sumOf { record -> record.emojis.count { it in setOf("dm_check", "dm_success", "✔️", "✅", "☑️") } }
                    val negativeMarks = records.values.sumOf { record -> record.emojis.count { it in setOf("dm_cross", "dm_cancel", "❌", "❎", "🚫") } }
                    val darkMoodWorkoutEmojis = setOf("dm_dumbbell", "dm_kettlebell", "dm_ghost_run", "dm_skull_lift", "dm_sneaker", "dm_ghost_yoga", "dm_skeleton", "dm_bike")
                    val workoutDays = records.values.count { record -> record.emojis.any { it in workoutEmojis || it in darkMoodWorkoutEmojis } }
                    val monthRecords = records.filterKeys { runCatching { YearMonth.from(LocalDate.parse(it)) == displayedMonth }.getOrDefault(false) }
                    var currentStreak = 0
                    var cursor = LocalDate.now()
                    while (completedDates.contains(cursor)) { currentStreak++; cursor = cursor.minusDays(1) }
                    var bestStreak = 0
                    var runningStreak = 0
                    var previous: LocalDate? = null
                    completedDates.forEach { date ->
                        runningStreak = if (previous?.plusDays(1) == date) runningStreak + 1 else 1
                        bestStreak = maxOf(bestStreak, runningStreak)
                        previous = date
                    }

                    item {
                        Text("Estatísticas · $selectedCalendar", color = textColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Acompanhe sua evolução nesta categoria.", color = textColor.copy(alpha = 0.72f))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            calendarCategories.forEach { category ->
                                FilterChip(
                                    selected = selectedCalendar == category,
                                    onClick = {
                                        selectedCalendar = category
                                        records = loadRecords(prefs.getString("day_records_v3_$category", null) ?: if (category == "Geral") prefs.getString("day_records_v3", null) else null)
                                    },
                                    label = { Text(category) }
                                )
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatisticCard("Dias registrados", totalDays.toString(), "dm_calendar", "Com marcações", cardColor, textColor, Modifier.weight(1f))
                            StatisticCard("Experiência", totalXp.toString(), "dm_star", "XP acumulado", cardColor, textColor, Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatisticCard("Sequência atual", currentStreak.toString(), "dm_fire", "Dias seguidos", cardColor, textColor, Modifier.weight(1f))
                            StatisticCard("Melhor sequência", bestStreak.toString(), "dm_trophy", "Recorde pessoal", cardColor, textColor, Modifier.weight(1f))
                        }
                    }
                    item {
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Resumo do mês", color = textColor, fontWeight = FontWeight.Bold)
                                Text(displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }, color = accent, fontWeight = FontWeight.SemiBold)
                                Text("Dias registrados: ${monthRecords.values.count { !it.isEmpty() }}", color = textColor)
                                Text("Notas escritas: ${monthRecords.values.count { it.note.isNotBlank() }}", color = textColor)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    TextButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) { Text("‹ Anterior") }
                                    TextButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) { Text("Próximo ›") }
                                }
                            }
                        }
                    }
                    item {
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Atividade no mês", color = textColor, fontWeight = FontWeight.Bold)
                                val days = displayedMonth.lengthOfMonth()
                                val offset = displayedMonth.atDay(1).dayOfWeek.value - 1
                                val cells = ((offset + days + 6) / 7) * 7
                                for (week in 0 until cells / 7) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        for (col in 0..6) {
                                            val day = week * 7 + col - offset + 1
                                            val rec = if (day in 1..days) records[displayedMonth.atDay(day).toString()] else null
                                            Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(5.dp))
                                                .background(if (rec == null || rec.isEmpty()) textColor.copy(alpha = 0.06f) else accent.copy(alpha = if ("✔️" in rec.emojis) 0.9f else 0.35f)),
                                                contentAlignment = Alignment.Center) {
                                                if (day in 1..days) Text(day.toString(), color = if (rec != null && !rec.isEmpty()) Color(0xFF17121D) else textColor.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text("Conquistas", color = textColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Marcos desbloqueados na sua jornada", color = textColor.copy(alpha = 0.68f), style = MaterialTheme.typography.bodySmall)
                    }
                    item {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AchievementCard("Primeiro passo", "Conclua seu primeiro dia.", "🌱", completedDays, 1, cardColor, textColor)
                            AchievementCard("Uma semana de foco", "Complete 7 dias.", "🔥", completedDays, 7, cardColor, textColor)
                            AchievementCard("Em ritmo constante", "Complete 30 dias.", "⚔️", completedDays, 30, cardColor, textColor)
                            AchievementCard("Lenda da jornada", "Complete 100 dias.", "👑", completedDays, 100, cardColor, textColor)
                        }
                    }
                    if (selectedCalendar == "Treino") {
                        item {
                            StatisticCard("Atividades físicas", workoutDays.toString(), "dm_dumbbell", "Dias com emojis de exercícios", cardColor, textColor)
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatisticCard("Marcações positivas", positiveMarks.toString(), "dm_check", "Total positivo", cardColor, textColor, Modifier.weight(1f))
                            StatisticCard("Marcações negativas", negativeMarks.toString(), "dm_cross", "Total negativo", cardColor, textColor, Modifier.weight(1f))
                        }
                    }
                    item {
                        StatisticCard("Dias com anotações", noteDays.toString(), "dm_scroll", "Notas salvas", cardColor, textColor)
                    }
                }
            }
        }


        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                containerColor = cardColor,
                title = { Text("Personalizar aparência", color = textColor) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Escolha a atmosfera da sua jornada", color = textColor.copy(alpha = 0.75f))
                        listOf("Arcano", "Floresta", "Aurora").forEach { theme ->
                            val swatch = when (theme) {
                                "Floresta" -> Color(0xFF61D69B)
                                "Aurora" -> Color(0xFFFF83C8)
                                else -> Color(0xFFC16BFF)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                    .background(swatch.copy(alpha = if (rpgTheme == theme) 0.20f else 0.08f))
                                    .clickable {
                                        rpgTheme = theme
                                        prefs.edit().putString("rpg_theme", theme).apply()
                                    }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(Modifier.size(22.dp).clip(CircleShape).background(swatch))
                                Column(Modifier.weight(1f)) {
                                    Text(theme, color = textColor, fontWeight = FontWeight.SemiBold)
                                    Text(when (theme) { "Floresta" -> "Verde natural"; "Aurora" -> "Rosa suave"; else -> "Roxo místico" }, color = textColor.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
                                }
                                if (rpgTheme == theme) Text("✓", color = swatch, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showSettings = false }) { Text("Concluir") } }
            )
        }

        // =================================================
        // PAINEL INFERIOR PARA EDITAR UM DIA
        // =================================================

        if (showDayDetails) {
            ModalBottomSheet(
                onDismissRequest = { showDayDetails = false },
                containerColor = cardColor,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Registro — $selectedCalendar",
                        color = textColor,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        selectedDate.format(
                            DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
                        ).replaceFirstChar { it.uppercase() },
                        color = textColor.copy(alpha = 0.7f)
                    )

                    Text("Cor do dia", color = textColor, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dayColors.forEach { (name, colorValue) ->
                            val selected = selectedColor == colorValue
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(colorValue?.let { Color(it) } ?: Color.Transparent)
                                        .then(
                                            if (selected) {
                                                Modifier.border(3.dp, Purple, CircleShape)
                                            } else {
                                                Modifier.border(1.dp, textColor.copy(alpha = 0.25f), CircleShape)
                                            }
                                        )
                                        .clickable { selectedColor = colorValue },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (colorValue == null) {
                                        Text("×", color = textColor, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                                Text(name, color = textColor.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    HorizontalDivider(color = textColor.copy(alpha = 0.12f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Como foi seu dia?", color = textColor, fontWeight = FontWeight.SemiBold)
                        Text("${selectedEmojis.size} selecionados", color = Purple, style = MaterialTheme.typography.labelMedium)
                    }
                    Text(
                        "Toque em vários emojis para marcar tudo o que aconteceu.",
                        color = textColor.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableEmojis) { emoji ->
                            val isSelected = emoji in selectedEmojis
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Purple.copy(alpha = 0.2f)
                                        else textColor.copy(alpha = 0.04f)
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(1.dp, Purple, RoundedCornerShape(12.dp))
                                        } else Modifier
                                    )
                                    .clickable {
                                        selectedEmojis = if (isSelected) {
                                            selectedEmojis - emoji
                                        } else {
                                            selectedEmojis + emoji
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                DarkMoodEmoji(emoji = emoji, modifier = Modifier.size(30.dp))
                            }
                        }
                    }

                    Text("Anotação", color = textColor, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = dayNote,
                        onValueChange = { dayNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Escreva algo sobre este dia...") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = { saveSelectedDay() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Salvar registro")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                persistRecords(records - selectedDate.toString())
                                showDayDetails = false
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE05252))
                            Spacer(Modifier.width(5.dp))
                            Text("Excluir registro", color = Color(0xFFE05252))
                        }
                        TextButton(onClick = { showDayDetails = false }) {
                            Text("Cancelar")
                        }
                    }

                    Text(
                        "Seus registros são salvos neste aparelho.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = textColor.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// =====================================================
// PROGRESSÃO RPG: XP E NÍVEIS
// =====================================================

@Composable
private fun XpProgressCard(
    level: Int,
    totalXp: Int,
    xpInLevel: Int,
    completedDays: Int,
    cardColor: Color,
    textColor: Color,
    accent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("🏆", style = MaterialTheme.typography.titleLarge)
                    Text("Nível $level", color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text("$totalXp XP", color = accent, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { xpInLevel / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = accent,
                trackColor = textColor.copy(alpha = 0.12f)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$completedDays dias concluídos", color = textColor.copy(alpha = 0.68f), style = MaterialTheme.typography.labelSmall)
                Text("$xpInLevel/100 XP", color = textColor.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
// =====================================================
// CONQUISTAS RPG
// =====================================================

@Composable
private fun AchievementCard(
    title: String,
    description: String,
    emoji: String,
    current: Int,
    target: Int,
    cardColor: Color,
    textColor: Color
) {
    val unlocked = current >= target
    val progress = (current.toFloat() / target).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                (if (unlocked) NeonCyan else textColor.copy(alpha = 0.12f)),
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text(
                text = if (unlocked) emoji else "🔒",
                style = MaterialTheme.typography.headlineMedium
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    title,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (unlocked) "Conquista desbloqueada!" else description,
                    color = textColor.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (unlocked) NeonCyan else Purple,
                    trackColor = textColor.copy(alpha = 0.12f)
                )
                Text(
                    "$current / $target dias",
                    color = textColor.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (unlocked) {
                Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =====================================================
// EMOJIS DARKMOOD
// =====================================================

@Composable
private fun DarkMoodEmoji(emoji: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resourceId = remember(emoji) {
        context.resources.getIdentifier(emoji, "drawable", context.packageName)
    }

    if (resourceId != 0) {
        Image(
            painter = painterResource(resourceId),
            contentDescription = emoji.removePrefix("dm_").replace('_', ' '),
            modifier = modifier,
            contentScale = ContentScale.Fit        )
    } else {
        Text(
            text = emojiFallbacks[emoji] ?: "✨",
            modifier = modifier,
            textAlign = TextAlign.Center
        )
    }
}

// =====================================================
// COMPONENTE DE ESTATÍSTICAS
// =====================================================

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    emoji: String,
    description: String,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Purple.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                DarkMoodEmoji(emoji = emoji, modifier = Modifier.size(34.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, color = textColor, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    color = textColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                value,
                color = Purple,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

// =====================================================
// SALVAMENTO DOS REGISTROS EM JSON
// =====================================================

private fun saveRecords(records: Map<String, DayRecord>): String {
    val root = JSONObject()

    records.forEach { (date, record) ->
        val obj = JSONObject()

        if (record.color == null) {
            obj.put("color", JSONObject.NULL)
        } else {
            obj.put("color", record.color)
        }

        obj.put("note", record.note)

        val emojis = JSONArray()
        record.emojis.forEach { emoji -> emojis.put(emoji) }
        obj.put("emojis", emojis)

        root.put(date, obj)
    }

    return root.toString()
}

private fun loadRecords(raw: String?): Map<String, DayRecord> {
    if (raw.isNullOrBlank()) return emptyMap()

    return try {
        val root = JSONObject(raw)
        val result = mutableMapOf<String, DayRecord>()

        root.keys().forEach { date ->
            val obj = root.getJSONObject(date)

            val color = if (obj.isNull("color")) null else obj.optLong("color")
            val note = obj.optString("note", "")
            val emojisArray = obj.optJSONArray("emojis") ?: JSONArray()
            val emojis = (0 until emojisArray.length()).map { index ->
                emojisArray.getString(index)
            }

            result[date] = DayRecord(
                color = color,
                emojis = emojis,
                note = note
            )
        }

        result
    } catch (_: Exception) {
        emptyMap()
    }
}