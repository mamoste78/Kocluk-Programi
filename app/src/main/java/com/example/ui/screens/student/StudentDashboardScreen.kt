package com.example.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.StudyLogEntity
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.SimpleLineChart
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
fun StudentDashboardScreen(viewModel: MainViewModel) {
    val student by viewModel.currentStudent.collectAsState()
    val teachers by viewModel.allTeachers.collectAsState()

    val studyLogs by viewModel.studentStudyLogs.collectAsState()
    val mockExams by viewModel.studentMockExams.collectAsState()
    val homeworks by viewModel.studentHomeworks.collectAsState()
    val chatMessages by viewModel.studentChatMessages.collectAsState()

    // 0: Ders, 1: Deneme, 2: Ödev, 3: Mesaj, 4: Analiz
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state for adding study log
    var showAddStudyDialog by remember { mutableStateOf(false) }
    // Dialog state for adding mock exam
    var showAddMockExamDialog by remember { mutableStateOf(false) }

    val assignedTeacher = teachers.find { it.ogretmen_id == student?.secilen_ogretmen_id }

    // Geri tuşuna basıldığında sekmeler arası gezinirken önce ilk sekmeye (Ders Takip) döner
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    // When entering the Homework tab, mark any "BEKLEMEDE" homework as "GORULDU" automatically!
    LaunchedEffect(selectedTab, homeworks) {
        if (selectedTab == 2) {
            homeworks.filter { it.durum == HomeworkEntity.STATUS_BEKLEMEDE }.forEach { hw ->
                viewModel.markHomeworkSeen(hw.id)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = PrimaryNavy,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
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
                                    text = "${student?.ogrenci_ad ?: ""} ${student?.ogrenci_soyad ?: ""}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${student?.sinif_bilgisi ?: ""} • Hedef: ${student?.ogrenci_hedef ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.testTag("btn_student_logout")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Çıkış Yap",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Assigned teacher pill
                    if (assignedTeacher != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Danışman: ${assignedTeacher.ogretmen_ad} ${assignedTeacher.ogretmen_soyad} (${assignedTeacher.brans})",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
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
                    modifier = Modifier.testTag("nav_student_ders")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Grade, contentDescription = "Deneme") },
                    label = { Text("Deneme") },
                    modifier = Modifier.testTag("nav_student_deneme")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Ödev") },
                    label = { Text("Ödev") },
                    modifier = Modifier.testTag("nav_student_odev")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Mesaj") },
                    label = { Text("Mesaj") },
                    modifier = Modifier.testTag("nav_student_mesaj")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analiz") },
                    label = { Text("Analiz") },
                    modifier = Modifier.testTag("nav_student_analiz")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddStudyDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_study")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ders Çalışması Ekle")
                }
            } else if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddMockExamDialog = true },
                    containerColor = SecondaryTeal,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_mock_exam")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Deneme Ekle")
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
            when (selectedTab) {
                0 -> StudentStudyLogsTab(studyLogs, onDelete = { viewModel.deleteStudyLog(it) })
                1 -> StudentMockExamsTab(mockExams, onDelete = { viewModel.deleteMockExam(it) })
                2 -> StudentHomeworkTab(homeworks, onMarkDone = { viewModel.markHomeworkDone(it) })
                3 -> StudentChatTab(
                    messages = chatMessages,
                    teacherName = assignedTeacher?.let { "${it.ogretmen_ad} ${it.ogretmen_soyad}" } ?: "Öğretmeniniz",
                    onSendMessage = { viewModel.sendStudentMessage(it) }
                )
                4 -> StudentAnalyticsTab(
                    student = student,
                    studyLogs = studyLogs,
                    mockExams = mockExams
                )
            }
        }
    }

    if (showAddStudyDialog) {
        AddStudyLogDialog(
            onDismiss = { showAddStudyDialog = false },
            onAdd = { sinav, ders, konu, soru, d, y, net, sure, not ->
                viewModel.addStudyLog(sinav, ders, konu, soru, d, y, net, sure, not)
                showAddStudyDialog = false
            }
        )
    }

    if (showAddMockExamDialog) {
        AddMockExamDialog(
            onDismiss = { showAddMockExamDialog = false },
            onAdd = { deneme, yayin, sinav, d, y, net, puan, not ->
                viewModel.addMockExam(deneme, yayin, sinav, d, y, net, puan, not)
                showAddMockExamDialog = false
            }
        )
    }
}

