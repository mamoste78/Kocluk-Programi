package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ders_takipleri")
data class StudyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ogrenci_id: String,
    val sinav_turu: String, // TYT, AYT, LGS, YKS, MSÜ, KPSS
    val ders: String,       // Matematik, Türkçe, Fizik, Kimya vb.
    val konu: String,       // ör. Türev, Paragraf, Optik
    val cozulensoru: Int,
    val dogru: Int,
    val yanlis: Int,
    val net: Double,
    val calismaSuresiDk: Int = 0,
    val tarih: Long = System.currentTimeMillis(),
    val not: String = ""
)
