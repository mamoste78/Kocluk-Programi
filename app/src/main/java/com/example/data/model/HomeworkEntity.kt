package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "odevler")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ogretmen_id: String,
    val ogrenci_id: String,
    val ders: String,       // ör. Matematik
    val konu: String,       // ör. Parabol ve İkinci Dereceden Denklemler
    val soruSayisi: Int,    // ör. 80
    val aciklama: String = "",
    val sonTeslimTarihi: String = "", // ör. 15 Ekim 2026
    val durum: String = STATUS_BEKLEMEDE, // "BEKLEMEDE", "GORULDU", "BITIRILDI"
    val gorulmeTarihi: Long? = null,
    val bitirmeTarihi: Long? = null,
    val olusturmaTarihi: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_BEKLEMEDE = "BEKLEMEDE"
        const val STATUS_GORULDU = "GORULDU"
        const val STATUS_BITIRILDI = "BITIRILDI"
    }
}
