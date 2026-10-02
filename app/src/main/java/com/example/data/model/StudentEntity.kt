package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ogrenciler")
data class StudentEntity(
    @PrimaryKey
    val ogrenci_id: String, // kullanıcı adı
    val ogrenci_parola: String,
    val ogrenci_ad: String,
    val ogrenci_soyad: String,
    val ogrenci_telno: String,
    val ogrenci_hedef: String, // hedef meslek (ör. Tıp Fakültesi, Bilgisayar Mühendisliği)
    val ogrenci_puan: Double, // hedef puan (ör. 490.0)
    val sinif_bilgisi: String, // ör. 12. Sınıf, Mezun vb.
    val secilen_ogretmen_id: String, // seçilen öğretmenin id'si
    val createdAt: Long = System.currentTimeMillis()
)
