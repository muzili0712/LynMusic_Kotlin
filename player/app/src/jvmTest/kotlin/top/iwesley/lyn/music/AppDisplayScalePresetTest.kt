package top.iwesley.lyn.music

import kotlin.test.Test
import kotlin.test.assertEquals
import top.iwesley.lyn.music.core.model.AppDisplayScalePreset
import top.iwesley.lyn.music.core.model.appDisplayScalePresetOrDefault
import top.iwesley.lyn.music.core.model.effectiveAppDisplayDensity

class AppDisplayScalePresetTest {
    @Test
    fun `preset scales stay fixed`() {
        assertEquals(0.9f, AppDisplayScalePreset.Compact.scale)
        assertEquals(1.0f, AppDisplayScalePreset.Default.scale)
        assertEquals(1.1f, AppDisplayScalePreset.Large.scale)
    }

    @Test
    fun `invalid preset name falls back to default`() {
        assertEquals(AppDisplayScalePreset.Default, appDisplayScalePresetOrDefault(null))
        assertEquals(AppDisplayScalePreset.Default, appDisplayScalePresetOrDefault("unknown"))
        assertEquals(AppDisplayScalePreset.Compact, appDisplayScalePresetOrDefault("Compact"))
    }

    @Test
    fun `effective density multiplies base density by preset scale`() {
        assertEquals(2.7f, effectiveAppDisplayDensity(3f, AppDisplayScalePreset.Compact), 0.0001f)
        assertEquals(3.0f, effectiveAppDisplayDensity(3f, AppDisplayScalePreset.Default), 0.0001f)
        assertEquals(3.3f, effectiveAppDisplayDensity(3f, AppDisplayScalePreset.Large), 0.0001f)
    }
}
