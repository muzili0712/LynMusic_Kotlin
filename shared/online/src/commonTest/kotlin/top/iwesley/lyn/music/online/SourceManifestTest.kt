package top.iwesley.lyn.music.online

import top.iwesley.lyn.music.online.types.SourceManifest
import top.iwesley.lyn.music.online.types.SourceMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SourceManifestTest {

    @Test
    fun five_sources_enabled_for_M0() {
        val enabled = SourceManifest.enabled.map { it.id }.toSet()
        assertEquals(
            expected = setOf("kw", "kg", "tx", "wy", "mg"),
            actual = enabled,
            message = "M0 should enable exactly kw/kg/tx/wy/mg; got $enabled",
        )
    }

    @Test
    fun xm_is_disabled_with_reason() {
        val xm = SourceManifest.byId("xm")
        assertNotNull(xm, "xm entry must exist in manifest")
        assertEquals(false, xm.enabled, "xm must be disabled in M0")
        val reason = xm.disabledReason
        assertNotNull(reason, "xm must declare a disabledReason")
        assertTrue(reason.isNotBlank(), "xm disabledReason must not be blank")
    }

    @Test
    fun enabled_sources_all_claim_search_url_lyric_pic_methods() {
        val required = setOf(
            SourceMethod.Search,
            SourceMethod.Url,
            SourceMethod.Lyric,
            SourceMethod.Pic,
        )
        SourceManifest.enabled.forEach { source ->
            assertTrue(
                source.methods.containsAll(required),
                "source ${source.id} must declare all of $required but has ${source.methods}",
            )
        }
    }
}
