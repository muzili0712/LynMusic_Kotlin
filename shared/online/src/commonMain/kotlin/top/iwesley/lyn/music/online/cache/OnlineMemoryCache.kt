package top.iwesley.lyn.music.online.cache

/**
 * 轻量 LRU 内存缓存；M0 使用纯 Kotlin 实现，避免 `LinkedHashMap(accessOrder)` 仅 JVM 可用造成的
 * expect/actual 样板。利用 [LinkedHashMap] 的插入序即为访问序：`get` 命中时先 remove 再重新 put，
 * 使其被视为最近访问；超过 [capacity] 时从队头（最久未使用）逐出。
 *
 * 线程安全：**调用方保证单协程访问**。M0 Repository 层封装自身逻辑在单一 suspend 调用链里，
 * 不会并发访问同一 cache 实例；如未来需要并发，可在此处加 `kotlinx.coroutines.sync.Mutex`。
 *
 * M0 策略：
 * - search / lyric / pic 缓存；
 * - URL 不缓存（有效期短，由 [top.iwesley.lyn.music.online.types.PlayableUrl.isExpired] 判定）。
 */
class OnlineMemoryCache<V : Any>(private val capacity: Int = 128) {

    init {
        require(capacity > 0) { "capacity must be > 0, got $capacity" }
    }

    private val map = LinkedHashMap<String, V>()

    /** 命中则提升为最近访问（remove + re-put）；未命中返回 `null`。 */
    fun get(key: String): V? {
        val v = map.remove(key) ?: return null
        map[key] = v
        return v
    }

    /** 写入；若超 [capacity] 则从最久未使用的条目开始逐出。 */
    fun put(key: String, value: V) {
        map.remove(key)
        map[key] = value
        while (map.size > capacity) {
            val eldest = map.keys.iterator().next()
            map.remove(eldest)
        }
    }

    /** 精确失效一条。 */
    fun invalidate(key: String) {
        map.remove(key)
    }

    /** 清空全部条目。 */
    fun clear() {
        map.clear()
    }

    /** 当前条目数，主要供测试 / 诊断使用。 */
    val size: Int get() = map.size
}
