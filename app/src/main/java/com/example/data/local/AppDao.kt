package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkEntity
import com.example.data.model.MockExamEntity
import com.example.data.model.StudentEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Öğrenci İşlemleri
    @Query("SELECT * FROM ogrenciler ORDER BY ogrenci_ad ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM ogrenciler WHERE secilen_ogretmen_id = :teacherId ORDER BY ogrenci_ad ASC")
    fun getStudentsByTeacher(teacherId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM ogrenciler WHERE ogrenci_id = :id LIMIT 1")
    fun getStudentById(id: String): Flow<StudentEntity?>

    @Query("SELECT * FROM ogrenciler WHERE ogrenci_id = :id LIMIT 1")
    suspend fun getStudentByIdDirect(id: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    // Öğretmen İşlemleri
    @Query("SELECT * FROM ogretmenler ORDER BY ogretmen_ad ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM ogretmenler WHERE ogretmen_id = :id LIMIT 1")
    fun getTeacherById(id: String): Flow<TeacherEntity?>

    @Query("SELECT * FROM ogretmenler WHERE ogretmen_id = :id LIMIT 1")
    suspend fun getTeacherByIdDirect(id: String): TeacherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)

    // Ders Çalışma Takip İşlemleri
    @Query("SELECT * FROM ders_takipleri WHERE ogrenci_id = :studentId ORDER BY tarih DESC")
    fun getStudyLogsByStudent(studentId: String): Flow<List<StudyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyLog(log: StudyLogEntity)

    @Query("DELETE FROM ders_takipleri WHERE id = :id")
    suspend fun deleteStudyLog(id: Long)

    // Deneme Sınavı İşlemleri
    @Query("SELECT * FROM denemeler WHERE ogrenci_id = :studentId ORDER BY tarih DESC")
    fun getMockExamsByStudent(studentId: String): Flow<List<MockExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockExam(exam: MockExamEntity)

    @Query("DELETE FROM denemeler WHERE id = :id")
    suspend fun deleteMockExam(id: Long)

    // Ödev İşlemleri
    @Query("SELECT * FROM odevler WHERE ogrenci_id = :studentId ORDER BY olusturmaTarihi DESC")
    fun getHomeworkByStudent(studentId: String): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM odevler WHERE ogretmen_id = :teacherId AND ogrenci_id = :studentId ORDER BY olusturmaTarihi DESC")
    fun getHomeworkByTeacherAndStudent(teacherId: String, studentId: String): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM odevler WHERE ogretmen_id = :teacherId ORDER BY olusturmaTarihi DESC")
    fun getHomeworkByTeacher(teacherId: String): Flow<List<HomeworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity)

    @Update
    suspend fun updateHomework(homework: HomeworkEntity)

    @Query("UPDATE odevler SET durum = :status, gorulmeTarihi = :seenTimestamp WHERE id = :id AND durum = 'BEKLEMEDE'")
    suspend fun markHomeworkSeen(id: Long, status: String = HomeworkEntity.STATUS_GORULDU, seenTimestamp: Long = System.currentTimeMillis())

    @Query("UPDATE odevler SET durum = :status, bitirmeTarihi = :doneTimestamp WHERE id = :id")
    suspend fun markHomeworkDone(id: Long, status: String = HomeworkEntity.STATUS_BITIRILDI, doneTimestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM odevler WHERE id = :id")
    suspend fun deleteHomework(id: Long)

    // Mesajlaşma İşlemleri
    @Query("""
        SELECT * FROM mesajlar 
        WHERE (gonderenId = :user1 AND aliciId = :user2) 
           OR (gonderenId = :user2 AND aliciId = :user1) 
        ORDER BY zamanDamgasi ASC
    """)
    fun getConversation(user1: String, user2: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}
