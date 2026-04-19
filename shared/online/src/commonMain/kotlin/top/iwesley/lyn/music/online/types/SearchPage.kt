package top.iwesley.lyn.music.online.types

/**
 * 通用分页结果容器。
 *
 * - [items] 当前页数据。
 * - [page] 1-based 当前页号。
 * - [totalPages] 总页数；未知时可为 0。
 * - [totalItems] 总条数；未知时可为 0。
 * - [sourceId] 返回该页的数据源 id（便于上层聚合多源时归属追踪）。
 */
data class SearchPage<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Int,
    val sourceId: String,
)
