package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mesajlar")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gonderenId: String,   // ogrenci_id veya ogretmen_id
    val aliciId: String,      // alıcı id
    val gonderenRol: String,  // "OGRENCI" veya "OGRETMEN"
    val gonderenAdi: String,  // gönderenin görünen adı
    val mesaj: String,
    val zamanDamgasi: Long = System.currentTimeMillis()
)
