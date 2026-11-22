package com.vetprep.app

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val Indigo = Color(0xFF1F2A6B); val Haldi = Color(0xFFF2B01E); val Chalk = Color(0xFFF4F5FA)
val Moss = Color(0xFF2E7D5B); val Mut = Color(0xFF5D6385); val Line = Color(0xFFDFE2EF)
val Ink = Color(0xFF14172B); val Bad = Color(0xFFB3261E)

data class Course(val n: String, val t: Int, val p: Int, val books: List<String> = emptyList())

val Years = listOf(
    listOf(
        Course("Veterinary Anatomy", 4, 3, listOf("Textbook of Veterinary Anatomy – Dyce, Sack & Wensing", "Primary Veterinary Anatomy – R.K. Ghosh", "Veterinary Anatomy of Domestic Mammals – König & Liebich", "Sisson and Grossman’s The Anatomy of the Domestic Animals – Getty")),
        Course("Veterinary Physiology", 4, 1, listOf("Cunningham’s Textbook of Veterinary Physiology – Klein", "Dukes’ Physiology of Domestic Animals – Reece et al.", "Veterinary Physiology – S.K. Garg")),
        Course("Livestock Production Management", 4, 2)),
    listOf(
        Course("Veterinary Pathology", 4, 2, listOf("Jubb, Kennedy & Palmer’s Pathology of Domestic Animals – Maxie (ed.)")),
        Course("Veterinary Biochemistry", 2, 1, listOf("Clinical Biochemistry of Domestic Animals – Kaneko, Harvey & Bruss")),
        Course("Veterinary Microbiology", 3, 2, listOf("Veterinary Microbiology and Microbial Disease – Quinn et al.", "Veterinary Microbiology – Hirsh et al.")),
        Course("Animal Genetics and Breeding", 3, 1, listOf("Introduction to Veterinary Genetics – F.W. Nicholas", "Genetics of Livestock Improvement – J.L. Lasley")),
        Course("Animal Nutrition", 3, 1, listOf("Animal Nutrition in the Tropics – R.A. Ranjhan", "Animal Nutrition – McDonald et al."))),
    listOf(
        Course("Veterinary Pharmacology and Toxicology", 4, 1, listOf("Veterinary Pharmacology and Therapeutics – Riviere & Papich")),
        Course("Veterinary Parasitology", 3, 2, listOf("Veterinary Parasitology – Taylor, Coop & Wall", "Helminths, Arthropods and Protozoa of Domesticated Animals – E.J.L. Soulsby")),
        Course("Veterinary Public Health and Epidemiology", 3, 1, listOf("Veterinary Epidemiology – M. Thrusfield")),
        Course("Livestock Products Technology", 2, 1),
        Course("Extension Education", 3, 1, listOf("Education and Communication for Development – O.P. Dahama & O.P. Bhatnagar")),
        Course("Veterinary Clinical Practices-I", 0, 1),
        Course("Livestock Farm Practices", 0, 2)),
    listOf(
        Course("Veterinary Surgery and Radiology", 2, 1, listOf("Ruminant Surgery: A Textbook of the Surgical Diseases of Cattle, Buffaloes, Camels, Sheep and Goats – R.P.S. Tyagi & J. Singh", "Essentials of Veterinary Surgery – A. Venugopalan", "A Textbook on Veterinary Surgery and Radiology, 2nd Edition – S.K. Nandi, S. Halder & M. Hoque (Kalyani Publishers)")),
        Course("Veterinary Medicine", 4, 1, listOf("Veterinary Medicine – Radostits et al.", "Large Animal Internal Medicine – Bradford P. Smith")),
        Course("Veterinary Gynaecology and Obstetrics", 2, 1, listOf("Veterinary Reproduction and Obstetrics – Noakes, Parkinson & England")),
        Course("Veterinary Clinical Practices-II", 0, 6)))

val Tips = listOf(
    "Draw and label anatomy diagrams every day, and learn Latin terms with flashcards. Turn physiology into flowcharts. Keep LPM notes short and point-wise.",
    "Link each lesion in pathology to its cause and organ. Make comparison tables for microbes. Practise spotters every week.",
    "Build drug-class tables for pharmacology. Draw life cycles for parasitology. Practise short answers in the exam format.",
    "Link every disease to its signs, diagnosis and treatment. Practise case write-ups and prescriptions. Revise the basics from earlier years.")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Indigo, background = Chalk)) { App() } }
    }
}

