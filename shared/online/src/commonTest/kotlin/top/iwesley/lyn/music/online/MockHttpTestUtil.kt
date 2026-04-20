package top.iwesley.lyn.music.online

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel

/**
 * 单源 URL resolver 测试共用的 MockEngine helper。
 *
 * 此前 Tx/Wy/Kw/Kg/Mg 五个测试文件各自复制了同一份 mockHttp / mockHttpCapturing
 * （外加 Tx/Wy 的 bodyText），合计约 50 行冗余；抽到本 util 避免 M1.1+ 加源时
 * 再复制一次，也便于未来统一调整 MockEngine DSL。
 */
internal fun mockHttp(
    json: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpClient = HttpClient(MockEngine { _ ->
    respond(
        content = ByteReadChannel(json),
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
})

/**
 * 捕获所有出站请求到 [captured]，全部返回相同响应；供 shape assertion 测试使用。
 */
internal fun mockHttpCapturing(
    captured: MutableList<HttpRequestData>,
    json: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpClient = HttpClient(MockEngine { req ->
    captured += req
    respond(
        content = ByteReadChannel(json),
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
})

/**
 * 从 MockEngine 抓到的请求中提取 body 文本。ktor 把 `setBody(String)` + `contentType(Json)`
 * 包装成 [TextContent]；若未来换 ktor 版本走 [OutgoingContent.ByteArrayContent]，也兜底解码。
 */
internal fun bodyText(req: HttpRequestData): String = when (val b = req.body) {
    is TextContent -> b.text
    is OutgoingContent.ByteArrayContent -> b.bytes().decodeToString()
    else -> error("unsupported outgoing body: ${b::class.simpleName}")
}
