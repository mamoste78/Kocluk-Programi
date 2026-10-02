package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkEntity
import com.example.data.model.MockExamEntity
import com.example.data.model.StudentEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.TeacherEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentEntity::class,
        TeacherEntity::class,
        StudyLogEntity::class,
        MockExamEntity::class,
        HomeworkEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sinav_takip_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            // Örnek Öğretmenler
            val t1 = TeacherEntity(
                ogretmen_id = "ahmet.hoca",
                ogretmen_parola = "123456",
                ogretmen_ad = "Ahmet",
                ogretmen_soyad = "Kaya",
                brans = "Matematik",
                brans_alanlari = "TYT - AYT Matematik, Geometri & Koçluk",
                telefon = "0555 123 4567"
            )
            val t2 = TeacherEntity(
                ogretmen_id = "zeynep.hoca",
                ogretmen_parola = "123456",
                ogretmen_ad = "Zeynep",
                ogretmen_soyad = "Demir",
                brans = "Fizik",
                brans_alanlari = "AYT Fizik & YKS Hazırlık",
                telefon = "0555 987 6543"
            )
            dao.insertTeacher(t1)
            dao.insertTeacher(t2)

            // Örnek Öğrenci
            val s1 = StudentEntity(
                ogrenci_id = "ali.yilmaz",
                ogrenci_parola = "123456",
                ogrenci_ad = "Ali",
                ogrenci_soyad = "Yılmaz",
                ogrenci_telno = "0532 555 1234",
                ogrenci_hedef = "Tıp Fakültesi",
                ogrenci_puan = 490.0,
                sinif_bilgisi = "12. Sınıf - Sayısal",
                secilen_ogretmen_id = "ahmet.hoca"
            )
            val s2 = StudentEntity(
                ogrenci_id = "elif.kaya",
                ogrenci_parola = "123456",
                ogrenci_ad = "Elif",
                ogrenci_soyad = "Kaya",
                ogrenci_telno = "0544 333 7890",
                ogrenci_hedef = "Bilgisayar Mühendisliği",
                ogrenci_puan = 475.0,
                sinif_bilgisi = "Mezun - Sayısal",
                secilen_ogretmen_id = "ahmet.hoca"
            )
            dao.insertStudent(s1)
            dao.insertStudent(s2)

            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            // Örnek Ders Çalışmaları (Ali için)
            dao.insertStudyLog(
                StudyLogEntity(
                    ogrenci_id = "ali.yilmaz",
                    sinav_turu = "AYT",
                    ders = "Matematik",
                    konu = "Türev ve Uygulamaları",
                    cozulensoru = 85,
                    dogru = 76,
                    yanlis = 6,
                    net = 74.5,
                    calismaSuresiDk = 120,
                    tarih = now - oneDay * 3,
                    not = "Maksimum-minimum problemleri tekrar edilmeli"
                )
            )
            dao.insertStudyLog(
                StudyLogEntity(
                    ogrenci_id = "ali.yilmaz",
                    sinav_turu = "TYT",
                    ders = "Türkçe",
                    konu = "Paragrafta Yapı & Anlam",
                    cozulensoru = 60,
                    dogru = 54,
                    yanlis = 4,
                    net = 53.0,
                    calismaSuresiDk = 65,
                    tarih = now - oneDay * 2,
                    not = "Hız gayet iyiydi"
                )
            )
            dao.insertStudyLog(
                StudyLogEntity(
                    ogrenci_id = "ali.yilmaz",
                    sinav_turu = "AYT",
                    ders = "Fizik",
                    konu = "Elektromanyetizma",
                    cozulensoru = 50,
                    dogru = 42,
                    yanlis = 5,
                    net = 40.75,
                    calismaSuresiDk = 90,
                    tarih = now - oneDay,
                    not = "İndüksiyon akımı formüllerine dikkat"
                )
            )
            dao.insertStudyLog(
                StudyLogEntity(
                    ogrenci_id = "ali.yilmaz",
                    sinav_turu = "AYT",
                    ders = "Matematik",
                    konu = "İntegral Alma Kuralları",
                    cozulensoru = 75,
                    dogru = 68,
                    yanlis = 4,
                    net = 67.0,
                    calismaSuresiDk = 100,
                    tarih = now,
                    not = "Değişken değiştirme yöntemi tamam"
                )
            )

            // Örnek Denemeler (Ali için)
            dao.insertMockExam(
                MockExamEntity(
                    ogrenci_id = "ali.yilmaz",
                    deneme_adi = "3D Türkiye Geneli TYT-1",
                    yayin_adi = "3D Yayınları",
                    sinav_turu = "TYT",
                    dogru = 102,
                    yanlis = 14,
                    net = 98.5,
                    puan = 445.0,
                    tarih = now - oneDay * 7,
                    not = "Türkçe ve Mat iyi, Fen biraz zorladı"
                )
            )
            dao.insertMockExam(
                MockExamEntity(
                    ogrenci_id = "ali.yilmaz",
                    deneme_adi = "Bilgi Sarmal TYT Deneme-3",
                    yayin_adi = "Bilgi Sarmal",
                    sinav_turu = "TYT",
                    dogru = 106,
                    yanlis = 10,
                    net = 103.5,
                    puan = 462.5,
                    tarih = now - oneDay * 2,
                    not = "Zaman yönetiminde gelişme var"
                )
            )
            dao.insertMockExam(
                MockExamEntity(
                    ogrenci_id = "ali.yilmaz",
                    deneme_adi = "Özdebir AYT Deneme-1",
                    yayin_adi = "Özdebir",
                    sinav_turu = "AYT",
                    dogru = 68,
                    yanlis = 8,
                    net = 66.0,
                    puan = 478.0,
                    tarih = now - oneDay * 4,
                    not = "Matematik neti harika"
                )
            )

            // Örnek Ödevler
            dao.insertHomework(
                HomeworkEntity(
                    ogretmen_id = "ahmet.hoca",
                    ogrenci_id = "ali.yilmaz",
                    ders = "Matematik",
                    konu = "Trigonometri Toplam-Fark ve Yarım Açı",
                    soruSayisi = 80,
                    aciklama = "Konu anlatım fasikülündeki tüm testler bitirilecek.",
                    sonTeslimTarihi = "10 Ekim 2026",
                    durum = HomeworkEntity.STATUS_BITIRILDI,
                    gorulmeTarihi = now - oneDay * 2,
                    bitirmeTarihi = now - oneDay,
                    olusturmaTarihi = now - oneDay * 3
                )
            )
            dao.insertHomework(
                HomeworkEntity(
                    ogretmen_id = "ahmet.hoca",
                    ogrenci_id = "ali.yilmaz",
                    ders = "Matematik",
                    konu = "İntegral Alan Hesabı",
                    soruSayisi = 100,
                    aciklama = "Eğri altında kalan alan sorularına özel dikkat göster.",
                    sonTeslimTarihi = "16 Ekim 2026",
                    durum = HomeworkEntity.STATUS_GORULDU,
                    gorulmeTarihi = now - 3600000L * 4,
                    olusturmaTarihi = now - oneDay
                )
            )
            dao.insertHomework(
                HomeworkEntity(
                    ogretmen_id = "ahmet.hoca",
                    ogrenci_id = "ali.yilmaz",
                    ders = "Geometri",
                    konu = "Analitik Geometri Doğrunun Analitiği",
                    soruSayisi = 60,
                    aciklama = "Noktanın doğruya uzaklığı testleri.",
                    sonTeslimTarihi = "20 Ekim 2026",
                    durum = HomeworkEntity.STATUS_BEKLEMEDE,
                    olusturmaTarihi = now
                )
            )

            // Örnek Mesajlar
            dao.insertMessage(
                ChatMessageEntity(
                    gonderenId = "ahmet.hoca",
                    aliciId = "ali.yilmaz",
                    gonderenRol = "OGRETMEN",
                    gonderenAdi = "Ahmet Hoca",
                    mesaj = "Merhaba Ali, son denemedeki net artışın çok iyi! İntegral ödevine başladın mı?",
                    zamanDamgasi = now - 3600000L * 5
                )
            )
            dao.insertMessage(
                ChatMessageEntity(
                    gonderenId = "ali.yilmaz",
                    aliciId = "ahmet.hoca",
                    gonderenRol = "OGRENCI",
                    gonderenAdi = "Ali Yılmaz",
                    mesaj = "Hocam merhabalar, teşekkür ederim! Evet başladım, 30 soru çözdüm, akşama kadar kalanını da bitireceğim inşallah.",
                    zamanDamgasi = now - 3600000L * 3
                )
            )
            dao.insertMessage(
                ChatMessageEntity(
                    gonderenId = "ahmet.hoca",
                    aliciId = "ali.yilmaz",
                    gonderenRol = "OGRETMEN",
                    gonderenAdi = "Ahmet Hoca",
                    mesaj = "Harikasın, takıldığın soru olursa fotoğrafını çekip bana iletebilirsin.",
                    zamanDamgasi = now - 3600000L * 2
                )
            )
        }
    }
}
