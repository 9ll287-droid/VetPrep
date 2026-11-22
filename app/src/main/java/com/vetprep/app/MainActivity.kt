package com.vetprep.app

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val Indigo = Color(0xFF1F2A6B); val Haldi = Color(0xFFF2B01E); val Chalk = Color(0xFFF4F5FA)
val Moss = Color(0xFF2E7D5B); val Mut = Color(0xFF5D6385); val Line = Color(0xFFDFE2EF)
val Ink = Color(0xFF14172B); val Bad = Color(0xFFB3261E); val Soft = Color(0xFFC9CEF0)

data class Course(val n: String, val t: Int, val p: Int, val books: List<String> = emptyList())
data class Doc(val course: String, val name: String, val uri: String)

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
        MobileAds.initialize(this) {}
        val billing = Billing(this)
        billing.start()
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Indigo, background = Chalk)) { App(billing) } }
    }
}

fun credits(y: Int) = Years[y - 1].sumOf { it.t + it.p }
fun openPdf(ctx: Context, d: Doc) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(d.uri), "application/pdf").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) { Toast.makeText(ctx, "No PDF reader found. Install any PDF reader app.", Toast.LENGTH_LONG).show() }
}

@Composable
fun App(billing: Billing) {
    val ctx = LocalContext.current
    val sp = remember { ctx.getSharedPreferences("vp", Context.MODE_PRIVATE) }
    val prog = remember { mutableStateMapOf<String, Int>().apply { Years.flatten().forEach { put(it.n, sp.getInt(it.n, 0)) } } }
    val docs = remember { mutableStateListOf<Doc>().apply { (sp.getString("docs", "") ?: "").split("\n").forEach { l -> val f = l.split("\t"); if (f.size == 3) add(Doc(f[0], f[1], f[2])) } } }
    val tasks = remember { mutableStateListOf<String>().apply { (sp.getString("tasks", "") ?: "").split("\n").filter { it.length > 2 }.forEach { add(it) } } }
    val streak = remember {
        val today = LocalDate.now().toString(); val last = sp.getString("last", "") ?: ""; var s = sp.getInt("streak", 0)
        if (last != today) { s = if (last == LocalDate.now().minusDays(1).toString()) s + 1 else 1; sp.edit().putString("last", today).putInt("streak", s).apply() }
        s
    }
    var exam by remember { mutableStateOf(sp.getString("exam", "") ?: "") }
    var tab by remember { mutableIntStateOf(0) }
    var year by remember { mutableIntStateOf(sp.getInt("year", 1)) }
    var onb by remember { mutableStateOf(sp.getBoolean("onb", false)) }
    var splash by remember { mutableStateOf(true) }
    var target by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { delay(1200); splash = false }
    val setY: (Int) -> Unit = { year = it; sp.edit().putInt("year", it).apply() }
    val save: (String, Int) -> Unit = { k, v -> sp.edit().putInt(k, v).apply() }
    val saveTasks = { sp.edit().putString("tasks", tasks.joinToString("\n")).apply() }
    val saveDocs = { sp.edit().putString("docs", docs.joinToString("\n") { "${it.course}\t${it.name}\t${it.uri}" }).apply() }
    val cards = remember { mutableStateListOf<Flash>().apply { (sp.getString("cards", "") ?: "").split("\n").forEach { l -> val f = l.split("\t"); if (f.size == 6) add(Flash(f[0].toLong(), f[1], f[2], f[3].toLong(), f[4].toInt(), f[5].toDouble())) } } }
    val saveCards = { sp.edit().putString("cards", cards.joinToString("\n") { "${it.id}\t${it.front}\t${it.back}\t${it.due}\t${it.gap}\t${it.ease}" }).apply() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            val name = ctx.contentResolver.query(uri, null, null, null, null)?.use { c -> if (c.moveToFirst()) c.getString(c.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else null } ?: "PDF"
            docs.add(Doc(target, name.replace("\t", " "), uri.toString())); saveDocs()
        }
    }
    val pick = {
        val d = LocalDate.now()
        DatePickerDialog(ctx, { _, y, m, day -> exam = LocalDate.of(y, m + 1, day).toString(); sp.edit().putString("exam", exam).apply() }, d.year, d.monthValue - 1, d.dayOfMonth).show()
    }
    var sub by remember { mutableIntStateOf(0) }
    val dueCards = cards.count { it.due <= LocalDate.now().toEpochDay() }
    val names = listOf("Home", "Books", "Cards", "Info")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.Create, Icons.Filled.Info)
    val heads = listOf("Vet Prep" to "", "Books" to "Your PDFs, one tap away.", "Cards" to "Revise at the right time.", "Info" to "Syllabus, exams and tools.")
    Box {
        if (!onb) Onboarding { y -> setY(y); sp.edit().putBoolean("onb", true).apply(); onb = true }
        else Scaffold(containerColor = Chalk, bottomBar = {
            Column {
                NavigationBar(containerColor = Color.White) {
                    names.forEachIndexed { i, n ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icons[i], null) }, label = { Text(n, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Indigo, selectedTextColor = Indigo, indicatorColor = Haldi.copy(alpha = 0.4f), unselectedIconColor = Mut, unselectedTextColor = Mut))
                    }
                }
                Banner()
            }
        }) { pad ->
            Box(Modifier.padding(pad)) {
                AnimatedContent(tab, transitionSpec = { (fadeIn(tween(250)) + slideInHorizontally { it / 12 }) togetherWith fadeOut(tween(100)) }, label = "tab") { t ->
                    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                        Title(heads[t].first, heads[t].second)
                        if (t == 3) Segmented(listOf("Courses", "Exams", "Tools"), sub) { sub = it }
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            when (t) {
                                0 -> Home(year, prog, exam, pick, streak, tasks, saveTasks, dueCards) { tab = 2 }
                                1 -> Library(year, setY, docs, { target = it; picker.launch(arrayOf("application/pdf")) }, { d, n -> val i = docs.indexOf(d); if (i >= 0) { docs[i] = d.copy(name = n.replace("	", " ")); saveDocs() } }) { docs.remove(it); saveDocs() }
                                2 -> Cards(cards, saveCards, billing)
                                else -> when (sub) { 0 -> Courses(year, setY, prog, save); 1 -> Exams(); else -> Tools(year, setY) }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
        AnimatedVisibility(splash, exit = fadeOut(tween(350))) { Splash() }
    }
}

@Composable
fun Banner() {
    Box(Modifier.fillMaxWidth().background(Color.White), Alignment.Center) {
        AndroidView(factory = { c -> AdView(c).apply { setAdSize(AdSize.BANNER); adUnitId = "ca-app-pub-3940256099942544/9214589741"; loadAd(AdRequest.Builder().build()) } }, modifier = Modifier.height(50.dp))
    }
}

@Composable
fun Splash() {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val w by animateDpAsState(if (go) 150.dp else 0.dp, tween(500, 150), label = "w")
    val a by animateFloatAsState(if (go) 1f else 0f, tween(500), label = "a")
    Box(Modifier.fillMaxSize().background(Indigo), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Vet Prep", color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 52.sp, modifier = Modifier.padding(top = ((1f - a) * 24).dp))
            Box(Modifier.padding(vertical = 10.dp).width(w).height(5.dp).clip(RoundedCornerShape(5.dp)).background(Haldi))
            Text("Plan. Revise. Pass.", color = Soft, fontSize = 16.sp)
        }
    }
}

@Composable
fun Onboarding(done: (Int) -> Unit) {
    val pager = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()
    val titles = listOf("Your whole degree, in one place", "Study in focused sessions", "Keep every book with you", "Which year are you in?")
    val bodies = listOf("All four years, every course and credit, organised the way MAFSU teaches them.", "Set your exam date, run a 25-minute timer and tick off today’s tasks.", "Add the PDFs you download and open them from one shelf.", "We will open the right courses for you.")
    Column(Modifier.fillMaxSize().background(Indigo).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(pager, Modifier.weight(1f)) { p ->
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Box(Modifier.height(130.dp), Alignment.Center) {
                    when (p) {
                        0 -> Ring(72)
                        1 -> Text("25:00", color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 46.sp)
                        2 -> Text("PDF", color = Haldi, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 52.sp)
                        else -> Text("1 2 3 4", color = Haldi, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 40.sp)
                    }
                }
                Text(titles[p], color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 20.dp))
                Text(bodies[p], color = Soft, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
                if (p == 3) (1..4).forEach { n ->
                    Button({ done(n) }, Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Haldi, contentColor = Color(0xFF3B2B00))) { Text("Year $n") }
                }
            }
        }
        Row(Modifier.padding(vertical = 12.dp)) {
            repeat(4) { i ->
                val c by animateColorAsState(if (pager.currentPage == i) Haldi else Color(0x55FFFFFF), label = "dot")
                Box(Modifier.padding(4.dp).size(10.dp).clip(CircleShape).background(c))
            }
        }
        if (pager.currentPage < 3) Button({ scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Indigo)) { Text("Next") } else Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun Title(t: String, s: String) {
    Column(Modifier.fillMaxWidth().background(Chalk).padding(top = 16.dp, bottom = 8.dp)) {
        Text(t, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Indigo)
        if (s.isNotEmpty()) Text(s, color = Mut, fontSize = 14.sp)
    }
}
@Composable fun H(t: String) = Text(t, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Indigo, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
@Composable fun Panel(content: @Composable ColumnScope.() -> Unit) =
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), RoundedCornerShape(20.dp), CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line)) { Column(Modifier.padding(16.dp), content = content) }
@Composable fun HaldiButton(t: String, on: () -> Unit) = Button(on, colors = ButtonDefaults.buttonColors(containerColor = Haldi, contentColor = Color(0xFF3B2B00))) { Text(t) }

