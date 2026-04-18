package top.iwesley.lyn.music.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OnlineSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OnlineSongEntity)

    @Query("SELECT * FROM online_song WHERE source = :source AND songmid = :songmid LIMIT 1")
    suspend fun find(source: String, songmid: String): OnlineSongEntity?

    @Query("DELETE FROM online_song WHERE source = :source AND songmid = :songmid")
    suspend fun delete(source: String, songmid: String)
}
