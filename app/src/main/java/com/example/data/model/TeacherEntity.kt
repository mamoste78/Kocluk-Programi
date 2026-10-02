package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ogretmenler")
data class TeacherEntity(
    @PrimaryKey
    val ogretmen_id: String, // kullanıcı adı
    val ogretmen_parola: String,
    val ogretmen_ad: String,
    val ogretmen_soyad: String,
    val brans: String, // ör. Matematik, Fizik, Rehberlik
    val brans_alanlari: String, // ör. TYT-AYT Matematik, Geometri, Sınav Koçluğu
    val telefon: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
