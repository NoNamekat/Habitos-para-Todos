package br.com.habitosparatodos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Purple = Color(0xFF7667F8)
private val Mint = Color(0xFF4FC6A5)
private val Pale = Color(0xFFF5F5FC)
private val Ink = Color(0xFF20233A)
data class Habit(val id: Long, val name: String, val emoji: String, val color: Long, val doneDates: Set<String> = emptySet())

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { HabitosApp() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitosApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("habitos_local", 0) }
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }
    var tab by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var showDayDetails by remember { mutableStateOf(false) }
    var dayNote by remember { mutableStateOf("") }
    var dayEmoji by remember { mutableStateOf("😊") }
    var dayNotes by remember {
        mutableStateOf(loadDayNotes(prefs.getString("day_notes", null)))
    }
    var dayEmojis by remember {
        mutableStateOf(loadDayEmojis(prefs.getString("day_emojis", null)))
    }
    fun saveDayDetails() {
        val key = selectedDate.toString()
        dayNotes = if (dayNote.isBlank()) dayNotes - key else dayNotes + (key to dayNote.trim())
        dayEmojis = dayEmojis + (key to dayEmoji)
        prefs.edit().putString("day_notes", saveStringMap(dayNotes)).putString("day_emojis", saveStringMap(dayEmojis)).apply()
        showDayDetails = false
    }
    fun openDayDetails(date: LocalDate) {
        selectedDate = date
        dayNote = dayNotes[date.toString()] ?: ""
        dayEmoji = dayEmojis[date.toString()] ?: "😊"
        showDayDetails = true
    }
    var habitName by remember { mutableStateOf("") }
    var habitEmoji by remember { mutableStateOf("✨") }
    var habits by remember {
        mutableStateOf(loadHabits(prefs.getString("habits", null)).ifEmpty { listOf(
            Habit(1, "Beber água", "💧", 0xFF76B8F5), Habit(2, "Estudar", "📚", 0xFF7667F8),
            Habit(3, "Treinar", "🏋️", 0xFFFF8D83), Habit(4, "Ler 10 páginas", "📖", 0xFF4FC6A5)
        ) })
    }
    fun persist(updated: List<Habit>) { habits = updated; prefs.edit().putString("habits", saveHabits(updated)).apply() }
    val month = YearMonth.from(selectedDate)
    val completed = habits.count { selectedDate.toString() in it.doneDates }
    val bg = if (dark) Color(0xFF151625) else Pale
    val card = if (dark) Color(0xFF23253A) else Color.White
    val ink = if (dark) Color(0xFFF4F4FC) else Ink

    MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = Purple, background = bg, surface = card) else lightColorScheme(primary = Purple, background = bg, surface = card)) {
        Scaffold(containerColor = bg, bottomBar = {
            NavigationBar(containerColor = card) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.CalendarMonth, null) }, label = { Text("Calendário") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Hábitos") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Insights, null) }, label = { Text("Estatísticas") })
            }
        }, floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }, containerColor = Purple, contentColor = Color.White) { Icon(Icons.Default.Add, "Adicionar hábito") } }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Olá! 👋", color = Purple, style = MaterialTheme.typography.labelLarge); Text("Hábitos para Todos", color = ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        IconButton(onClick = { dark = !dark; prefs.edit().putBoolean("dark", dark).apply() }) { Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "Alternar tema", tint = ink) }
                    }
                }
                item {
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Purple), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("SEU PROGRESSO", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium)
                            Text("$completed de ${habits.size} hábitos concluídos", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(progress = { if (habits.isEmpty()) 0f else completed.toFloat() / habits.size }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Mint, trackColor = Color.White.copy(alpha = .25f))
                            Text(if (completed == habits.size && habits.isNotEmpty()) "Mandou bem! Tudo concluído 🎉" else "Um passo de cada vez. Você consegue!", color = Color.White.copy(alpha = .9f))
                        }
                    }
                }
                if (tab == 0) {
                    item {
                        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = card)) {
                            Column(Modifier.padding(15.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }, color = ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                    Row { TextButton(onClick = { selectedDate = selectedDate.minusMonths(1).withDayOfMonth(1) }) { Text("‹") }; TextButton(onClick = { selectedDate = selectedDate.plusMonths(1).withDayOfMonth(1) }) { Text("›") } }
                                }
                                Row(Modifier.fillMaxWidth()) { listOf("S", "T", "Q", "Q", "S", "S", "D").forEach { Text(it, Modifier.weight(1f), color = ink.copy(alpha = .55f), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold) } }
                                Spacer(Modifier.height(8.dp))
                                val offset = month.atDay(1).dayOfWeek.value - 1
                                val total = month.lengthOfMonth()
                                val cells = ((offset + total + 6) / 7) * 7
                                for (week in 0 until cells / 7) Row(Modifier.fillMaxWidth()) {
                                    for (col in 0..6) {
                                        val day = week * 7 + col - offset + 1
                                        Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                                            if (day in 1..total) {
                                                val date = month.atDay(day); val chosen = date == selectedDate
                                                Column(Modifier.fillMaxSize().background(if (chosen) Purple else Color.Transparent, CircleShape).clickable { openDayDetails(date) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                    Text("$day", color = if (chosen) Color.White else ink, fontWeight = if (chosen || date == today) FontWeight.Bold else FontWeight.Normal)
                                                    val emoji = dayEmojis[date.toString()]
                                                    if (emoji != null) Text(emoji, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                                    if (habits.any { date.toString() in it.doneDates }) Box(Modifier.padding(top = 2.dp).size(4.dp).background(if (chosen) Color.White else Mint, CircleShape))
                                                }
                                            }
                                        }
                                    }
                                }
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Dia selecionado: ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", color = ink.copy(alpha = .6f), style = MaterialTheme.typography.bodySmall)
                                    TextButton(onClick = { openDayDetails(selectedDate) }) { Icon(Icons.Default.EditNote, null); Spacer(Modifier.width(4.dp)); Text("Nota e emoji") }
                                }
                            }
                        }
                    }
                    item { Text("Hábitos do dia", color = ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    items(habits, key = { it.id }) { h -> HabitRow(h, selectedDate.toString() in h.doneDates, card, ink, { toggle(h, selectedDate, habits, ::persist) }, { persist(habits.filterNot { it.id == h.id }) }) }
                } else if (tab == 1) {
                    item { Text("Todos os hábitos", color = ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    items(habits, key = { it.id }) { h -> HabitRow(h, selectedDate.toString() in h.doneDates, card, ink, { toggle(h, selectedDate, habits, ::persist) }, { persist(habits.filterNot { it.id == h.id }) }) }
                } else {
                    item { Text("Seu progresso", color = ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    item { Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = card)) { Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Resumo geral", color = ink, fontWeight = FontWeight.Bold); Text("Hábitos cadastrados: ${habits.size}", color = ink); Text("Marcações concluídas: ${habits.sumOf { it.doneDates.size }}", color = ink); Text("Conclusões no dia selecionado: $completed/${habits.size}", color = ink); Text("Pequenas ações viram grandes resultados. 🌱", color = ink.copy(alpha = .7f)) } } }
                    items(habits, key = { it.id }) { h -> Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(h.emoji, style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.width(12.dp)); Column { Text(h.name, color = ink, fontWeight = FontWeight.SemiBold); Text("${h.doneDates.size} dias concluídos", color = ink.copy(alpha = .65f)) } } } }
                }
            }
        }
    }
    if (showDayDetails) AlertDialog(
        onDismissRequest = { showDayDetails = false },
        title = { Text("Seu dia • ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM"))}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Como foi seu dia? Escolha um emoji")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("😊", "💪", "🔥", "😴", "🥰", "😔", "🎯", "🌱", "🎉", "📚").forEach { e ->
                        Text(e, Modifier.background(if (dayEmoji == e) Purple.copy(alpha = .2f) else Color.Transparent, RoundedCornerShape(10.dp)).clickable { dayEmoji = e }.padding(6.dp))
                    }
                }
                OutlinedTextField(value = dayNote, onValueChange = { dayNote = it }, label = { Text("Nota do dia") }, placeholder = { Text("O que aconteceu hoje?") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Text("Os registros ficam salvos neste aparelho.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = { saveDayDetails() }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = { showDayDetails = false }) { Text("Cancelar") } }
    )
    if (showAdd) AlertDialog(onDismissRequest = { showAdd = false; habitName = "" }, title = { Text("Novo hábito") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = habitName, onValueChange = { habitName = it }, label = { Text("Nome do hábito") }, singleLine = true)
            Text("Escolha um emoji")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("✨", "💧", "📚", "🏋️", "📖", "🥗", "🧘", "🚶").forEach { e -> Text(e, Modifier.clickable { habitEmoji = e }.background(if (habitEmoji == e) Purple.copy(alpha = .2f) else Color.Transparent, RoundedCornerShape(9.dp)).padding(6.dp)) } }
        }
    }, confirmButton = { TextButton(onClick = { val n = habitName.trim(); if (n.isNotEmpty()) { val cs = listOf(0xFF76B8F5, 0xFF7667F8, 0xFFFF8D83, 0xFF4FC6A5, 0xFFFFC857); persist(habits + Habit(System.currentTimeMillis(), n, habitEmoji, cs[habits.size % cs.size])); showAdd = false; habitName = ""; habitEmoji = "✨" } }) { Text("Adicionar") } }, dismissButton = { TextButton(onClick = { showAdd = false; habitName = "" }) { Text("Cancelar") } })
}