fun credits(y: Int) = Years[y - 1].sumOf { it.t + it.p }

@Composable
fun App() {
    val ctx = LocalContext.current
    val sp = remember { ctx.getSharedPreferences("vp", Context.MODE_PRIVATE) }
    val prog = remember { mutableStateMapOf<String, Int>().apply { Years.flatten().forEach { put(it.n, sp.getInt(it.n, 0)) } } }
    var exam by remember { mutableStateOf(sp.getString("exam", "") ?: "") }
    var tab by remember { mutableIntStateOf(0) }
    var year by remember { mutableIntStateOf(1) }
    var splash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1900); splash = false }
    val save: (String, Int) -> Unit = { k, v -> sp.edit().putInt(k, v).apply() }
    val pick = {
        val d = LocalDate.now()
        DatePickerDialog(ctx, { _, y, m, day -> exam = LocalDate.of(y, m + 1, day).toString(); sp.edit().putString("exam", exam).apply() }, d.year, d.monthValue - 1, d.dayOfMonth).show()
    }
    val names = listOf("Home", "Courses", "Exams", "Tools")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Menu, Icons.Filled.Info, Icons.Filled.Settings)
    Box {
        Scaffold(containerColor = Chalk, bottomBar = {
            NavigationBar(containerColor = Color.White) {
                names.forEachIndexed { i, n ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icons[i], null) }, label = { Text(n) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Indigo, selectedTextColor = Indigo, indicatorColor = Haldi.copy(alpha = 0.4f), unselectedIconColor = Mut, unselectedTextColor = Mut))
                }
            }
        }) { pad ->
            Box(Modifier.padding(pad)) {
                AnimatedContent(tab, transitionSpec = { (fadeIn(tween(280)) + slideInHorizontally { it / 12 }) togetherWith fadeOut(tween(120)) }, label = "tab") { t ->
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
                        when (t) {
                            0 -> Home(year, { year = it }, prog, exam, pick) { year = it; tab = 1 }
                            1 -> Courses(year, { year = it }, prog, save)
                            2 -> Exams()
                            else -> Tools(year) { year = it }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(splash, exit = fadeOut(tween(600))) { Splash() }
    }
}

@Composable
fun Splash() {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val w by animateDpAsState(if (go) 150.dp else 0.dp, tween(1000, 500), label = "w")
    val a by animateFloatAsState(if (go) 1f else 0f, tween(900), label = "a")
    Box(Modifier.fillMaxSize().background(Indigo), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Vet Prep", color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 52.sp, modifier = Modifier.padding(top = ((1f - a) * 24).dp))
            Box(Modifier.padding(vertical = 10.dp).width(w).height(5.dp).clip(RoundedCornerShape(5.dp)).background(Haldi))
            Text("Know your syllabus. Study smarter.", color = Color(0xFFC9CEF0), modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable fun Title(t: String, s: String) {
    Text(t, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Indigo)
    Text(s, color = Mut, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))
}
@Composable fun H(t: String) = Text(t, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Indigo, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
@Composable fun Panel(content: @Composable ColumnScope.() -> Unit) =
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), RoundedCornerShape(20.dp), CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line)) { Column(Modifier.padding(16.dp), content = content) }

@Composable
fun Chips(y: Int, on: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.spacedBy(8.dp)) {
        (1..4).forEach { n ->
            val sel = n == y
            val bg by animateColorAsState(if (sel) Indigo else Color.White, label = "chip")
            Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(bg).border(1.dp, Line, RoundedCornerShape(14.dp)).clickable { on(n) }.padding(vertical = 10.dp), Alignment.Center) {
                Text("Year $n", color = if (sel) Color.White else Mut, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun Ring(p: Int) {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val a by animateFloatAsState(if (go) p / 100f else 0f, tween(1400, easing = FastOutSlowInEasing), label = "ring")
    Box(Modifier.size(116.dp), Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            drawArc(Color(0x30FFFFFF), -90f, 360f, false, style = Stroke(26f, cap = StrokeCap.Round))
            drawArc(Haldi, -90f, 360f * a, false, style = Stroke(26f, cap = StrokeCap.Round))
        }
        Text("$p%", color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 26.sp)
    }
}

@Composable
fun Home(y: Int, setY: (Int) -> Unit, prog: Map<String, Int>, exam: String, pick: () -> Unit, open: (Int) -> Unit) {
    val c = Years[y - 1]
    val avg = c.sumOf { prog[it.n] ?: 0 } / c.size
    val left = runCatching { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(exam)) }.getOrNull()
    Title("Vet Prep", "Your BVSc & AH guide for all four professional years.")
    Chips(y, setY)
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), RoundedCornerShape(28.dp), CardDefaults.cardColors(containerColor = Indigo)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Ring(avg)
            Column(Modifier.padding(start = 18.dp)) {
                Text("Year $y", color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("${c.size} courses, ${credits(y)} credits. Move the sliders in Courses to track what you have revised.", color = Color(0xFFC9CEF0), fontSize = 14.sp)
            }
        }
    }
    Panel {
        Text("Your next exam date", color = Mut, fontSize = 13.sp)
        Button(pick, colors = ButtonDefaults.buttonColors(containerColor = Haldi, contentColor = Color(0xFF3B2B00))) { Text(if (exam.isEmpty()) "Pick a date" else exam) }
        if (left != null) Text(if (left > 0) "$left days left. Plan your revision backwards from here." else if (left == 0L) "Exam day. You are ready." else "That date has passed. Pick the next one.", fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 8.dp))
    }
    H("The full programme")
    Panel {
        (1..4).forEach { n ->
            Row(Modifier.fillMaxWidth().clickable { open(n) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Year $n", fontWeight = FontWeight.Bold, color = Ink)
                    Text("${Years[n - 1].size} courses" + if (n == 4) ", runs for 1.5 years" else "", color = Mut, fontSize = 13.sp)
                }
                Text("${credits(n)} credits", color = Color(0xFF7A5600), fontWeight = FontWeight.SemiBold)
            }
        }
        Text("Total 81 credits, then a 1-year internship.", color = Mut, fontSize = 13.sp)
    }
}

