package top.iwesley.lyn.music.online.source

import kotlinx.serialization.json.Json

/**
 * 在线源响应解析统一用这个 Json 实例。
 *
 * - ignoreUnknownKeys=true：所有真实源的响应都包含大量无关字段
 * - isLenient=true：部分源（kw 的某些端点）返回非严格 JSON（单引号、裸 key）
 * - coerceInputValues=true：拿到 null 时退回 default，避免解析中断
 *
 * 仅内部使用；外部 API/UI 用默认 Json 即可。
 */
internal val OnlineJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}