private fun toggle(h: Habit, date: LocalDate, habits: List<Habit>, persist: (List<Habit>) -> Unit) {
    val key = date.toString()
    persist(habits.map { item -> if (item.id == h.id) item.copy(doneDates = if (key in item.doneDates) item.doneDates - key else item.doneDates + key) else item })
}

@Composable
fun HabitRow(h: Habit, checked: Boolean, card: Color, ink: Color, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = card), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).background(Color(h.color).copy(alpha = .18f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text(h.emoji, style = MaterialTheme.typography.titleLarge) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(h.name, color = ink, fontWeight = FontWeight.SemiBold); Text(if (checked) "Concluído neste dia" else "Toque para marcar como feito", color = ink.copy(alpha = .58f), style = MaterialTheme.typography.bodySmall) }
            Checkbox(checked = checked, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = Purple))
            Text("×", Modifier.clickable { onDelete() }.padding(horizontal = 4.dp), color = ink.copy(alpha = .55f), style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun saveHabits(habits: List<Habit>): String {
    val arr = JSONArray()
    habits.forEach { h -> val o = JSONObject(); o.put("id", h.id); o.put("name", h.name); o.put("emoji", h.emoji); o.put("color", h.color); val dates = JSONArray(); h.doneDates.forEach { dates.put(it) }; o.put("dates", dates); arr.put(o) }
    return arr.toString()
}
private fun loadHabits(raw: String?): List<Habit> {
    if (raw.isNullOrBlank()) return emptyList()
    return try { val arr = JSONArray(raw); (0 until arr.length()).map { i -> val o = arr.getJSONObject(i); val d = o.getJSONArray("dates"); Habit(o.getLong("id"), o.getString("name"), o.optString("emoji", "✨"), o.optLong("color", 0xFF7667F8), (0 until d.length()).map { d.getString(it) }.toSet()) } } catch (_: Exception) { emptyList() }
}


private fun saveStringMap(map: Map<String, String>): String {
    val obj = JSONObject()
    map.forEach { (key, value) -> obj.put(key, value) }
    return obj.toString()
}

private fun loadDayNotes(raw: String?): Map<String, String> = loadStringMap(raw)
private fun loadDayEmojis(raw: String?): Map<String, String> = loadStringMap(raw)

private fun loadStringMap(raw: String?): Map<String, String> {
    if (raw.isNullOrBlank()) return emptyMap()
    return try {
        val obj = JSONObject(raw)
        obj.keys().asSequence().associateWith { obj.getString(it) }
    } catch (_: Exception) { emptyMap() }
}
