package com.example.demolauncharapp.ui

import android.R
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.demolauncharapp.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_FIRST_RUN = "first_run_done"
        private const val SPLASH_DELAY = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateSplash()
    }

    private fun animateSplash() {
        // Animate logo group: scale from 0.7 + fade in
        binding.logoGroup.apply {
            scaleX = 0.7f
            scaleY = 0.7f
            alpha = 0f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setStartDelay(150)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // Animate footer fade in
        binding.splashFooter.animate()
            .alpha(0.5f)
            .setDuration(800)
            .setStartDelay(600)
            .start()

        // Navigate after delay
        handler.postDelayed({
            navigateNext()
        }, SPLASH_DELAY)
    }

    private fun navigateNext() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isFirstRun = !prefs.getBoolean(KEY_FIRST_RUN, false)
        val hasChosenApps = prefs.getBoolean(PermissionActivity.KEY_HAS_CHOSEN_APPS, false)

        val intent = when {
            isFirstRun -> {
                Intent(this, AppSelectionActivity::class.java)
            }
            !hasChosenApps -> {
                Intent(this, AppSelectionActivity::class.java)
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Smooth transition
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}