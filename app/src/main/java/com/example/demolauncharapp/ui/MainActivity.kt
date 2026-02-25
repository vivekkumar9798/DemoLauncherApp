package com.example.demolauncharapp.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.demolauncharapp.adapter.AppAdapter
import com.example.demolauncharapp.databinding.ActivityMainBinding
import com.example.demolauncharapp.helper.AppInfo
import android.content.SharedPreferences

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appAdapter: AppAdapter
    private var allApps = listOf<AppInfo>()

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowFlags()
        setupBackPressedCallback()
        setupRecyclerView()
        loadInstalledApps()
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    override fun onResume() {
        super.onResume()
        if (!isDefaultLauncher()) {
            val prefs = getSharedPreferences(PermissionActivity.PREFS_NAME, MODE_PRIVATE)
            prefs.edit().putBoolean(PermissionActivity.KEY_FIRST_RUN, false).apply()
            startActivity(Intent(this, PermissionActivity::class.java))
            finish()
            return
        }

        loadInstalledApps()
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing to keep user in launcher
            }
        })
    }

    private fun setupRecyclerView() {
        appAdapter = AppAdapter(onAppClick = { launchApp(it.packageName) }, onAppLongClick = { })
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 4)
            adapter = appAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadInstalledApps() {
        val prefs: SharedPreferences = getSharedPreferences(PermissionActivity.PREFS_NAME, MODE_PRIVATE)
        val hasChosen = prefs.getBoolean(PermissionActivity.KEY_HAS_CHOSEN_APPS, false)
        val selectedPackages: Set<String>? = if (hasChosen) prefs.getStringSet(PermissionActivity.KEY_SELECTED_APPS, null) else null

        val installed = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val apps = installed
            .filter { appInfo ->
                if (appInfo.packageName == packageName) return@filter false
                if (!appInfo.enabled) return@filter false
                if (packageManager.getLaunchIntentForPackage(appInfo.packageName) == null) return@filter false
                val isUser = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
                if (!(isUser || !isHiddenSystemApp(appInfo.packageName))) return@filter false
                selectedPackages == null || selectedPackages.contains(appInfo.packageName)
            }
            .distinctBy { it.packageName }
            .mapNotNull { appInfo ->
                try {
                    val icon = appInfo.loadIcon(packageManager)
                    val label = appInfo.loadLabel(packageManager).toString()
                    AppInfo(appInfo.packageName, label, icon, 0xFF5B8CFF.toInt())
                } catch (_: Exception) { null }
            }
            .sortedBy { it.label.lowercase() }

        appAdapter.submitList(apps)
    }

    private fun launchApp(packageName: String) {
        packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun isHiddenSystemApp(packageName: String): Boolean {
        return setOf("android", "com.android.systemui", "com.android.phone").contains(packageName)
    }
}
