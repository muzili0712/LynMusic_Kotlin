package top.iwesley.lyn.music.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "online_song",
    primaryKeys = ["source", "songmid"],
)
data class OnlineSongEntity(
    val source: String,
    val songmid: String,
    val name: String,
    val singer: String,
    @ColumnInfo(name = "album") val album: String? = null,
    @ColumnInfo(name = "interval_seconds") val intervalSeconds: Int = 0,
    @ColumnInfo(name = "cover_url") val coverUrl: String? = null,
    @ColumnInfo(name = "default_quality") val defaultQuality: String = "320k",
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