@Composable
fun Courses(y: Int, setY: (Int) -> Unit, prog: SnapshotStateMap<String, Int>, save: (String, Int) -> Unit) {
    Title("Courses", "Tap a course for credits, books and your revision progress.")
    Chips(y, setY)
    Years[y - 1].forEach { CourseCard(it, prog, save) }
    H("How to study this year")
    Panel { Text(Tips[y - 1], color = Ink) }
    H("How to add your own book")
    Panel {
        listOf("Tap Copy next to the book name.", "Get the book from your college library, a bookstore, or the publisher’s e-book site.", "Keep your PDF in the Downloads folder on your phone.", "Open it in your PDF reader, such as Xodo.").forEachIndexed { i, s ->
            Text("${i + 1}. $s", color = Ink, modifier = Modifier.padding(vertical = 3.dp))
        }
        Text("Book names are commonly used titles. Follow the list your teachers give you.", color = Mut, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun CourseCard(c: Course, prog: SnapshotStateMap<String, Int>, save: (String, Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    var v by remember { mutableFloatStateOf((prog[c.n] ?: 0).toFloat()) }
    val ctx = LocalContext.current
    val cb = LocalClipboardManager.current
    val rot by animateFloatAsState(if (open) 90f else 0f, label = "chev")
    Panel {
        Row(Modifier.fillMaxWidth().clickable { open = !open }, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(c.n, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink)
                Text("${c.t} theory + ${c.p} practical credits", color = Mut, fontSize = 13.sp)
            }
            Text("›", color = Mut, fontSize = 24.sp, modifier = Modifier.rotate(rot))
        }
        AnimatedVisibility(open) {
            Column(Modifier.padding(top = 12.dp)) {
                Text("Revised so far: ${v.toInt()}%", color = Mut, fontSize = 13.sp)
                Slider(v, { v = it; prog[c.n] = it.toInt() }, valueRange = 0f..100f, onValueChangeFinished = { save(c.n, v.toInt()) }, colors = SliderDefaults.colors(thumbColor = Moss, activeTrackColor = Moss))
                Text("Books", color = Mut, fontSize = 13.sp)
                if (c.books.isEmpty()) Text("Book list not confirmed yet. Ask your department which book to follow.", color = Mut, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                c.books.forEach { b ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(b, Modifier.weight(1f).padding(end = 8.dp), fontSize = 14.sp, color = Ink)
                        Button({ cb.setText(AnnotatedString(b)); Toast.makeText(ctx, "Book name copied", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = Haldi, contentColor = Color(0xFF3B2B00))) { Text("Copy") }
                    }
                }
            }
        }
    }
}

@Composable fun Bullets(title: String, items: List<String>) = Panel {
    Text(title, fontWeight = FontWeight.Bold, color = Ink)
    items.forEachIndexed { i, s -> Text("${i + 1}. $s", color = Ink, modifier = Modifier.padding(top = 6.dp)) }
}

@Composable
fun Exams() {
    Title("Exams", "How marks work under the MAFSU 2016 regulations.")
    Bullets("Each course", listOf("3 internal tests of 40 marks. The best two count.", "Annual theory exam: 2 papers of 100 marks, 3 hours each.", "Questions are 40% objective and 60% subjective.", "Annual practical: 2 papers of 60 marks each.", "Weightage: theory 60, practical 40."))
    Bullets("To pass", listOf("50% in theory and 50% in practical, separately.", "75% attendance in theory and in practical, separately.", "Up to 2 failed subjects can go to a compartment exam.", "Grade point is marks divided by 10. Overall grade point must be at least 5.0. A grade of 8.0 or more is Distinction."))
    Text("Always confirm with your college notice board, because rules can be updated.", color = Mut, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
}

@Composable
fun Field(v: String, l: String, m: Modifier = Modifier, on: (String) -> Unit) =
    OutlinedTextField(v, on, m, label = { Text(l) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

fun att(a: String, h: String): Pair<String, Boolean>? {
    val x = a.toDoubleOrNull() ?: return null
    val hd = h.toDoubleOrNull() ?: return null
    if (hd <= 0) return null
    val p = x / hd * 100
    val n = maxOf(0, Math.ceil((0.75 * hd - x) / 0.25).toInt())
    return if (p >= 75) Pair("%.1f%%. Safe.".format(p), true) else Pair("%.1f%%. Attend the next $n classes without a miss.".format(p), false)
}

@Composable
fun Tools(y: Int, setY: (Int) -> Unit) {
    var ta by remember { mutableStateOf("") }; var th by remember { mutableStateOf("") }
    var pa by remember { mutableStateOf("") }; var ph by remember { mutableStateOf("") }
    val marks = remember { mutableStateMapOf<String, String>() }
    Title("Tools", "Check where you stand before it is too late.")
    Panel {
        Text("Attendance (75% needed)", fontWeight = FontWeight.Bold, color = Ink)
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Field(ta, "Theory attended", Modifier.weight(1f)) { ta = it }; Field(th, "Theory held", Modifier.weight(1f)) { th = it }
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Field(pa, "Practical attended", Modifier.weight(1f)) { pa = it }; Field(ph, "Practical held", Modifier.weight(1f)) { ph = it }
        }
        att(ta, th)?.let { Text("Theory: ${it.first}", color = if (it.second) Moss else Bad, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
        att(pa, ph)?.let { Text("Practical: ${it.first}", color = if (it.second) Moss else Bad, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)) }
    }
    Panel {
        Text("Overall grade point for Year $y", fontWeight = FontWeight.Bold, color = Ink)
        Chips(y, setY)
        Text("Enter your percentage for each course.", color = Mut, fontSize = 13.sp)
        var s = 0.0; var cr = 0
        Years[y - 1].forEach { c ->
            Field(marks[c.n] ?: "", "${c.n} (${c.t + c.p} credits)", Modifier.fillMaxWidth().padding(top = 8.dp)) { marks[c.n] = it }
            marks[c.n]?.toDoubleOrNull()?.let { s += it / 10 * (c.t + c.p); cr += c.t + c.p }
        }
        if (cr > 0) {
            val g = s / cr
            Text("Grade point: %.2f  %s".format(g, if (g >= 8) "Distinction" else if (g >= 5) "Pass" else "Below 5.0"), fontWeight = FontWeight.Bold, color = if (g >= 5) Moss else Bad, modifier = Modifier.padding(top = 10.dp))
        }
    }
}
