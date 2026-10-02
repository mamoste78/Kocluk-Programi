package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkEntity
import com.example.data.model.MockExamEntity
import com.example.data.model.StudentEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.TeacherEntity
import com.example.data.remote.FirebaseManager
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class UserRole {
    NONE, OGRENCI, OGRETMEN
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    val firebaseManager = FirebaseManager(application)
    private val repository: AppRepository
    val database: AppDatabase

    // Oturum kalıcılığı için SharedPreferences (Kullanıcı çıkış yapmadıkça otomatik giriş kalır)
    private val sessionPrefs = application.getSharedPreferences("user_session_pref", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SAVED_ROLE = "saved_user_role"
        private const val KEY_SAVED_USER_ID = "saved_user_id"
    }

    // Firebase Bulut Senkronizasyon Durumu
    private val _syncStatus = MutableStateFlow(firebaseManager.lastSyncStatus)
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(firebaseManager.lastSyncTimestamp)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Aktif Kullanıcı Durumu
    private val _currentRole = MutableStateFlow(UserRole.NONE)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentStudent = MutableStateFlow<StudentEntity?>(null)
    val currentStudent: StateFlow<StudentEntity?> = _currentStudent.asStateFlow()

    private val _currentTeacher = MutableStateFlow<TeacherEntity?>(null)
    val currentTeacher: StateFlow<TeacherEntity?> = _currentTeacher.asStateFlow()

    // Öğretmen için seçili öğrenci
    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    init {
        database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppRepository(database.appDao(), firebaseManager)

        // Veritabanı boşsa örnek verileri doldur
        viewModelScope.launch(Dispatchers.IO) {
            val teachers = database.appDao().getAllTeachers()
            teachers.collect { list ->
                if (list.isEmpty()) {
                    AppDatabase.populateInitialData(database.appDao())
                }
            }
        }

        // Buluttaki verileri otomatik olarak senkronize et
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.fetchAndMergeStudentsFromFirebase()
            } catch (e: Exception) {
                // Çevrimdışıysa yerel verilerle devam et
            }
        }

        // OTOMATİK GİRİŞ: Çıkış yapılmadığı sürece kayıtlı kullanıcıyı otomatik başlat
        val savedRole = sessionPrefs.getString(KEY_SAVED_ROLE, null)
        val savedUserId = sessionPrefs.getString(KEY_SAVED_USER_ID, null)

        if (!savedUserId.isNullOrBlank() && !savedRole.isNullOrBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                if (savedRole == "OGRENCI") {
                    val student = repository.getStudentDirect(savedUserId)
                    if (student != null) {
                        withContext(Dispatchers.Main) {
                            _currentStudent.value = student
                            _currentTeacher.value = null
                            _currentRole.value = UserRole.OGRENCI
                        }
                    }
                } else if (savedRole == "OGRETMEN") {
                    val teacher = repository.getTeacherDirect(savedUserId)
                    if (teacher != null) {
                        withContext(Dispatchers.Main) {
                            _currentTeacher.value = teacher
                            _currentStudent.value = null
                            _currentRole.value = UserRole.OGRETMEN
                        }
                    }
                }
            }
        }
    }

    // Tüm Öğretmenler
    val allTeachers: StateFlow<List<TeacherEntity>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tüm Öğrenciler
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Giriş yapan öğretmene bağlı öğrenciler
    val teacherStudents: StateFlow<List<StudentEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) {
            repository.getStudentsByTeacher(teacher.ogretmen_id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Öğrenci Paneli Veri Akışları
    val studentStudyLogs: StateFlow<List<StudyLogEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getStudyLogs(student.ogrenci_id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentMockExams: StateFlow<List<MockExamEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getMockExams(student.ogrenci_id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentHomeworks: StateFlow<List<HomeworkEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getHomeworkForStudent(student.ogrenci_id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentChatMessages: StateFlow<List<ChatMessageEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null && student.secilen_ogretmen_id.isNotBlank()) {
            repository.getMessages(student.ogrenci_id, student.secilen_ogretmen_id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Öğretmen Paneli - Seçilen Öğrencinin Veri Akışları
    val selectedStudentStudyLogs: StateFlow<List<StudyLogEntity>> = _selectedStudent.flatMapLatest { student ->
        if (student != null) repository.getStudyLogs(student.ogrenci_id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStudentMockExams: StateFlow<List<MockExamEntity>> = _selectedStudent.flatMapLatest { student ->
        if (student != null) repository.getMockExams(student.ogrenci_id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStudentHomeworks: StateFlow<List<HomeworkEntity>> = _selectedStudent.flatMapLatest { student ->
        val teacher = _currentTeacher.value
        if (teacher != null && student != null) {
            repository.getHomeworkForTeacherAndStudent(teacher.ogretmen_id, student.ogrenci_id)
        } else if (student != null) {
            repository.getHomeworkForStudent(student.ogrenci_id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teacherChatMessages: StateFlow<List<ChatMessageEntity>> = _selectedStudent.flatMapLatest { student ->
        val teacher = _currentTeacher.value
        if (teacher != null && student != null) {
            repository.getMessages(teacher.ogretmen_id, student.ogrenci_id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- FIREBASE SENKRONİZASYON METODLARI ---

    fun syncAllStudentsToFirebase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            val students = repository.allStudents.first()
            val result = repository.syncAllStudentsToFirebase(students)
            _isSyncing.value = false
            _syncStatus.value = firebaseManager.lastSyncStatus
            _lastSyncTime.value = firebaseManager.lastSyncTimestamp
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    onResult(true, result.getOrDefault("Firebase senkronizasyonu başarılı."))
                } else {
                    onResult(false, result.exceptionOrNull()?.message ?: "Hata oluştu.")
                }
            }
        }
    }

    fun fetchStudentsFromFirebase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            val result = repository.fetchAndMergeStudentsFromFirebase()
            _isSyncing.value = false
            _syncStatus.value = firebaseManager.lastSyncStatus
            _lastSyncTime.value = firebaseManager.lastSyncTimestamp
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    val count = result.getOrDefault(0)
                    onResult(true, "$count öğrenci Firebase'den başarıyla yüklendi.")
                } else {
                    onResult(false, result.exceptionOrNull()?.message ?: "Firebase okunamadı.")
                }
            }
        }
    }

    // --- KULLANICI GİRİŞ & KAYIT İŞLEMLERİ (OTOMATİK GİRİŞ DESTEKLİ) ---

    fun loginStudent(username: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val student = repository.getStudentDirect(username.trim())
            withContext(Dispatchers.Main) {
                if (student != null && student.ogrenci_parola == pass) {
                    sessionPrefs.edit()
                        .putString(KEY_SAVED_ROLE, "OGRENCI")
                        .putString(KEY_SAVED_USER_ID, student.ogrenci_id)
                        .apply()

                    _currentStudent.value = student
                    _currentTeacher.value = null
                    _currentRole.value = UserRole.OGRENCI
                    onResult(true, null)
                } else if (student == null) {
                    onResult(false, "Öğrenci bulunamadı. Lütfen kullanıcı adınızı kontrol edin.")
                } else {
                    onResult(false, "Hatalı şifre girdiniz.")
                }
            }
        }
    }

    fun loginTeacher(username: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val teacher = repository.getTeacherDirect(username.trim())
            withContext(Dispatchers.Main) {
                if (teacher != null && teacher.ogretmen_parola == pass) {
                    sessionPrefs.edit()
                        .putString(KEY_SAVED_ROLE, "OGRETMEN")
                        .putString(KEY_SAVED_USER_ID, teacher.ogretmen_id)
                        .apply()

                    _currentTeacher.value = teacher
                    _currentStudent.value = null
                    _currentRole.value = UserRole.OGRETMEN
                    onResult(true, null)
                } else if (teacher == null) {
                    onResult(false, "Öğretmen bulunamadı. Lütfen kullanıcı adınızı kontrol edin.")
                } else {
                    onResult(false, "Hatalı şifre girdiniz.")
                }
            }
        }
    }

    fun registerStudent(student: StudentEntity, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getStudentDirect(student.ogrenci_id.trim())
            if (existing != null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Bu kullanıcı adı zaten kullanılıyor.")
                }
                return@launch
            }
            // Hem yerel Room veritabanına kaydeder hem de Firebase Cloud'a gönderir
            repository.saveStudent(student)
            _syncStatus.value = firebaseManager.lastSyncStatus
            _lastSyncTime.value = firebaseManager.lastSyncTimestamp

            sessionPrefs.edit()
                .putString(KEY_SAVED_ROLE, "OGRENCI")
                .putString(KEY_SAVED_USER_ID, student.ogrenci_id)
                .apply()

            withContext(Dispatchers.Main) {
                _currentStudent.value = student
                _currentTeacher.value = null
                _currentRole.value = UserRole.OGRENCI
                onResult(true, null)
            }
        }
    }

    fun registerTeacher(teacher: TeacherEntity, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getTeacherDirect(teacher.ogretmen_id.trim())
            if (existing != null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Bu kullanıcı adı zaten kullanılıyor.")
                }
                return@launch
            }
            repository.saveTeacher(teacher)

            sessionPrefs.edit()
                .putString(KEY_SAVED_ROLE, "OGRETMEN")
                .putString(KEY_SAVED_USER_ID, teacher.ogretmen_id)
                .apply()

            withContext(Dispatchers.Main) {
                _currentTeacher.value = teacher
                _currentStudent.value = null
                _currentRole.value = UserRole.OGRETMEN
                onResult(true, null)
            }
        }
    }

    fun logout() {
        sessionPrefs.edit().clear().apply()
        _currentRole.value = UserRole.NONE
        _currentStudent.value = null
        _currentTeacher.value = null
        _selectedStudent.value = null
    }

    // --- ÖĞRENCİ SEÇİMİ (ÖĞRETMEN İÇİN) ---
    fun selectStudent(student: StudentEntity) {
        _selectedStudent.value = student
    }

    // --- DERS ÇALIŞMA TAKİBİ İŞLEMLERİ ---
    fun addStudyLog(
        sinavTuru: String,
        ders: String,
        konu: String,
        cozulenSoru: Int,
        dogru: Int,
        yanlis: Int,
        net: Double,
        sureDk: Int = 0,
        not: String = ""
    ) {
        val studentId = _currentStudent.value?.ogrenci_id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addStudyLog(
                StudyLogEntity(
                    ogrenci_id = studentId,
                    sinav_turu = sinavTuru,
                    ders = ders,
                    konu = konu,
                    cozulensoru = cozulenSoru,
                    dogru = dogru,
                    yanlis = yanlis,
                    net = net,
                    calismaSuresiDk = sureDk,
                    tarih = System.currentTimeMillis(),
                    not = not
                )
            )
        }
    }

    fun deleteStudyLog(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudyLog(id)
        }
    }

    // --- DENEME SINAVI İŞLEMLERİ ---
    fun addMockExam(
        denemeAdi: String,
        yayinAdi: String,
        sinavTuru: String,
        dogru: Int,
        yanlis: Int,
        net: Double,
        puan: Double = 0.0,
        not: String = ""
    ) {
        val studentId = _currentStudent.value?.ogrenci_id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMockExam(
                MockExamEntity(
                    ogrenci_id = studentId,
                    deneme_adi = denemeAdi,
                    yayin_adi = yayinAdi,
                    sinav_turu = sinavTuru,
                    dogru = dogru,
                    yanlis = yanlis,
                    net = net,
                    puan = puan,
                    tarih = System.currentTimeMillis(),
                    not = not
                )
            )
        }
    }

    fun deleteMockExam(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMockExam(id)
        }
    }

    // --- ÖDEV İŞLEMLERİ ---
    fun markHomeworkSeen(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markHomeworkSeen(id)
        }
    }

    fun markHomeworkDone(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markHomeworkDone(id)
        }
    }

    fun assignHomework(
        studentId: String,
        ders: String,
        konu: String,
        soruSayisi: Int,
        sonTeslimTarihi: String,
        aciklama: String = ""
    ) {
        val teacherId = _currentTeacher.value?.ogretmen_id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addHomework(
                HomeworkEntity(
                    ogretmen_id = teacherId,
                    ogrenci_id = studentId,
                    ders = ders,
                    konu = konu,
                    soruSayisi = soruSayisi,
                    aciklama = aciklama,
                    sonTeslimTarihi = sonTeslimTarihi,
                    durum = HomeworkEntity.STATUS_BEKLEMEDE,
                    olusturmaTarihi = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteHomework(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHomework(id)
        }
    }

    // --- MESAJLAŞMA İŞLEMLERİ ---
    fun sendStudentMessage(text: String) {
        val student = _currentStudent.value ?: return
        val teacherId = student.secilen_ogretmen_id
        if (text.isBlank() || teacherId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.sendMessage(
                ChatMessageEntity(
                    gonderenId = student.ogrenci_id,
                    aliciId = teacherId,
                    gonderenRol = "OGRENCI",
                    gonderenAdi = "${student.ogrenci_ad} ${student.ogrenci_soyad}",
                    mesaj = text.trim(),
                    zamanDamgasi = System.currentTimeMillis()
                )
            )
        }
    }

    fun sendTeacherMessage(text: String) {
        val teacher = _currentTeacher.value ?: return
        val student = _selectedStudent.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.sendMessage(
                ChatMessageEntity(
                    gonderenId = teacher.ogretmen_id,
                    aliciId = student.ogrenci_id,
                    gonderenRol = "OGRETMEN",
                    gonderenAdi = "${teacher.ogretmen_ad} ${teacher.ogretmen_soyad}",
                    mesaj = text.trim(),
                    zamanDamgasi = System.currentTimeMillis()
                )
            )
        }
    }
}