// ----------------- TAB 1: DERS TAKİP -----------------
@Composable
fun StudentStudyLogsTab(
    logs: List<StudyLogEntity>,
    onDelete: (Long) -> Unit
) {
    val totalQuestions = logs.sumOf { it.cozulensoru }
    val totalNet = logs.sumOf { it.net }
    val avgNet = if (logs.isNotEmpty()) totalNet / logs.size else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Toplam Soru",
                    value = "$totalQuestions",
                    subtitle = "${logs.size} Çalışma Kaydı",
                    icon = Icons.Default.EditNote,
                    iconTint = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Ortalama Net",
                    value = String.format(Locale.getDefault(), "%.1f", avgNet),
                    subtitle = "Toplam: ${String.format(Locale.getDefault(), "%.1f", totalNet)} Net",
                    icon = Icons.Default.Grade,
                    iconTint = SecondaryTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Henüz ders çalışma kaydınız yok.", fontWeight = FontWeight.Bold)
                        Text("+ butonuna basarak ilk çalışma kaydınızı ekleyin.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                val dateStr = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale("tr")).format(Date(log.tarih))
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
                                    color = PrimaryBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = log.sinav_turu,
                                        color = PrimaryBlue,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = log.ders,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { onDelete(log.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Konu: ${log.konu}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
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
                                if (log.calismaSuresiDk > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${log.calismaSuresiDk} dk", style = MaterialTheme.typography.bodySmall, color = AccentAmber)
                                    }
                                }
                                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- TAB 2: DENEME TAKİP -----------------
@Composable
fun StudentMockExamsTab(
    exams: List<MockExamEntity>,
    onDelete: (Long) -> Unit
) {
    val totalCount = exams.size
    val maxNet = exams.maxOfOrNull { it.net } ?: 0.0
    val lastNet = exams.firstOrNull()?.net ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Deneme Sayısı",
                    value = "$totalCount Adet",
                    subtitle = "Son Net: ${String.format(Locale.getDefault(), "%.2f", lastNet)}",
                    icon = Icons.Default.Grade,
                    iconTint = SecondaryTeal,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Zirve Net",
                    value = String.format(Locale.getDefault(), "%.2f", maxNet),
                    subtitle = "En yüksek başarı",
                    icon = Icons.Default.Star,
                    iconTint = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (exams.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Grade, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Henüz deneme kaydı yok.", fontWeight = FontWeight.Bold)
                        Text("+ butonuna basarak ilk denemenizi kaydedin.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            items(exams, key = { it.id }) { exam ->
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
                                    color = SecondaryTeal.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${exam.sinav_turu} • ${exam.yayin_adi}",
                                        color = SecondaryTeal,
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

                            IconButton(onClick = { onDelete(exam.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Doğru", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${exam.dogru}", fontWeight = FontWeight.Bold, color = StatusSuccess, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Yanlış", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${exam.yanlis}", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 16.sp)
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
                                    Text("${exam.puan.toInt()}", fontWeight = FontWeight.Bold, color = AccentAmber, fontSize = 16.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (exam.not.isNotBlank()) {
                                Text("Not: ${exam.not}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ----------------- TAB 3: ÖDEV TAKİP -----------------
@Composable
fun StudentHomeworkTab(
    homeworks: List<HomeworkEntity>,
    onMarkDone: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val completedCount = homeworks.count { it.durum == HomeworkEntity.STATUS_BITIRILDI }
            StatCard(
                title = "Ödev Durumu",
                value = "$completedCount / ${homeworks.size} Tamamlandı",
                subtitle = "Öğretmeninizin size tanımladığı çalışma görevleri",
                icon = Icons.Default.Assignment,
                iconTint = PrimaryBlue,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (homeworks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Henüz size atanmış ödev bulunmuyor.", fontWeight = FontWeight.Bold)
                        Text("Öğretmeniniz ödev atadığında burada görünecektir.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            items(homeworks, key = { it.id }) { hw ->
                val isDone = hw.durum == HomeworkEntity.STATUS_BITIRILDI

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = hw.ders,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = hw.konu,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            StatusBadge(status = hw.durum)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hedef Soru: ${hw.soruSayisi} Soru",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (hw.aciklama.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Öğretmen Notu: ${hw.aciklama}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (hw.sonTeslimTarihi.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Son Teslim: ${hw.sonTeslimTarihi}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Completion action row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    if (!isDone) onMarkDone(hw.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isDone,
                                onCheckedChange = { checked ->
                                    if (checked) onMarkDone(hw.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = StatusSuccess),
                                modifier = Modifier.testTag("checkbox_hw_${hw.id}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDone) "Ödevi Başarıyla Bitirdiniz! (Sisteme Kaydedildi)" else "Ödevi Tamamladım Olarak İşaretle",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDone) StatusSuccess else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------- TAB 4: MESAJLAŞMA -----------------
@Composable
fun StudentChatTab(
    messages: List<ChatMessageEntity>,
    teacherName: String,
    onSendMessage: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Teacher Banner in Chat
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
                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = teacherName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(text = "Hızlı İletişim & Soru Danışma", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                            text = "Henüz mesaj yok. Öğretmeninize bir soru ileterek başlayın!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isMe = msg.gonderenRol == "OGRENCI"
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
                            containerColor = if (isMe) PrimaryBlue else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isMe) "Ben" else msg.gonderenAdi,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) Color.White.copy(alpha = 0.8f) else PrimaryBlue
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
                    placeholder = { Text("Öğretmeninize mesaj yazın...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("student_chat_input"),
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
                        .background(PrimaryBlue)
                        .testTag("btn_student_send_msg")
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

// ----------------- TAB 5: ANALİZ & DETAYLI RAPORLAMA -----------------
@Composable
fun StudentAnalyticsTab(
    student: com.example.data.model.StudentEntity?,
    studyLogs: List<StudyLogEntity>,
    mockExams: List<MockExamEntity>
) {
    val totalQuestions = studyLogs.sumOf { it.cozulensoru }
    val totalCorrect = studyLogs.sumOf { it.dogru }
    val accuracy = if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions * 100) else 0f

    val totalStudyHours = studyLogs.sumOf { it.calismaSuresiDk } / 60f

    // Subject breakdown for Bar Chart
    val subjectMap = studyLogs.groupBy { it.ders }.mapValues { entry ->
        entry.value.sumOf { it.cozulensoru }.toFloat()
    }.toList()

    // Mock exam net progression
    val examNets = mockExams.reversed().map { it.net.toFloat() }
    val examLabels = mockExams.reversed().map {
        if (it.deneme_adi.length > 8) it.deneme_adi.take(7) + ".." else it.deneme_adi
    }

    val targetScore = student?.ogrenci_puan ?: 450.0
    val lastMockNet = mockExams.firstOrNull()?.net ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Target Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HEDEF GELİŞİMİ",
                            color = AccentGold,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = student?.ogrenci_hedef ?: "Hedef Belirlenmedi",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Hedef: ${targetScore.toInt()} Puan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                val targetNetEstimate = (targetScore / 500.0 * 120.0).coerceAtMost(120.0)
                val progressRatio = if (targetNetEstimate > 0) (lastMockNet / targetNetEstimate).toFloat().coerceIn(0f, 1f) else 0f

                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = AccentGold,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Son Net: ${String.format(Locale.getDefault(), "%.1f", lastMockNet)} Net",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Hedeflenen: ~${String.format(Locale.getDefault(), "%.1f", targetNetEstimate)} Net (%${(progressRatio * 100).toInt()})",
                        color = AccentGold,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3 Key Stats
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "Toplam Soru",
                value = "$totalQuestions",
                icon = Icons.Default.EditNote,
                iconTint = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                title = "Doğruluk Oranı",
                value = "%${String.format(Locale.getDefault(), "%.0f", accuracy)}",
                icon = Icons.Default.CheckCircle,
                iconTint = StatusSuccess,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                title = "Çalışma Süresi",
                value = "${String.format(Locale.getDefault(), "%.1f", totalStudyHours)} sa",
                icon = Icons.Default.Timer,
                iconTint = AccentAmber,
                modifier = Modifier.weight(1f)
            )
        }

        // Chart 1: Subject Question Distribution
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 Derslere Göre Soru Dağılımı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ders bazında çözülen toplam soru sayıları",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(14.dp))
                SimpleBarChart(
                    items = subjectMap,
                    barColor = PrimaryBlue,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Chart 2: Mock Exam Trend
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📈 Deneme Sınavı Net Gelişimi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zaman içindeki deneme net trendiniz",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(14.dp))
                SimpleLineChart(
                    values = examNets,
                    labels = examLabels,
                    lineColor = SecondaryTeal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ----------------- DERS ÇALIŞMASI EKLEME DİYALOĞU -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudyLogDialog(
    onDismiss: () -> Unit,
    onAdd: (sinav: String, ders: String, konu: String, soru: Int, d: Int, y: Int, net: Double, sure: Int, not: String) -> Unit
) {
    var sinavTuru by remember { mutableStateOf("AYT") }
    var ders by remember { mutableStateOf("Matematik") }
    var konu by remember { mutableStateOf("") }
    var soruStr by remember { mutableStateOf("50") }
    var dogruStr by remember { mutableStateOf("42") }
    var yanlisStr by remember { mutableStateOf("5") }
    var sureStr by remember { mutableStateOf("60") }
    var not by remember { mutableStateOf("") }

    val sinavOptions = listOf("TYT", "AYT", "LGS", "YKS", "MSÜ", "KPSS")
    val dersOptions = listOf("Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji", "Türkçe", "Tarih", "Coğrafya", "Felsefe")

    val d = dogruStr.toIntOrNull() ?: 0
    val y = yanlisStr.toIntOrNull() ?: 0
    val calculatedNet = (d - (y * 0.25)).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Ders Çalışması Ekle", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sınav Türü
                var sinavExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sinavExpanded,
                    onExpandedChange = { sinavExpanded = !sinavExpanded }
                ) {
                    OutlinedTextField(
                        value = sinavTuru,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sınav Türü") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sinavExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sinavExpanded,
                        onDismissRequest = { sinavExpanded = false }
                    ) {
                        sinavOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { sinavTuru = opt; sinavExpanded = false })
                        }
                    }
                }

                // Ders Seçimi
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
                    label = { Text("Konu (örn. Türev, Paragraf)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_study_konu")
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = soruStr,
                        onValueChange = { soruStr = it },
                        label = { Text("Soru Sayısı") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_study_soru")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = sureStr,
                        onValueChange = { sureStr = it },
                        label = { Text("Süre (dk)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dogruStr,
                        onValueChange = { dogruStr = it },
                        label = { Text("Doğru") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_study_dogru")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = yanlisStr,
                        onValueChange = { yanlisStr = it },
                        label = { Text("Yanlış") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_study_yanlis")
                    )
                }

                // Otomatik Net Göstergesi
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hesaplanan Net:", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f Net", calculatedNet),
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            fontSize = 16.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = not,
                    onValueChange = { not = it },
                    label = { Text("Not (İsteğe bağlı)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = soruStr.toIntOrNull() ?: 0
                    val sure = sureStr.toIntOrNull() ?: 0
                    val topic = if (konu.isBlank()) "Genel Tekrar" else konu.trim()
                    onAdd(sinavTuru, ders, topic, s, d, y, calculatedNet, sure, not)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("btn_confirm_study")
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

// ----------------- DENEME EKLEME DİYALOĞU -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMockExamDialog(
    onDismiss: () -> Unit,
    onAdd: (deneme: String, yayin: String, sinav: String, d: Int, y: Int, net: Double, puan: Double, not: String) -> Unit
) {
    var denemeAdi by remember { mutableStateOf("") }
    var yayinAdi by remember { mutableStateOf("") }
    var sinavTuru by remember { mutableStateOf("TYT") }
    var dogruStr by remember { mutableStateOf("95") }
    var yanlisStr by remember { mutableStateOf("12") }
    var puanStr by remember { mutableStateOf("430") }
    var not by remember { mutableStateOf("") }

    val sinavOptions = listOf("TYT", "AYT", "LGS", "YKS", "MSÜ", "KPSS")

    val d = dogruStr.toIntOrNull() ?: 0
    val y = yanlisStr.toIntOrNull() ?: 0
    val calculatedNet = (d - (y * 0.25)).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Deneme Sınavı Ekle", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = denemeAdi,
                    onValueChange = { denemeAdi = it },
                    label = { Text("Deneme Adı (örn. 3D TYT-2)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_mock_name")
                )

                OutlinedTextField(
                    value = yayinAdi,
                    onValueChange = { yayinAdi = it },
                    label = { Text("Yayın Adı (örn. 3D, Bilgi Sarmal)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_mock_publisher")
                )

                var sinavExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sinavExpanded,
                    onExpandedChange = { sinavExpanded = !sinavExpanded }
                ) {
                    OutlinedTextField(
                        value = sinavTuru,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sınav Türü") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sinavExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sinavExpanded,
                        onDismissRequest = { sinavExpanded = false }
                    ) {
                        sinavOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { sinavTuru = opt; sinavExpanded = false })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dogruStr,
                        onValueChange = { dogruStr = it },
                        label = { Text("Doğru") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_mock_dogru")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = yanlisStr,
                        onValueChange = { yanlisStr = it },
                        label = { Text("Yanlış") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_mock_yanlis")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = puanStr,
                        onValueChange = { puanStr = it },
                        label = { Text("Puan (İsteğe bağlı)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = SecondaryTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hesaplanan Net:", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f Net", calculatedNet),
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTeal,
                            fontSize = 16.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = not,
                    onValueChange = { not = it },
                    label = { Text("Not / İzlenim") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = puanStr.toDoubleOrNull() ?: 0.0
                    val name = if (denemeAdi.isBlank()) "Deneme Sınavı" else denemeAdi.trim()
                    val pub = if (yayinAdi.isBlank()) "Genel Yayın" else yayinAdi.trim()
                    onAdd(name, pub, sinavTuru, d, y, calculatedNet, p, not)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                modifier = Modifier.testTag("btn_confirm_mock")
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
