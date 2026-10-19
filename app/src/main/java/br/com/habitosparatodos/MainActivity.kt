package br.com.habitosparatodos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// =====================================================
// CORES DO APLICATIVO
// =====================================================

private val Purple = Color(0xFF7667F8)
private val Mint = Color(0xFF4FC6A5)
private val Ink = Color(0xFF20233A)
private val LightBackground = Color(0xFFF6F6FC)
private val DarkBackground = Color(0xFF151625)

private val dayColors: List<Pair<String, Long?>> = listOf(
    "Sem cor" to null,
    "Rosa" to 0xFFFFD6D6L,
    "Vermelho" to 0xFFFFA8A8L,
    "Laranja" to 0xFFFFD2A8L,
    "Amarelo" to 0xFFFFE9A8L,
    "Verde" to 0xFFB8EAC5L,
    "Turquesa" to 0xFFB2E8E0L,
    "Azul" to 0xFFBBD8FFL,
    "Roxo" to 0xFFD9C8FFL,
    "Lilás" to 0xFFEAD9FFL,
    "Cinza" to 0xFFE0E0E8L
)

// =====================================================
// EMOJIS DISPONÍVEIS
// =====================================================

private val availableEmojis = listOf(
    "✔️", "✅", "☑️", "❌", "❎", "🚫",
    "🏋️", "💪", "🏃", "🚴", "🏊", "⚽",
    "🥊", "🧘", "🤸", "🏃‍♂️", "🚶", "🧗",
    "📚", "📝", "💻", "🎯", "🧠", "💼",
    "📖", "✏️", "🎓", "📊", "🔬", "🖊️",
    "🍎", "💧", "🥗", "💊", "🛌", "🥦",
    "🍉", "🥑", "☀️", "🌙", "🚰", "🩺",
    "😀", "😎", "😴", "😔", "😡", "🥳",
    "🤩", "😊", "😢", "😌", "😍", "🤗",
    "😰", "🥹", "😇", "😋", "🤔", "😤",
    "❤️", "🥰", "👨‍👩‍👧", "🎉", "🤝", "🙏",
    "💖", "💝", "🌹", "🐶", "🐱", "🏡",
    "🎮", "🎬", "🎵", "🎨", "🎸", "🎹",
    "📷", "🌳", "🏖️", "✈️", "🛍️", "☕",
    "🔥", "🌱", "⭐", "✨", "🌈", "💤"
)

private val workoutEmojis = setOf(
    "🏋️", "💪", "🏃", "🚴", "🏊", "⚽",
    "🥊", "🧘", "🤸", "🏃‍♂️", "🚶", "🧗"
)

// =====================================================
// MODELO DE REGISTRO
// =====================================================

data class DayRecord(
    val color: Long? = null,
    val emojis: List<String> = emptyList(),
    val note: String = ""
) {
    fun isEmpty(): Boolean {
        return color == null && emojis.isEmpty() && note.isBlank()
    }
}

// =====================================================
// ACTIVITY
// =====================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite que a janela se ajuste quando o teclado aparecer.
        window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setContent {
            HabitosApp()
        }
    }
}

