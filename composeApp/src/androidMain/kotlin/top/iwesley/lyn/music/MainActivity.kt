package top.iwesley.lyn.music

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import top.iwesley.lyn.music.platform.createAndroidAppComponent

class MainActivity : ComponentActivity() {
    private lateinit var appComponent: LynMusicAppComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val isTablet = resources.configuration.smallestScreenWidthDp >= 600
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        appComponent = createAndroidAppComponent(this)

        setContent {
            App(appComponent)
        }
    }
}
