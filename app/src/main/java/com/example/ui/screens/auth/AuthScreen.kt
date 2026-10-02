package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CastForEducation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.data.model.TeacherEntity
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: MainViewModel) {
    // 0 = Öğrenci, 1 = Öğretmen
    var selectedTab by remember { mutableIntStateOf(0) }
    // false = Giriş Yap, true = Kayıt Ol
    var isRegisterMode by remember { mutableStateOf(false) }

    val teachers by viewModel.allTeachers.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Common fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Student specific fields
    var studentName by remember { mutableStateOf("") }
    var studentSurname by remember { mutableStateOf("") }
    var studentPhone by remember { mutableStateOf("") }
    var studentTargetJob by remember { mutableStateOf("") }
    var studentTargetScore by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("12. Sınıf - Sayısal") }
    var selectedTeacherId by remember { mutableStateOf("") }

    // Teacher specific fields
    var teacherName by remember { mutableStateOf("") }
    var teacherSurname by remember { mutableStateOf("") }
    var teacherBranch by remember { mutableStateOf("Matematik") }
    var teacherBranchAreas by remember { mutableStateOf("") }

    val classOptions = listOf(
        "9. Sınıf", "10. Sınıf", "11. Sınıf",
        "12. Sınıf - Sayısal", "12. Sınıf - Eşit Ağırlık", "12. Sınıf - Sözel", "12. Sınıf - Dil",
        "Mezun - Sayısal", "Mezun - Eşit Ağırlık", "Mezun - Sözel",
        "LGS Hazırlık (8. Sınıf)", "KPSS / Lisans", "MSÜ"
    )

    val branchOptions = listOf(
        "Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji",
        "Türkçe & Edebiyat", "Tarih", "Coğrafya", "Felsefe", "Rehberlik & Koçluk"
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PrimaryNavy, PrimaryBlue)
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sınav & Ders Takip",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Öğretmen - Öğrenci Koçluk & Başarı Platformu",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Dual Role Tab
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.testTag("tab_student"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Öğrenci", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.testTag("tab_teacher"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CastForEducation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Öğretmen", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Card Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Switch between Login / Register
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRegisterMode) {
                                if (selectedTab == 0) "Yeni Öğrenci Kaydı" else "Yeni Öğretmen Kaydı"
                            } else {
                                if (selectedTab == 0) "Öğrenci Girişi" else "Öğretmen Girişi"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                errorMessage = null
                                successMessage = null
                            },
                            modifier = Modifier.testTag("toggle_register_mode")
                        ) {
                            Text(
                                text = if (isRegisterMode) "Giriş Yap" else "Kayıt Ol",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFB91C1C),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = successMessage ?: "",
                                color = Color(0xFF047857),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Form Fields
                    if (isRegisterMode) {
                        // REGISTRATION FORM
                        if (selectedTab == 0) {
                            // STUDENT REGISTRATION
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = studentName,
                                    onValueChange = { studentName = it },
                                    label = { Text("Ad") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("student_reg_name"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = studentSurname,
                                    onValueChange = { studentSurname = it },
                                    label = { Text("Soyad") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("student_reg_surname"),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Kullanıcı Adı (ogrenci_id)") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("student_reg_username"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Şifre (ogrenci_parola)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("student_reg_password"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = studentPhone,
                                onValueChange = { studentPhone = it },
                                label = { Text("Telefon Numarası (ogrenci_telno)") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("student_reg_phone"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = studentTargetJob,
                                    onValueChange = { studentTargetJob = it },
                                    label = { Text("Hedef Meslek") },
                                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("student_reg_target"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = studentTargetScore,
                                    onValueChange = { studentTargetScore = it },
                                    label = { Text("Hedef Puan") },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .testTag("student_reg_score"),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Class Selection Dropdown
                            var classExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = classExpanded,
                                onExpandedChange = { classExpanded = !classExpanded }
                            ) {
                                OutlinedTextField(
                                    value = studentClass,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Sınıf Bilgisi") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = classExpanded,
                                    onDismissRequest = { classExpanded = false }
                                ) {
                                    classOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                studentClass = opt
                                                classExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Teacher Selection Dropdown
                            var teacherDropdownExpanded by remember { mutableStateOf(false) }
                            val currentTeacherDisplay = teachers.find { it.ogretmen_id == selectedTeacherId }?.let {
                                "${it.ogretmen_ad} ${it.ogretmen_soyad} (${it.brans})"
                            } ?: if (teachers.isNotEmpty()) {
                                if (selectedTeacherId.isBlank()) selectedTeacherId = teachers.first().ogretmen_id
                                "${teachers.first().ogretmen_ad} ${teachers.first().ogretmen_soyad} (${teachers.first().brans})"
                            } else "Henüz Öğretmen Kayıtlı Değil"

                            ExposedDropdownMenuBox(
                                expanded = teacherDropdownExpanded,
                                onExpandedChange = { teacherDropdownExpanded = !teacherDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = currentTeacherDisplay,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Seçilen Öğretmen") },
                                    leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("student_reg_teacher_select")
                                )
                                ExposedDropdownMenu(
                                    expanded = teacherDropdownExpanded,
                                    onDismissRequest = { teacherDropdownExpanded = false }
                                ) {
                                    teachers.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text("${t.ogretmen_ad} ${t.ogretmen_soyad} - ${t.brans}") },
                                            onClick = {
                                                selectedTeacherId = t.ogretmen_id
                                                teacherDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                        } else {
                            // TEACHER REGISTRATION
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = teacherName,
                                    onValueChange = { teacherName = it },
                                    label = { Text("Ad") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("teacher_reg_name"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = teacherSurname,
                                    onValueChange = { teacherSurname = it },
                                    label = { Text("Soyad") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("teacher_reg_surname"),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Kullanıcı Adı (ogretmen_id)") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("teacher_reg_username"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Şifre") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("teacher_reg_password"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Branch Selection Dropdown
                            var branchDropdownExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = branchDropdownExpanded,
                                onExpandedChange = { branchDropdownExpanded = !branchDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = teacherBranch,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Branş") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("teacher_reg_branch")
                                )
                                ExposedDropdownMenu(
                                    expanded = branchDropdownExpanded,
                                    onDismissRequest = { branchDropdownExpanded = false }
                                ) {
                                    branchOptions.forEach { b ->
                                        DropdownMenuItem(
                                            text = { Text(b) },
                                            onClick = {
                                                teacherBranch = b
                                                branchDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = teacherBranchAreas,
                                onValueChange = { teacherBranchAreas = it },
                                label = { Text("Branş Alanları & Uzmanlık") },
                                placeholder = { Text("ör: TYT-AYT Matematik, Geometri, Koçluk") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("teacher_reg_areas"),
                                singleLine = false,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Register Button
                        Button(
                            onClick = {
                                errorMessage = null
                                if (selectedTab == 0) {
                                    // Register Student
                                    if (username.isBlank() || password.isBlank() || studentName.isBlank()) {
                                        errorMessage = "Lütfen ad, kullanıcı adı ve şifre alanlarını doldurunuz."
                                        return@Button
                                    }
                                    val targetPuanVal = studentTargetScore.toDoubleOrNull() ?: 0.0
                                    val teacherToAssign = if (selectedTeacherId.isNotBlank()) selectedTeacherId else (teachers.firstOrNull()?.ogretmen_id ?: "")

                                    val student = StudentEntity(
                                        ogrenci_id = username.trim(),
                                        ogrenci_parola = password,
                                        ogrenci_ad = studentName.trim(),
                                        ogrenci_soyad = studentSurname.trim(),
                                        ogrenci_telno = studentPhone.trim(),
                                        ogrenci_hedef = studentTargetJob.trim(),
                                        ogrenci_puan = targetPuanVal,
                                        sinif_bilgisi = studentClass,
                                        secilen_ogretmen_id = teacherToAssign
                                    )
                                    viewModel.registerStudent(student) { success, err ->
                                        if (!success) errorMessage = err
                                    }
                                } else {
                                    // Register Teacher
                                    if (username.isBlank() || password.isBlank() || teacherName.isBlank()) {
                                        errorMessage = "Lütfen ad, kullanıcı adı ve şifre alanlarını doldurunuz."
                                        return@Button
                                    }
                                    val teacher = TeacherEntity(
                                        ogretmen_id = username.trim(),
                                        ogretmen_parola = password,
                                        ogretmen_ad = teacherName.trim(),
                                        ogretmen_soyad = teacherSurname.trim(),
                                        brans = teacherBranch,
                                        brans_alanlari = teacherBranchAreas.trim()
                                    )
                                    viewModel.registerTeacher(teacher) { success, err ->
                                        if (!success) errorMessage = err
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_register"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (selectedTab == 0) "Öğrenci Olarak Kaydol" else "Öğretmen Olarak Kaydol",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                    } else {
                        // LOGIN FORM
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Kullanıcı Adı") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Şifre") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "Lütfen kullanıcı adı ve şifrenizi giriniz."
                                    return@Button
                                }
                                if (selectedTab == 0) {
                                    viewModel.loginStudent(username, password) { success, err ->
                                        if (!success) errorMessage = err
                                    }
                                } else {
                                    viewModel.loginTeacher(username, password) { success, err ->
                                        if (!success) errorMessage = err
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_login"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == 0) PrimaryBlue else SecondaryTeal
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (selectedTab == 0) "Öğrenci Girişi Yap" else "Öğretmen Girişi Yap",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
