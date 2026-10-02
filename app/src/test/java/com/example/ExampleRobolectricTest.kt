package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkEntity
import com.example.data.model.MockExamEntity
import com.example.data.model.StudentEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.TeacherEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sınav Takip", appName)
    }

    @Test
    fun testStudentAndTeacherRegistrationAndQueries() = runBlocking {
        val teacher = TeacherEntity(
            ogretmen_id = "test.teacher",
            ogretmen_parola = "pass123",
            ogretmen_ad = "Mehmet",
            ogretmen_soyad = "Demir",
            brans = "Fizik",
            brans_alanlari = "AYT Fizik"
        )
        dao.insertTeacher(teacher)

        val fetchedTeacher = dao.getTeacherByIdDirect("test.teacher")
        assertNotNull(fetchedTeacher)
        assertEquals("Mehmet", fetchedTeacher?.ogretmen_ad)
        assertEquals("Fizik", fetchedTeacher?.brans)

        val student = StudentEntity(
            ogrenci_id = "test.student",
            ogrenci_parola = "pass123",
            ogrenci_ad = "Can",
            ogrenci_soyad = "Yıldız",
            ogrenci_telno = "05550001122",
            ogrenci_hedef = "Tıp Fakültesi",
            ogrenci_puan = 495.0,
            sinif_bilgisi = "12. Sınıf",
            secilen_ogretmen_id = "test.teacher"
        )
        dao.insertStudent(student)

        val fetchedStudent = dao.getStudentByIdDirect("test.student")
        assertNotNull(fetchedStudent)
        assertEquals("Can", fetchedStudent?.ogrenci_ad)
        assertEquals("Tıp Fakültesi", fetchedStudent?.ogrenci_hedef)
        assertEquals("test.teacher", fetchedStudent?.secilen_ogretmen_id)
    }

    @Test
    fun testStudyLogAndMockExam() = runBlocking {
        val log = StudyLogEntity(
            ogrenci_id = "test.student",
            sinav_turu = "AYT",
            ders = "Matematik",
            konu = "Türev",
            cozulensoru = 50,
            dogru = 45,
            yanlis = 4,
            net = 44.0
        )
        dao.insertStudyLog(log)

        val logs = dao.getStudyLogsByStudent("test.student").first()
        assertEquals(1, logs.size)
        assertEquals("Türev", logs.first().konu)
        assertEquals(44.0, logs.first().net, 0.01)

        val exam = MockExamEntity(
            ogrenci_id = "test.student",
            deneme_adi = "TYT Deneme 1",
            yayin_adi = "3D",
            sinav_turu = "TYT",
            dogru = 100,
            yanlis = 10,
            net = 97.5,
            puan = 450.0
        )
        dao.insertMockExam(exam)

        val exams = dao.getMockExamsByStudent("test.student").first()
        assertEquals(1, exams.size)
        assertEquals(97.5, exams.first().net, 0.01)
    }

    @Test
    fun testHomeworkLifecycleAndSeenBitirdiStatus() = runBlocking {
        val hw = HomeworkEntity(
            ogretmen_id = "test.teacher",
            ogrenci_id = "test.student",
            ders = "Fizik",
            konu = "Dalgalar",
            soruSayisi = 60,
            durum = HomeworkEntity.STATUS_BEKLEMEDE
        )
        dao.insertHomework(hw)

        var list = dao.getHomeworkByStudent("test.student").first()
        assertEquals(1, list.size)
        assertEquals(HomeworkEntity.STATUS_BEKLEMEDE, list.first().durum)

        val hwId = list.first().id
        // When student sees homework
        dao.markHomeworkSeen(hwId)
        list = dao.getHomeworkByStudent("test.student").first()
        assertEquals(HomeworkEntity.STATUS_GORULDU, list.first().durum)

        // When student completes homework
        dao.markHomeworkDone(hwId)
        list = dao.getHomeworkByStudent("test.student").first()
        assertEquals(HomeworkEntity.STATUS_BITIRILDI, list.first().durum)
    }

    @Test
    fun testChatMessages() = runBlocking {
        val msg1 = ChatMessageEntity(
            gonderenId = "test.teacher",
            aliciId = "test.student",
            gonderenRol = "OGRETMEN",
            gonderenAdi = "Mehmet Hoca",
            mesaj = "Ödevini tamamladın mı?"
        )
        dao.insertMessage(msg1)

        val msg2 = ChatMessageEntity(
            gonderenId = "test.student",
            aliciId = "test.teacher",
            gonderenRol = "OGRENCI",
            gonderenAdi = "Can Yıldız",
            mesaj = "Evet hocam, bitirdim."
        )
        dao.insertMessage(msg2)

        val conversation = dao.getConversation("test.teacher", "test.student").first()
        assertEquals(2, conversation.size)
        assertEquals("Ödevini tamamladın mı?", conversation[0].mesaj)
        assertEquals("Evet hocam, bitirdim.", conversation[1].mesaj)
    }

    @Test
    fun testFirebaseManagerConstants() {
        assertEquals("ogrenciler", com.example.data.remote.FirebaseManager.COL_STUDENTS)
        assertEquals("ogretmenler", com.example.data.remote.FirebaseManager.COL_TEACHERS)
        assertEquals("ders_takipleri", com.example.data.remote.FirebaseManager.COL_STUDY_LOGS)
        assertEquals("denemeler", com.example.data.remote.FirebaseManager.COL_MOCK_EXAMS)
        assertEquals("odevler", com.example.data.remote.FirebaseManager.COL_HOMEWORKS)
        assertEquals("mesajlar", com.example.data.remote.FirebaseManager.COL_MESSAGES)
    }
}
