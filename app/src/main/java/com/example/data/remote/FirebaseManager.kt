package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkEntity
import com.example.data.model.MockExamEntity
import com.example.data.model.StudentEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.TeacherEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FirebaseManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("firebase_sync_pref", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "FirebaseManager"
        const val PROJECT_ID = "gen-lang-client-0609862453"
        const val API_KEY = "AIzaSyBje-_K5Lvhjizsm5DnU-IbhhbRQUMygtQ"
        const val APP_ID = "1:957192566936:android:b2089ea47b3b44b8b60b73"
        const val STORAGE_BUCKET = "gen-lang-client-0609862453.firebasestorage.app"

        // Koleksiyon İsimleri
        const val COL_STUDENTS = "ogrenciler"
        const val COL_TEACHERS = "ogretmenler"
        const val COL_STUDY_LOGS = "ders_takipleri"
        const val COL_MOCK_EXAMS = "denemeler"
        const val COL_HOMEWORKS = "odevler"
        const val COL_MESSAGES = "mesajlar"

        const val FIRESTORE_RULES_SNIPPET = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}"""
    }

    private var firestore: FirebaseFirestore? = null
    private var firebaseAuth: FirebaseAuth? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    var lastSyncTimestamp: Long
        get() = prefs.getLong("last_sync_time", 0L)
        set(value) = prefs.edit().putLong("last_sync_time", value).apply()

    var lastSyncStatus: String
        get() = prefs.getString("last_sync_status", "Firebase hazır") ?: "Firebase hazır"
        set(value) = prefs.edit().putString("last_sync_status", value).apply()

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId(PROJECT_ID)
                    .setApplicationId(APP_ID)
                    .setApiKey(API_KEY)
                    .setStorageBucket(STORAGE_BUCKET)
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            firestore = FirebaseFirestore.getInstance()
            firebaseAuth = FirebaseAuth.getInstance()
            Log.d(TAG, "Firebase Firestore ve Auth başarıyla başlatıldı.")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase SDK başlatma notu: ${e.message}")
        }
    }

    /**
     * Firebase Anonim Giriş: Firestore güvenlik kuralları authenticated kullanıcı istiyorsa
     * arka planda otomatik olarak yetkilendirme sağlar.
     */
    suspend fun ensureAuthenticated() = withContext(Dispatchers.IO) {
        try {
            val auth = firebaseAuth ?: FirebaseAuth.getInstance().also { firebaseAuth = it }
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
                Log.d(TAG, "Firebase Anonim oturum açıldı: ${auth.currentUser?.uid}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Anonim auth notu: ${e.message}")
        }
    }

    private fun formatError(e: Throwable): String {
        val msg = e.localizedMessage ?: e.message ?: "Bilinmeyen hata"
        return if (msg.contains("PERMISSION_DENIED", ignoreCase = true) ||
            msg.contains("Missing or insufficient permissions", ignoreCase = true)
        ) {
            "İzin Hatası (Permission Denied): Firebase Console'da Cloud Firestore Kuralları (Rules) açık olmalıdır. Lütfen kuralları 'allow read, write: if true;' olarak güncelleyiniz."
        } else {
            msg
        }
    }

    // --- ÖĞRENCİ İŞLEMLERİ ---

    suspend fun saveStudent(student: StudentEntity): Result<String> = withContext(Dispatchers.IO) {
        ensureAuthenticated()

        val studentMap = hashMapOf(
            "ogrenci_id" to student.ogrenci_id,
            "ogrenci_parola" to student.ogrenci_parola,
            "ogrenci_ad" to student.ogrenci_ad,
            "ogrenci_soyad" to student.ogrenci_soyad,
            "ogrenci_telno" to student.ogrenci_telno,
            "ogrenci_hedef" to student.ogrenci_hedef,
            "ogrenci_puan" to student.ogrenci_puan,
            "sinif_bilgisi" to student.sinif_bilgisi,
            "secilen_ogretmen_id" to student.secilen_ogretmen_id,
            "guncellenme_zamani" to System.currentTimeMillis()
        )

        // 1. Firestore SDK ile dene
        try {
            val db = firestore
            if (db != null) {
                db.collection(COL_STUDENTS)
                    .document(student.ogrenci_id)
                    .set(studentMap, SetOptions.merge())
                    .await()
                lastSyncTimestamp = System.currentTimeMillis()
                lastSyncStatus = "Firebase bulutuna kaydedildi (${student.ogrenci_ad})"
                return@withContext Result.success("Öğrenci Firebase veritabanına kaydedildi.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore SDK error: ${e.message}, trying REST...")
            if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                val formatted = formatError(e)
                lastSyncStatus = "İzin Hatası: Firestore kurallarını güncelleyiniz"
                return@withContext Result.failure(Exception(formatted))
            }
        }

        // 2. REST API ile kayıt denemesi
        try {
            val restUrl = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/$COL_STUDENTS/${student.ogrenci_id}?key=$API_KEY"
            val fieldsJson = JSONObject().apply {
                put("ogrenci_id", JSONObject().put("stringValue", student.ogrenci_id))
                put("ogrenci_parola", JSONObject().put("stringValue", student.ogrenci_parola))
                put("ogrenci_ad", JSONObject().put("stringValue", student.ogrenci_ad))
                put("ogrenci_soyad", JSONObject().put("stringValue", student.ogrenci_soyad))
                put("ogrenci_telno", JSONObject().put("stringValue", student.ogrenci_telno))
                put("ogrenci_hedef", JSONObject().put("stringValue", student.ogrenci_hedef))
                put("ogrenci_puan", JSONObject().put("doubleValue", student.ogrenci_puan))
                put("sinif_bilgisi", JSONObject().put("stringValue", student.sinif_bilgisi))
                put("secilen_ogretmen_id", JSONObject().put("stringValue", student.secilen_ogretmen_id))
            }
            val body = JSONObject().put("fields", fieldsJson).toString()
            val req = Request.Builder()
                .url(restUrl)
                .patch(body.toRequestBody("application/json".toMediaType()))
                .build()
            val resp = httpClient.newCall(req).execute()
            if (resp.isSuccessful) {
                lastSyncTimestamp = System.currentTimeMillis()
                lastSyncStatus = "Firebase güncellendi (${student.ogrenci_ad})"
                return@withContext Result.success("Firebase'e kaydedildi.")
            } else if (resp.code == 403 || resp.code == 401) {
                val err = "İzin Hatası (${resp.code}): Firestore kurallarında okuma/yazma izni kapalı."
                lastSyncStatus = err
                return@withContext Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Log.e(TAG, "REST write error: ${e.message}")
        }

        lastSyncStatus = "Lokalde saklandı"
        Result.success("Lokal veritabanına kaydedildi.")
    }

    suspend fun saveTeacher(teacher: TeacherEntity): Result<String> = withContext(Dispatchers.IO) {
        ensureAuthenticated()

        val teacherMap = hashMapOf(
            "ogretmen_id" to teacher.ogretmen_id,
            "ogretmen_parola" to teacher.ogretmen_parola,
            "ogretmen_ad" to teacher.ogretmen_ad,
            "ogretmen_soyad" to teacher.ogretmen_soyad,
            "brans" to teacher.brans,
            "brans_alanlari" to teacher.brans_alanlari,
            "telefon" to teacher.telefon,
            "guncellenme_zamani" to System.currentTimeMillis()
        )

        try {
            val db = firestore
            if (db != null) {
                db.collection(COL_TEACHERS)
                    .document(teacher.ogretmen_id)
                    .set(teacherMap, SetOptions.merge())
                    .await()
                return@withContext Result.success("Öğretmen Firebase'e kaydedildi.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Teacher firestore save error: ${e.message}")
        }
        Result.success("Lokalde saklandı.")
    }

    // --- DERS TAKİP VE DENEMELER ---

    suspend fun saveStudyLog(log: StudyLogEntity) = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val db = firestore ?: return@withContext
            val map = hashMapOf(
                "ogrenci_id" to log.ogrenci_id,
                "sinav_turu" to log.sinav_turu,
                "ders" to log.ders,
                "konu" to log.konu,
                "cozulensoru" to log.cozulensoru,
                "dogru" to log.dogru,
                "yanlis" to log.yanlis,
                "net" to log.net,
                "calismaSuresiDk" to log.calismaSuresiDk,
                "tarih" to log.tarih,
                "not" to log.not
            )
            db.collection(COL_STUDY_LOGS).add(map).await()
            lastSyncTimestamp = System.currentTimeMillis()
            lastSyncStatus = "Ders takibi Firebase'e aktarıldı"
        } catch (e: Exception) {
            Log.w(TAG, "Study log save error: ${e.message}")
        }
    }

    suspend fun saveMockExam(exam: MockExamEntity) = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val db = firestore ?: return@withContext
            val map = hashMapOf(
                "ogrenci_id" to exam.ogrenci_id,
                "deneme_adi" to exam.deneme_adi,
                "yayin_adi" to exam.yayin_adi,
                "sinav_turu" to exam.sinav_turu,
                "dogru" to exam.dogru,
                "yanlis" to exam.yanlis,
                "net" to exam.net,
                "puan" to exam.puan,
                "tarih" to exam.tarih,
                "not" to exam.not
            )
            db.collection(COL_MOCK_EXAMS).add(map).await()
            lastSyncTimestamp = System.currentTimeMillis()
            lastSyncStatus = "Deneme sınavı Firebase'e aktarıldı"
        } catch (e: Exception) {
            Log.w(TAG, "Mock exam save error: ${e.message}")
        }
    }

    // --- ÖDEVLER ---

    suspend fun saveHomework(hw: HomeworkEntity) = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val db = firestore ?: return@withContext
            val map = hashMapOf(
                "ogretmen_id" to hw.ogretmen_id,
                "ogrenci_id" to hw.ogrenci_id,
                "ders" to hw.ders,
                "konu" to hw.konu,
                "soruSayisi" to hw.soruSayisi,
                "aciklama" to hw.aciklama,
                "sonTeslimTarihi" to hw.sonTeslimTarihi,
                "durum" to hw.durum,
                "olusturmaTarihi" to hw.olusturmaTarihi
            )
            val docId = if (hw.id > 0) hw.id.toString() else null
            if (docId != null) {
                db.collection(COL_HOMEWORKS).document(docId).set(map, SetOptions.merge()).await()
            } else {
                db.collection(COL_HOMEWORKS).add(map).await()
            }
            lastSyncTimestamp = System.currentTimeMillis()
            lastSyncStatus = "Ödev Firebase'e aktarıldı"
        } catch (e: Exception) {
            Log.w(TAG, "Homework save error: ${e.message}")
        }
    }

    suspend fun updateHomeworkStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val db = firestore ?: return@withContext
            db.collection(COL_HOMEWORKS).document(id.toString())
                .update("durum", status)
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Update homework status error: ${e.message}")
        }
    }

    // --- MESAJLAR ---

    suspend fun saveChatMessage(msg: ChatMessageEntity) = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val db = firestore ?: return@withContext
            val map = hashMapOf(
                "gonderenId" to msg.gonderenId,
                "aliciId" to msg.aliciId,
                "gonderenRol" to msg.gonderenRol,
                "gonderenAdi" to msg.gonderenAdi,
                "mesaj" to msg.mesaj,
                "zamanDamgasi" to msg.zamanDamgasi
            )
            db.collection(COL_MESSAGES).add(map).await()
            lastSyncTimestamp = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.w(TAG, "Message save error: ${e.message}")
        }
    }

    // --- FIREBASE'DEN TÜM ÖĞRENCİLERİ GETİR ---

    suspend fun fetchAllStudents(): Result<List<StudentEntity>> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection(COL_STUDENTS).get().await()
                val list = mutableListOf<StudentEntity>()
                for (doc in snapshot.documents) {
                    val s = StudentEntity(
                        ogrenci_id = doc.getString("ogrenci_id") ?: doc.id,
                        ogrenci_parola = doc.getString("ogrenci_parola") ?: "123456",
                        ogrenci_ad = doc.getString("ogrenci_ad") ?: "İsimsiz",
                        ogrenci_soyad = doc.getString("ogrenci_soyad") ?: "",
                        ogrenci_telno = doc.getString("ogrenci_telno") ?: "",
                        ogrenci_hedef = doc.getString("ogrenci_hedef") ?: "Hedef",
                        ogrenci_puan = doc.getDouble("ogrenci_puan") ?: 0.0,
                        sinif_bilgisi = doc.getString("sinif_bilgisi") ?: "12. Sınıf",
                        secilen_ogretmen_id = doc.getString("secilen_ogretmen_id") ?: ""
                    )
                    list.add(s)
                }
                lastSyncTimestamp = System.currentTimeMillis()
                lastSyncStatus = "${list.size} öğrenci Firebase'den yüklendi"
                return@withContext Result.success(list)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fetch students error: ${e.message}")
            return@withContext Result.failure(Exception(formatError(e)))
        }
        Result.failure(Exception("Firebase'den veri okunamadı."))
    }

    // --- TÜM YEREL ÖĞRENCİLERİ FIREBASE'E YÜKLE ---

    suspend fun syncAllStudentsToFirebase(students: List<StudentEntity>): Result<String> = withContext(Dispatchers.IO) {
        ensureAuthenticated()
        try {
            val db = firestore
            if (db != null) {
                val batch = db.batch()
                students.forEach { s ->
                    val docRef = db.collection(COL_STUDENTS).document(s.ogrenci_id)
                    val map = hashMapOf(
                        "ogrenci_id" to s.ogrenci_id,
                        "ogrenci_parola" to s.ogrenci_parola,
                        "ogrenci_ad" to s.ogrenci_ad,
                        "ogrenci_soyad" to s.ogrenci_soyad,
                        "ogrenci_telno" to s.ogrenci_telno,
                        "ogrenci_hedef" to s.ogrenci_hedef,
                        "ogrenci_puan" to s.ogrenci_puan,
                        "sinif_bilgisi" to s.sinif_bilgisi,
                        "secilen_ogretmen_id" to s.secilen_ogretmen_id,
                        "guncellenme_zamani" to System.currentTimeMillis()
                    )
                    batch.set(docRef, map, SetOptions.merge())
                }
                batch.commit().await()
                lastSyncTimestamp = System.currentTimeMillis()
                lastSyncStatus = "${students.size} öğrenci Firebase'e aktarıldı"
                return@withContext Result.success("${students.size} öğrenci Firebase Cloud Firestore'a başarıyla aktarıldı.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync error: ${e.message}")
            val formatted = formatError(e)
            lastSyncStatus = "Hata: İzin reddedildi"
            return@withContext Result.failure(Exception(formatted))
        }
        Result.failure(Exception("Firebase bağlantısı kurulamadı."))
    }
}
