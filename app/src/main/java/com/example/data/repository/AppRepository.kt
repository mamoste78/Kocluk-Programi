package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkEntity
import com.example.data.model.MockExamEntity
import com.example.data.model.StudentEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.TeacherEntity
import com.example.data.remote.FirebaseManager
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val dao: AppDao,
    val firebaseManager: FirebaseManager
) {

    // Öğrenciler
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    val allTeachers: Flow<List<TeacherEntity>> = dao.getAllTeachers()

    fun getStudentsByTeacher(teacherId: String): Flow<List<StudentEntity>> =
        dao.getStudentsByTeacher(teacherId)

    fun getStudent(id: String): Flow<StudentEntity?> = dao.getStudentById(id)
    suspend fun getStudentDirect(id: String): StudentEntity? = dao.getStudentByIdDirect(id)

    fun getTeacher(id: String): Flow<TeacherEntity?> = dao.getTeacherById(id)
    suspend fun getTeacherDirect(id: String): TeacherEntity? = dao.getTeacherByIdDirect(id)

    suspend fun saveStudent(student: StudentEntity) {
        // 1. Lokale kaydet (Room)
        dao.insertStudent(student)
        // 2. Firebase Cloud Veritabanına kaydet
        firebaseManager.saveStudent(student)
    }

    suspend fun saveTeacher(teacher: TeacherEntity) {
        dao.insertTeacher(teacher)
        firebaseManager.saveTeacher(teacher)
    }

    // Firebase senkronizasyon fonksiyonları
    suspend fun syncAllStudentsToFirebase(students: List<StudentEntity>): Result<String> {
        return firebaseManager.syncAllStudentsToFirebase(students)
    }

    suspend fun fetchAndMergeStudentsFromFirebase(): Result<Int> {
        val res = firebaseManager.fetchAllStudents()
        return if (res.isSuccess) {
            val list = res.getOrDefault(emptyList())
            list.forEach { s ->
                dao.insertStudent(s)
            }
            Result.success(list.size)
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Firebase verileri okunamadı"))
        }
    }

    // Ders Takip
    fun getStudyLogs(studentId: String): Flow<List<StudyLogEntity>> =
        dao.getStudyLogsByStudent(studentId)

    suspend fun addStudyLog(log: StudyLogEntity) {
        dao.insertStudyLog(log)
        firebaseManager.saveStudyLog(log)
    }

    suspend fun deleteStudyLog(id: Long) = dao.deleteStudyLog(id)

    // Denemeler
    fun getMockExams(studentId: String): Flow<List<MockExamEntity>> =
        dao.getMockExamsByStudent(studentId)

    suspend fun addMockExam(exam: MockExamEntity) {
        dao.insertMockExam(exam)
        firebaseManager.saveMockExam(exam)
    }

    suspend fun deleteMockExam(id: Long) = dao.deleteMockExam(id)

    // Ödevler
    fun getHomeworkForStudent(studentId: String): Flow<List<HomeworkEntity>> =
        dao.getHomeworkByStudent(studentId)

    fun getHomeworkForTeacher(teacherId: String): Flow<List<HomeworkEntity>> =
        dao.getHomeworkByTeacher(teacherId)

    fun getHomeworkForTeacherAndStudent(teacherId: String, studentId: String): Flow<List<HomeworkEntity>> =
        dao.getHomeworkByTeacherAndStudent(teacherId, studentId)

    suspend fun addHomework(homework: HomeworkEntity) {
        dao.insertHomework(homework)
        firebaseManager.saveHomework(homework)
    }

    suspend fun markHomeworkSeen(id: Long) {
        dao.markHomeworkSeen(id)
        firebaseManager.updateHomeworkStatus(id, HomeworkEntity.STATUS_GORULDU)
    }

    suspend fun markHomeworkDone(id: Long) {
        dao.markHomeworkDone(id)
        firebaseManager.updateHomeworkStatus(id, HomeworkEntity.STATUS_BITIRILDI)
    }

    suspend fun deleteHomework(id: Long) = dao.deleteHomework(id)

    // Mesajlar
    fun getMessages(user1: String, user2: String): Flow<List<ChatMessageEntity>> =
        dao.getConversation(user1, user2)

    suspend fun sendMessage(message: ChatMessageEntity) {
        dao.insertMessage(message)
        firebaseManager.saveChatMessage(message)
    }
}