@Composable
fun YearPicker(y: Int, on: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val rot by animateFloatAsState(if (open) 180f else 0f, label = "yr")
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, Line, RoundedCornerShape(16.dp)).animateContentSize()) {
        Row(Modifier.fillMaxWidth().clickable { open = !open }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Year $y", Modifier.weight(1f), color = Indigo, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("${credits(y)} credits", color = Mut, fontSize = 13.sp, modifier = Modifier.padding(end = 6.dp))
            Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.rotate(rot), tint = Mut)
        }
        if (open) (1..4).forEach { n ->
            Row(Modifier.fillMaxWidth().background(if (n == y) Haldi.copy(alpha = 0.25f) else Color.Transparent).clickable { on(n); open = false }.padding(14.dp)) {
                Text("Year $n", Modifier.weight(1f), color = Ink)
                Text("${Years[n - 1].size} courses", color = Mut, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun Ring(p: Int, fg: Color = Color.White, track: Color = Color(0x30FFFFFF)) {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val a by animateFloatAsState(if (go) p / 100f else 0f, tween(1200, easing = FastOutSlowInEasing), label = "ring")
    Box(Modifier.size(104.dp), Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            drawArc(track, -90f, 360f, false, style = Stroke(24f, cap = StrokeCap.Round))
            drawArc(Haldi, -90f, 360f * a, false, style = Stroke(24f, cap = StrokeCap.Round))
        }
        Text("$p%", color = fg, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

@Composable
fun Home(y: Int, prog: Map<String, Int>, exam: String, pick: () -> Unit, streak: Int, tasks: SnapshotStateList<String>, saveTasks: () -> Unit, due: Int, goCards: () -> Unit) {
    val ctx = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(10000) } }
    val next = Years[y - 1].minByOrNull { prog[it.n] ?: 0 }
    val left = runCatching { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(exam)) }.getOrNull()
    var secs by remember { mutableIntStateOf(25 * 60) }
    var run by remember { mutableStateOf(false) }
    var t by remember { mutableStateOf("") }
    L
