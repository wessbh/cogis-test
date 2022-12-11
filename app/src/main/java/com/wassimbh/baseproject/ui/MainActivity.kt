package com.wassimbh.baseproject.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.wassimbh.baseproject.R
import com.wassimbh.baseproject.utilities.Coroutines
import com.wassimbh.baseproject.utilities.launchFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemUI()
    }

    /**
     * function to enable full screen
     */
    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        else{
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(
                window,
                findViewById(R.id.main_container)
            ).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}