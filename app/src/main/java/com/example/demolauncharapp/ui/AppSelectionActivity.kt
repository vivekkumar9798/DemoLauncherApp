package com.example.demolauncharapp.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.demolauncharapp.adapter.AppSelectionAdapter
import com.example.demolauncharapp.databinding.ActivityAppSelectionBinding
import com.example.demolauncharapp.helper.AppInfo

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var appAdapter: AppSelectionAdapter
    private var allApps = listOf<AppInfo>()

    companion object {
        const val PREFS_NAME = "launcher_prefs"
        const val KEY_HAS_CHOSEN_APPS = "has_chosen_apps"
        const val KEY_SELECTED_APPS = "selected_apps"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Block status bar expansion and system UI access
        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
            android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Full screen setup
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            try {
                val controller = window.insetsController
                controller?.let {
                    it.hide(
                        WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars() or
                        WindowInsets.Type.systemBars() or
                        WindowInsets.Type.displayCutout() or
                        WindowInsets.Type.captionBar() or
                        WindowInsets.Type.ime() or
                        WindowInsets.Type.mandatorySystemGestures()
                    )
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } catch (e: Exception) {
                // Fallback to system UI flags if insets controller fails
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadApps()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        appAdapter = AppSelectionAdapter(emptyList(), emptySet())
        binding.recyclerViewApps.apply {
            layoutManager = GridLayoutManager(this@AppSelectionActivity, 3)
            adapter = appAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadApps() {
        allApps = loadLaunchableApps()
        
        // Don't select any apps by default - user must choose manually
        val initialSelected = prefs.getStringSet(KEY_SELECTED_APPS, null) ?: emptySet()
        
        appAdapter = AppSelectionAdapter(allApps, initialSelected)
        appAdapter.setOnSelectionChanged {
            updateUI()
        }
        binding.recyclerViewApps.adapter = appAdapter
        
        updateUI()
    }

    private fun loadLaunchableApps(): List<AppInfo> {
        return packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                if (appInfo.packageName == packageName) return@filter false
                if (!appInfo.enabled) return@filter false
                if (packageManager.getLaunchIntentForPackage(appInfo.packageName) == null) return@filter false
                val isUser = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
                isUser || packageManager.getLaunchIntentForPackage(appInfo.packageName) != null
            }
            .mapNotNull { appInfo ->
                try {
                    val icon = appInfo.loadIcon(packageManager)
                    val label = appInfo.loadLabel(packageManager).toString()
                    AppInfo(appInfo.packageName, label, icon, 0xFF5B8CFF.toInt())
                } catch (_: Exception) { null }
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun setupClickListeners() {
        binding.checkBoxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) appAdapter.selectAll() else appAdapter.deselectAll()
            updateUI()
        }

        binding.btnContinue.setOnClickListener {
            val selectedApps = appAdapter.getSelectedPackages()
            
            if (selectedApps.isEmpty()) {
                Toast.makeText(this, "Please select at least one app", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save selection
            prefs.edit()
                .putBoolean(KEY_HAS_CHOSEN_APPS, true)
                .putStringSet(KEY_SELECTED_APPS, selectedApps)
                .apply()

            // Navigate to permission screen
            val intent = Intent(this, PermissionActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateUI() {
        val selectedCount = appAdapter.getSelectedPackages().size
        val totalCount = appAdapter.getItemCount()
        
        binding.checkBoxSelectAll.isChecked = appAdapter.isAllSelected()
        binding.tvSelectedCount.text = "$selectedCount of $totalCount apps selected"
        
        // Update continue button state
        binding.btnContinue.isEnabled = selectedCount > 0
        binding.btnContinue.alpha = if (selectedCount > 0) 1.0f else 0.5f
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
