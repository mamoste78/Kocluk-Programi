package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "denemeler")
data class MockExamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ogrenci_id: String,
    val deneme_adi: String, // ör. 3D Türkiye Geneli Deneme 1
    val yayin_adi: String,  // ör. 3D Yayınları, Bilgi Sarmal, Limit
    val sinav_turu: String, // TYT, AYT, LGS vb.
    val dogru: Int,
    val yanlis: Int,
    val net: Double,
    val puan: Double = 0.0,
    val tarih: Long = System.currentTimeMillis(),
    val not: String = ""
)
