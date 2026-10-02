package com.example.ui.screens.teacher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkEntity
import com.example.data.model.MockExamEntity
import com.example.data.model.StudentEntity
import com.example.data.model.StudyLogEntity
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(viewModel: MainViewModel) {
    val teacher by viewModel.currentTeacher.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val assignedStudents by viewModel.teacherStudents.collectAsState()

    // Determine available students (prioritize assigned, fallback to all students)
    val studentsList = if (assignedStudents.isNotEmpty()) assignedStudents else allStudents

    val selectedStudent by viewModel.selectedStudent.collectAsState()

    // Auto-select first student if none selected
    if (selectedStudent == null && studentsList.isNotEmpty()) {
        viewModel.selectStudent(studentsList.first())
    }

    val studyLogs by viewModel.selectedStudentStudyLogs.collectAsState()
    val mockExams by viewModel.selectedStudentMockExams.collectAsState()
    val homeworks by viewModel.selectedStudentHomeworks.collectAsState()
    val chatMessages by viewModel.teacherChatMessages.collectAsState()

    // 0: Ders, 1: Deneme, 2: Ödev, 3: Mesaj
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAssignHomeworkDialog by remember { mutableStateOf(false) }

    // Geri tuşuna basıldığında sekmeler arası gezinirken önce ilk sekmeye (Ders Takip) döner
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        topBar = {
            Surface(
                color = SecondaryTeal,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Teacher Info Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${teacher?.ogretmen_ad ?: ""} ${teacher?.ogretmen_soyad ?: ""}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${teacher?.brans ?: ""} • ${teacher?.brans_alanlari ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.testTag("btn_teacher_logout")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Çıkış Yap",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // "ÖNCE ÖĞRENCİ SEÇİLECEK" - Student Selector Chips
                    Text(
                        text = "TAKİP EDİLEN ÖĞRENCİ SEÇİMİ:",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        studentsList.forEach { s ->
                            val isSelected = s.ogrenci_id == selectedStudent?.ogrenci_id
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .clickable { viewModel.selectStudent(s) }
                                    .testTag("select_student_${s.ogrenci_id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) SecondaryTeal else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${s.ogrenci_ad} ${s.ogrenci_soyad}",
                                        color = if (isSelected) SecondaryTeal else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Ders") },
                    label = { Text("Ders") },
                    modifier = Modifier.testTag("nav_teacher_ders")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Grade, contentDescription = "Deneme") },
                    label = { Text("Deneme") },
                    modifier = Modifier.testTag("nav_teacher_deneme")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Ödev") },
                    label = { Text("Ödev") },
                    modifier = Modifier.testTag("nav_teacher_odev")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Mesaj") },
                    label = { Text("Mesaj") },
                    modifier = Modifier.testTag("nav_teacher_mesaj")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 2 && selectedStudent != null) {
                FloatingActionButton(
                    onClick = { showAssignHomeworkDialog = true },
                    containerColor = SecondaryTeal,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_teacher_assign_hw")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ödev Gönder")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedStudent == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Lütfen yukarıdan bir öğrenci seçiniz.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Selected Student Quick Info Banner
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${selectedStudent?.ogrenci_ad} ${selectedStudent?.ogrenci_soyad} (${selectedStudent?.sinif_bilgisi})",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Hedef: ${selectedStudent?.ogrenci_hedef} • ${selectedStudent?.ogrenci_puan?.toInt()} Puan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            if (selectedStudent?.ogrenci_telno?.isNotBlank() == true) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedStudent?.ogrenci_telno ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Active Tab Content
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> TeacherStudentStudyLogsTab(studyLogs)
                            1 -> TeacherStudentMockExamsTab(mockExams)
                            2 -> TeacherHomeworkManagementTab(
                                homeworks = homeworks,
                                onDelete = { viewModel.deleteHomework(it) }
                            )
                            3 -> TeacherStudentChatTab(
                                messages = chatMessages,
                                studentName = "${selectedStudent?.ogrenci_ad} ${selectedStudent?.ogrenci_soyad}",
                                onSendMessage = { viewModel.sendTeacherMessage(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAssignHomeworkDialog && selectedStudent != null) {
        AssignHomeworkDialog(
            studentName = "${selectedStudent?.ogrenci_ad} ${selectedStudent?.ogrenci_soyad}",
            teacherBranch = teacher?.brans ?: "Matematik",
            onDismiss = { showAssignHomeworkDialog = false },
            onAssign = { ders, konu, soru, sonTarih, aciklama ->
                viewModel.assignHomework(
                    studentId = selectedStudent!!.ogrenci_id,
                    ders = ders,
                    konu = konu,
                    soruSayisi = soru,
                    sonTeslimTarihi = sonTarih,
                    aciklama = aciklama
                )
                showAssignHomeworkDialog = false
            }
        )
    }
}

// ----------------- ÖĞRETMEN: DERS TAKİP SEKMESİ (TARİH FİLTRELİ) -----------------
@Composable
fun TeacherStudentStudyLogsTab(logs: List<StudyLogEntity>) {
    // Tarih filtreleri: Tümü, Bugün, Son 7 Gün, Bu Ay
    var dateFilter by remember { mutableStateOf("Tümü") }

    val now = System.currentTimeMillis()
    val oneDay = 86400000L
    val filteredLogs = remember(logs, dateFilter) {
        when (dateFilter) {
            "Bugün" -> logs.filter { (now - it.tarih) < oneDay }
            "Son 7 Gün" -> logs.filter { (now - it.tarih) < (oneDay * 7) }
            "Bu Ay" -> logs.filter { (now - it.tarih) < (oneDay * 30) }
            else -> logs
        }
    }

    val totalQuestions = filteredLogs.sumOf { it.cozulensoru }
    val totalNet = filteredLogs.sumOf { it.net }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date Filter Chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tarihe Göre Filtrele:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Tümü", "Bugün", "Son 7 Gün", "Bu Ay").forEach { f ->
                        FilterChip(
                            selected = dateFilter == f,
                            onClick = { dateFilter = f },
                            label = { Text(f) },
                            modifier = Modifier.testTag("filter_date_$f")
                        )
                    }
                }
            }
        }

        // Summary for filtered period
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Çözülen Soru ($dateFilter)",
                    value = "$totalQuestions Soru",
                    subtitle = "${filteredLogs.size} Çalışma Oturumu",
                    icon = Icons.Default.MenuBook,
                    iconTint = SecondaryTeal,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Toplam Net",
                    value = String.format(Locale.getDefault(), "%.1f Net", totalNet),
                    subtitle = if (filteredLogs.isNotEmpty()) "Ort. ${String.format(Locale.getDefault(), "%.1f", totalNet / filteredLogs.size)}" else "0 Net",
                    icon = Icons.Default.Grade,
                    iconTint = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Bu zaman aralığında çalışma kaydı bulunmuyor.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                val dateStr = SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale("tr")).format(Date(log.tarih))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = SecondaryTeal.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${log.sinav_turu} • ${log.ders}",
                                        color = SecondaryTeal,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Konu: ${log.konu}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Soru", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${log.cozulensoru}", fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Doğru", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${log.dogru}", fontWeight = FontWeight.Bold, color = StatusSuccess)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Yanlış", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${log.yanlis}", fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Net", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    String.format(Locale.getDefault(), "%.2f", log.net),
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }

                        if (log.not.isNotBlank() || log.calismaSuresiDk > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (log.not.isNotBlank()) {
                                    Text("Öğrenci Notu: ${log.not}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                if (log.calismaSuresiDk > 0) {
                                    Text("${log.calismaSuresiDk} dk çalışıldı", style = MaterialTheme.typography.bodySmall, color = AccentAmber)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- ÖĞRETMEN: DENEME TAKİP SEKMESİ (TARİH FİLTRELİ) -----------------
@Composable
fun TeacherStudentMockExamsTab(exams: List<MockExamEntity>) {
    var examFilter by remember { mutableStateOf("Tümü") }

    val filteredExams = remember(exams, examFilter) {
        when (examFilter) {
            "TYT" -> exams.filter { it.sinav_turu == "TYT" }
            "AYT" -> exams.filter { it.sinav_turu == "AYT" }
            else -> exams
        }
    }

    val maxNet = filteredExams.maxOfOrNull { it.net } ?: 0.0
    val lastNet = filteredExams.firstOrNull()?.net ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Tümü", "TYT", "AYT").forEach { f ->
                    FilterChip(
                        selected = examFilter == f,
                        onClick = { examFilter = f },
                        label = { Text(f) }
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Deneme Sayısı",
                    value = "${filteredExams.size} Sınav",
                    subtitle = "Son Net: ${String.format(Locale.getDefault(), "%.2f", lastNet)}",
                    icon = Icons.Default.Grade,
                    iconTint = SecondaryTeal,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "En Yüksek Net",
                    value = String.format(Locale.getDefault(), "%.2f", maxNet),
                    subtitle = "Maksimum net seviyesi",
                    icon = Icons.Default.Star,
                    iconTint = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (filteredExams.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Grade, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Henüz bu kategoride deneme sınavı kaydı yok.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
        } else {
            items(filteredExams, key = { it.id }) { exam ->
                val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(Date(exam.tarih))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(
                                    color = PrimaryBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${exam.sinav_turu} • ${exam.yayin_adi}",
                                        color = PrimaryBlue,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exam.deneme_adi,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Doğru", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${exam.dogru}", fontWeight = FontWeight.Bold, color = StatusSuccess)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Yanlış", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${exam.yanlis}", fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Net", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    String.format(Locale.getDefault(), "%.2f", exam.net),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue,
                                    fontSize = 18.sp
                                )
                            }
                            if (exam.puan > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Puan", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("${exam.puan.toInt()}", fontWeight = FontWeight.Bold, color = AccentAmber)
                                }
                            }
                        }

                        if (exam.not.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Öğrenci Notu: ${exam.not}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ----------------- ÖĞRETMEN: ÖDEV YÖNETİMİ SEKMESİ -----------------
@Composable
fun TeacherHomeworkManagementTab(
    homeworks: List<HomeworkEntity>,
    onDelete: (Long) -> Unit
) {
    val completedCount = homeworks.count { it.durum == HomeworkEntity.STATUS_BITIRILDI }
    val seenCount = homeworks.count { it.durum == HomeworkEntity.STATUS_GORULDU }
    val pendingCount = homeworks.count { it.durum == HomeworkEntity.STATUS_BEKLEMEDE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ödev Durumu Genel Özeti",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$pendingCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AccentAmber)
                            Text("Beklemede", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$seenCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryBlue)
                            Text("Görüldü", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$completedCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StatusSuccess)
                            Text("Bitirdi", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }

        if (homeworks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Bu öğrenciye henüz ödev verilmedi.", fontWeight = FontWeight.Bold)
                        Text("+ butonuna basarak yeni bir ders ve konu ödevi gönderin.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            items(homeworks, key = { it.id }) { hw ->
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(Date(hw.olusturmaTarihi))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = hw.ders,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SecondaryTeal,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = hw.konu,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = hw.durum)
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { onDelete(hw.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Ödevi Sil", tint = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Verilen Soru: ${hw.soruSayisi} Soru",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (hw.aciklama.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Açıklama: ${hw.aciklama}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (hw.sonTeslimTarihi.isNotBlank()) {
                                Text("Son Teslim: ${hw.sonTeslimTarihi}", style = MaterialTheme.typography.labelSmall, color = AccentAmber, fontWeight = FontWeight.Bold)
                            }
                            Text("Veriliş: $dateStr", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }

                        // Detailed status timestamps
                        if (hw.durum == HomeworkEntity.STATUS_BITIRILDI && hw.bitirmeTarihi != null) {
                            val doneStr = SimpleDateFormat("dd MMM HH:mm", Locale("tr")).format(Date(hw.bitirmeTarihi))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("✓ Öğrenci bu ödevi $doneStr tarihinde BİTİRDİ.", color = StatusSuccess, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        } else if (hw.durum == HomeworkEntity.STATUS_GORULDU && hw.gorulmeTarihi != null) {
                            val seenStr = SimpleDateFormat("dd MMM HH:mm", Locale("tr")).format(Date(hw.gorulmeTarihi))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("👁 Öğrenci bu ödevi $seenStr tarihinde GÖRDÜ.", color = PrimaryBlue, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// ----------------- ÖĞRETMEN: MESAJLAŞMA SEKMESİ -----------------
@Composable
fun TeacherStudentChatTab(
    messages: List<ChatMessageEntity>,
    studentName: String,
    onSendMessage: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Student Info in Chat Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SecondaryTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(text = "Öğrenci ile Birebir Chat & Rehberlik", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Henüz mesajlaşma geçmişi yok. Öğrencinize ilk mesajı yazarak rehberlik edin!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isMe = msg.gonderenRol == "OGRETMEN"
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.zamanDamgasi))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) SecondaryTeal else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isMe) "Ben" else msg.gonderenAdi,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) Color.White.copy(alpha = 0.8f) else SecondaryTeal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.mesaj,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timeStr,
                                color = if (isMe) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Input bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Öğrenciye mesajınızı iletin...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("teacher_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SecondaryTeal)
                        .testTag("btn_teacher_send_msg")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ----------------- ÖDEV ATAMA DİYALOĞU -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignHomeworkDialog(
    studentName: String,
    teacherBranch: String,
    onDismiss: () -> Unit,
    onAssign: (ders: String, konu: String, soru: Int, sonTarih: String, aciklama: String) -> Unit
) {
    var ders by remember { mutableStateOf(teacherBranch) }
    var konu by remember { mutableStateOf("") }
    var soruStr by remember { mutableStateOf("80") }
    var sonTarih by remember { mutableStateOf("15 Ekim 2026") }
    var aciklama by remember { mutableStateOf("") }

    val dersOptions = listOf("Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji", "Türkçe & Edebiyat", "Tarih", "Coğrafya")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Ödev Gönder", fontWeight = FontWeight.Bold)
                Text("Öğrenci: $studentName", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var dersExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = dersExpanded,
                    onExpandedChange = { dersExpanded = !dersExpanded }
                ) {
                    OutlinedTextField(
                        value = ders,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ders") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dersExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dersExpanded,
                        onDismissRequest = { dersExpanded = false }
                    ) {
                        dersOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { ders = opt; dersExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = konu,
                    onValueChange = { konu = it },
                    label = { Text("Konu (örn. İntegral Alan)") },
                    modifier = Modifier.fillMaxWidth().testTag("assign_hw_konu")
                )

                OutlinedTextField(
                    value = soruStr,
                    onValueChange = { soruStr = it },
                    label = { Text("Hedef Soru Sayısı") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("assign_hw_soru")
                )

                OutlinedTextField(
                    value = sonTarih,
                    onValueChange = { sonTarih = it },
                    label = { Text("Son Teslim Tarihi") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = aciklama,
                    onValueChange = { aciklama = it },
                    label = { Text("Öğretmen Açıklaması / Talimat") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = soruStr.toIntOrNull() ?: 50
                    val topic = if (konu.isBlank()) "Konu Tekrarı" else konu.trim()
                    onAssign(ders, topic, s, sonTarih, aciklama)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                modifier = Modifier.testTag("btn_confirm_assign_hw")
            ) {
                Text("Ödevi Gönder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
