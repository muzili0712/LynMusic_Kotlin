package top.iwesley.lyn.music.online

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import top.iwesley.lyn.music.online.cache.OnlineMemoryCache

/**
 * [OnlineMemoryCache] 契约测试：
 * 1. 基本 put/get 命中；
 * 2. 超容量时按 LRU 策略淘汰最久未使用条目（同时验证 `get` 会刷新 recency）。
 *
 * 命名用下划线：Apple/Android commonTest 不支持反引号空格方法名。
 */
class OnlineMemoryCacheTest {

    @Test
    fun put_then_get_returns_value() {
        val cache = OnlineMemoryCache<String>(capacity = 4)
        cache.put("a", "A")
        cache.put("b", "B")
        assertEquals("A", cache.get("a"))
        assertEquals("B", cache.get("b"))
        assertNull(cache.get("missing"))
    }

    @Test
    fun eviction_drops_least_recently_used_when_over_capacity() {
        val cache = OnlineMemoryCache<String>(capacity = 2)
        cache.put("a", "A")
        cache.put("b", "B")
        // 访问 a 使之成为最近使用
        assertEquals("A", cache.get("a"))
        // 写入 c 应淘汰最久未使用的 b
        cache.put("c", "C")

        assertEquals("A", cache.get("a"))
        assertEquals("C", cache.get("c"))
        assertNull(cache.get("b"))
        assertEquals(2, cache.size)
    }
}
