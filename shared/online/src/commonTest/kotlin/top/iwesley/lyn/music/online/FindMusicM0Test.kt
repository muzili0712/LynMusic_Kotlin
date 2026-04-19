package top.iwesley.lyn.music.online

import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.resolve.FindMusicM0
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.online.types.SearchPage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FindMusicM0Test {

    private fun song(
        src: String,
        id: String,
        name: String,
        singer: String,
        sec: Int,
    ) = OnlineSong(
        id = OnlineMusicId(src, id),
        name = name,
        singer = singer,
        album = null,
        albumId = null,
        intervalSeconds = sec,
        coverUrl = null,
        availableQualities = listOf(Quality.K320),
        defaultQuality = Quality.K320,
    )

    @Test
    fun exact_name_singer_duration_picks_first_match_from_alternative_source() = runTest {
        val target = song("kw", "1", "稻香", "周杰伦", 200)
        val hit = song("kg", "9", "稻香", "周杰伦", 201)
        val facade = FakeMusicSourceFacade(
            searchResults = mapOf(
                ("kg" to "稻香 周杰伦") to SearchPage(listOf(hit), 1, 1, 1, "kg"),
            ),
        )
        val find = FindMusicM0(OnlineMusicRepository(facade))
        val r = find.find(target, excludeSource = "kw")
        assertEquals("kg", r?.id?.source)
    }

    @Test
    fun returns_null_when_no_source_has_matching_name() = runTest {
        val target = song("kw", "1", "稻香", "周杰伦", 200)
        val find = FindMusicM0(OnlineMusicRepository(FakeMusicSourceFacade()))
        assertNull(find.find(target, excludeSource = "kw"))
    }

    @Test
    fun duration_tolerance_rejects_very_different_durations_but_falls_back_to_pass2() = runTest {
        val target = song("kw", "1", "稻香", "周杰伦", 200)
        // 时长差 60s 超出 3s 容差 → Pass 1 fail；但 name + singer 主歌手依然匹配 → Pass 2 命中
        val farOff = song("kg", "9", "稻香", "周杰伦", 260)
        val facade = FakeMusicSourceFacade(
            searchResults = mapOf(
                ("kg" to "稻香 周杰伦") to SearchPage(listOf(farOff), 1, 1, 1, "kg"),
            ),
        )
        val find = FindMusicM0(OnlineMusicRepository(facade), durationToleranceSec = 3)
        val r = find.find(target, excludeSource = "kw")
        assertEquals("kg", r?.id?.source)
    }
}