// =====================================================
// APLICATIVO PRINCIPAL
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitosApp() {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences("habitos_local", 0)
    }

    val today = remember { LocalDate.now() }

    var selectedDate by remember {
        mutableStateOf(today)
    }

    var displayedMonth by remember {
        mutableStateOf(YearMonth.from(today))
    }

    var dark by remember {
        mutableStateOf(prefs.getBoolean("dark", false))
    }

    var currentTab by remember {
        mutableIntStateOf(0)
    }

    var showDayDetails by remember {
        mutableStateOf(false)
    }

    var dayNote by remember {
        mutableStateOf("")
    }

    var selectedColor by remember {
        mutableStateOf<Long?>(null)
    }

    var selectedEmojis by remember {
        mutableStateOf(setOf<String>())
    }

    var records by remember {
        mutableStateOf(
            loadRecords(prefs.getString("day_records_v3", null))
        )
    }

    val background = if (dark) DarkBackground else LightBackground
    val cardColor = if (dark) Color(0xFF23253A) else Color.White
    val textColor = if (dark) Color(0xFFF4F4FC) else Ink

    fun persistRecords(updated: Map<String, DayRecord>) {
        records = updated

        prefs.edit()
            .putString("day_records_v3", saveRecords(updated))
            .apply()
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

        val record = DayRecord(
            color = selectedColor,
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
                primary = Purple,
                secondary = Mint,
                background = background,
                surface = cardColor
            )
        } else {
            lightColorScheme(
                primary = Purple,
                secondary = Mint,
                background = background,
                surface = cardColor
            )
        }
    ) {
        Scaffold(
            containerColor = background,

            bottomBar = {
                NavigationBar(
                    containerColor = cardColor
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Calendário"
                            )
                        },
                        label = { Text("Calendário") }
                    )

                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = {
                            Icon(
                                Icons.Default.Insights,
                                contentDescription = "Estatísticas"
                            )
                        },
                        label = { Text("Estatísticas") }
                    )
                }
            }
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp),

                contentPadding = PaddingValues(
                    top = 18.dp,
                    bottom = 24.dp
                ),

                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // CABEÇALHO MINIMALISTA

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Meu calendário",
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

                        IconButton(
                            onClick = {
                                dark = !dark

                                prefs.edit()
                                    .putBoolean("dark", dark)
                                    .apply()
                            }
                        ) {
                            Icon(
                                imageVector = if (dark) {
                                    Icons.Default.LightMode
                                } else {
                                    Icons.Default.DarkMode
                                },
                                contentDescription = "Alternar tema",
                                tint = textColor
                            )
                        }
                    }
                }

                if (currentTab == 0) {

                    // CALENDÁRIO

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardColor
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        displayedMonth.format(
                                            DateTimeFormatter.ofPattern(
                                                "MMMM yyyy",
                                                Locale("pt", "BR")
                                            )
                                        ).replaceFirstChar { it.uppercase() },

                                        color = textColor,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row {
                                        TextButton(
                                            onClick = {
                                                displayedMonth =
                                                    displayedMonth.minusMonths(1)
                                            }
                                        ) {
                                            Text(
                                                "‹",
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                displayedMonth =
                                                    displayedMonth.plusMonths(1)
                                            }
                                        ) {
                                            Text(
                                                "›",
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        "SEG", "TER", "QUA", "QUI",
                                        "SEX", "SÁB", "DOM"
                                    ).forEach { weekday ->
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

                                val offset =
                                    displayedMonth.atDay(1).dayOfWeek.value - 1

                                val totalDays = displayedMonth.lengthOfMonth()

                                val totalCells =
                                    ((offset + totalDays + 6) / 7) * 7

                                for (week in 0 until totalCells / 7) {

                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                        for (column in 0..6) {

                                            val day =
                                                week * 7 + column - offset + 1

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(0.82f)
                                                    .padding(2.dp),

                                                contentAlignment = Alignment.Center
                                            ) {

                                                if (day in 1..totalDays) {

                                                    val date =
                                                        displayedMonth.atDay(day)

                                                    val key = date.toString()

                                                    val record = records[key]

                                                    val isSelected =
                                                        date == selectedDate

                                                    val dayBackground =
                                                        if (record?.color != null) {
                                                            Color(record.color)
                                                        } else if (isSelected) {
                                                            Purple.copy(alpha = 0.12f)
                                                        } else {
                                                            Color.Transparent
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
                                                                        Purple,
                                                                        CircleShape
                                                                    )
                                                                } else {
                                                                    Modifier
                                                                }
                                                            )
                                                            .clickable {
                                                                openDay(date)
                                                            }
                                                            .padding(2.dp),

                                                        horizontalAlignment =
                                                            Alignment.CenterHorizontally,

                                                        verticalArrangement =
                                                            Arrangement.Center
                                                    ) {

                                                        Text(
                                                            text = day.toString(),

                                                            color = textColor,

                                                            fontWeight =
                                                                if (
                                                                    date == today ||
                                                                    isSelected
                                                                ) {
                                                                    FontWeight.Bold
                                                                } else {
                                                                    FontWeight.Normal
                                                                },

                                                            style = MaterialTheme.typography.bodyMedium
                                                        )

                                                        val emojiPreview =
                                                            record?.emojis
                                                                ?.take(2)
                                                                ?.joinToString("")

                                                        if (!emojiPreview.isNullOrEmpty()) {
                                                            Text(
                                                                text = emojiPreview,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                maxLines = 1
                                                            )
                                                        } else if (
                                                            record?.note?.isNotBlank() == true
                                                        ) {
                                                            Text(
                                                                "📝",
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                HorizontalDivider(
                                    color = textColor.copy(alpha = 0.1f)
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    "Toque em qualquer dia para registrar suas atividades.",
                                    color = textColor.copy(alpha = 0.65f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // RESUMO DO DIA SELECIONADO

                    item {
                        val record = records[selectedDate.toString()]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    openDay(selectedDate)
                                },

                            shape = RoundedCornerShape(22.dp),

                            colors = CardDefaults.cardColors(
                                containerColor = cardColor
                            )
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
                                            selectedDate.format(
                                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                            ),

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

                                    Text(
                                        "Editar ✏️",
                                        color = Purple,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (
                                    record == null ||
                                    record.isEmpty()
                                ) {
                                    Text(
                                        "Nenhum registro neste dia.",
                                        color = textColor.copy(alpha = 0.65f)
                                    )
                                } else {

                                    if (record.emojis.isNotEmpty()) {
                                        Text(
                                            record.emojis.joinToString(" "),
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }

                                    if (record.note.isNotBlank()) {
                                        Text(
                                            record.note,
                                            color = textColor,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    if (record.color != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(14.dp)
                                                    .background(
                                                        Color(record.color),
                                                        CircleShape
                                                    )
                                                    .border(
                                                        1.dp,
                                                        textColor.copy(alpha = 0.2f),
                                                        CircleShape
                                                    )
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

                    // ESTATÍSTICAS CALCULADAS PELOS REGISTROS

                    val totalDays = records.values.count {
                        !it.isEmpty()
                    }

                    val positiveMarks = records.values.sumOf {
                        it.emojis.count { emoji ->
                            emoji == "✔️" ||
                                emoji == "✅" ||
                                emoji == "☑️"
                        }
                    }

                    val negativeMarks = records.values.sumOf {
                        it.emojis.count { emoji ->
                            emoji == "❌" ||
                                emoji == "❎" ||
                                emoji == "🚫"
                        }
                    }

                    val workoutDays = records.values.count { record ->
                        record.emojis.any { it in workoutEmojis }
                    }

                    val noteDays = records.values.count {
                        it.note.isNotBlank()
                    }

                    val monthRecords = records.filterKeys {
                        runCatching {
                            YearMonth.from(LocalDate.parse(it)) == displayedMonth
                        }.getOrDefault(false)
                    }

                    item {
                        Text(
                            "Suas estatísticas",
                            color = textColor,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            "Um resumo das suas marcações no calendário.",
                            color = textColor.copy(alpha = 0.65f)
                        )
                    }

                    item {
                        StatisticCard(
                            title = "Dias registrados",
                            value = totalDays.toString(),
                            emoji = "📅",
                            description = "Dias com alguma marcação",
                            cardColor = cardColor,
                            textColor = textColor
                        )
                    }

                    item {
                        StatisticCard(
                            title = "Atividades físicas",
                            value = workoutDays.toString(),
                            emoji = "💪",
                            description = "Dias com emojis de exercícios",
                            cardColor = cardColor,
                            textColor = textColor
                        )
                    }

                    item {
                        StatisticCard(
                            title = "Marcações positivas",
                            value = positiveMarks.toString(),
                            emoji = "✔️",
                            description = "Total de marcações positivas",
                            cardColor = cardColor,
                            textColor = textColor
                        )
                    }

                    item {
                        StatisticCard(
                            title = "Marcações negativas",
                            value = negativeMarks.toString(),
                            emoji = "❌",
                            description = "Total de marcações negativas",
                            cardColor = cardColor,
                            textColor = textColor
                        )
                    }

                    item {
                        StatisticCard(
                            title = "Dias com anotações",
                            value = noteDays.toString(),
                            emoji = "📝",
                            description = "Dias com notas salvas",
                            cardColor = cardColor,
                            textColor = textColor
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardColor
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Resumo do mês",
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    displayedMonth.format(
                                        DateTimeFormatter.ofPattern(
                                            "MMMM yyyy",
                                            Locale("pt", "BR")
                                        )
                                    ).replaceFirstChar { it.uppercase() },

                                    color = Purple,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    "Dias registrados: ${monthRecords.values.count { !it.isEmpty() }}",
                                    color = textColor
                                )

                                Text(
                                    "Notas escritas: ${monthRecords.values.count { it.note.isNotBlank() }}",
                                    color = textColor
                                )

                                Text(
                                    "Continue registrando seus dias. Cada passo conta! 🌱",
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =================================================
        // PAINEL INFERIOR PARA EDITAR UM DIA
        // =================================================

        if (showDayDetails) {

            ModalBottomSheet(
                onDismissRequest = {
                    showDayDetails = false
                },

                containerColor = cardColor,

                sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
            ) {

                // AJUSTE: permite rolar o painel com o teclado aberto.

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
                        "Registro do dia",
                        color = textColor,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        selectedDate.format(
                            DateTimeFormatter.ofPattern(
                                "EEEE, dd 'de' MMMM",
                                Locale("pt", "BR")
                            )
                        ).replaceFirstChar { it.uppercase() },

                        color = textColor.copy(alpha = 0.7f)
                    )

                    // ESCOLHER A COR

                    Text(
                        "Cor do dia",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )

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
                                        .background(
                                            colorValue?.let { Color(it) }
                                                ?: Color.Transparent
                                        )
                                        .then(
                                            if (selected) {
                                                Modifier.border(
                                                    3.dp,
                                                    Purple,
                                                    CircleShape
                                                )
                                            } else {
                                                Modifier.border(
                                                    1.dp,
                                                    textColor.copy(alpha = 0.25f),
                                                    CircleShape
                                                )
                                            }
                                        )
                                        .clickable {
                                            selectedColor = colorValue
                                        },

                                    contentAlignment = Alignment.Center
                                ) {
                                    if (colorValue == null) {
                                        Text(
                                            "×",
                                            color = textColor,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }

                                Text(
                                    name,
                                    color = textColor.copy(alpha = 0.75f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = textColor.copy(alpha = 0.12f)
                    )

                    // ESCOLHER VÁRIOS EMOJIS

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Como foi seu dia?",
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            "${selectedEmojis.size} selecionados",
                            color = Purple,
                            style = MaterialTheme.typography.labelMedium
                        )
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
                                        if (isSelected) {
                                            Purple.copy(alpha = 0.2f)
                                        } else {
                                            textColor.copy(alpha = 0.04f)
                                        }
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                1.dp,
                                                Purple,
                                                RoundedCornerShape(12.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable {
                                        selectedEmojis =
                                            if (isSelected) {
                                                selectedEmojis - emoji
                                            } else {
                                                selectedEmojis + emoji
                                            }
                                    },

                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    emoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // NOTA DO DIA

                    Text(
                        "Anotação",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = dayNote,

                        onValueChange = {
                            dayNote = it
                        },

                        modifier = Modifier.fillMaxWidth(),

                        placeholder = {
                            Text("Escreva algo sobre este dia...")
                        },

                        minLines = 3,
                        maxLines = 5,

                        shape = RoundedCornerShape(16.dp)
                    )

                    // AÇÕES

                    Button(
                        onClick = {
                            saveSelectedDay()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple
                        )
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null
                        )

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
                                persistRecords(
                                    records - selectedDate.toString()
                                )

                                showDayDetails = false
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFE05252)
                            )

                            Spacer(Modifier.width(5.dp))

                            Text(
                                "Excluir registro",
                                color = Color(0xFFE05252)
                            )
                        }

                        TextButton(
                            onClick = {
                                showDayDetails = false
                            }
                        ) {
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
// COMPONENTE DE ESTATÍSTICAS
// =====================================================

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    emoji: String,
    description: String,
    cardColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
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
                    .background(
                        Purple.copy(alpha = 0.12f),
                        RoundedCornerShape(16.dp)
                    ),

                contentAlignment = Alignment.Center
            ) {
                Text(
                    emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),

                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )

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

private fun saveRecords(
    records: Map<String, DayRecord>
): String {

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

        record.emojis.forEach { emoji ->
            emojis.put(emoji)
        }

        obj.put("emojis", emojis)

        root.put(date, obj)
    }

    return root.toString()
}

private fun loadRecords(
    raw: String?
): Map<String, DayRecord> {

    if (raw.isNullOrBlank()) {
        return emptyMap()
    }

    return try {

        val root = JSONObject(raw)
        val result = mutableMapOf<String, DayRecord>()

        root.keys().forEach { date ->

            val obj = root.getJSONObject(date)

            val color = if (obj.isNull("color")) {
                null
            } else {
                obj.optLong("color")
            }

            val note = obj.optString("note", "")

            val emojisArray = obj.optJSONArray("emojis")
                ?: JSONArray()

            val emojis = (0 until emojisArray.length()).map {
                emojisArray.getString(it)
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
