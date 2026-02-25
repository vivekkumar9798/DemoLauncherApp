package com.example.demolauncharapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demolauncharapp.R
import com.example.demolauncharapp.adapter.AppSelectionAdapter
import com.example.demolauncharapp.databinding.ActivityPermissionBinding
import com.example.demolauncharapp.databinding.DialogAppSelectionBinding
import com.example.demolauncharapp.helper.AppInfo

class PermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding
    private lateinit var prefs: SharedPreferences

    companion object {
        const val PREFS_NAME = "launcher_prefs"
        const val KEY_FIRST_RUN = "first_run_done"
        const val KEY_HAS_CHOSEN_APPS = "has_chosen_apps"
        const val KEY_SELECTED_APPS = "selected_apps"
        private const val REQ_WALLPAPER = 1001
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
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        refreshStatusLabels()
    }

    override fun onResume() {
        super.onResume()
        refreshStatusLabels()
    }

    private fun setupClickListeners() {
        // Card: App Access (Android 11+ needs QUERY_ALL_PACKAGES via special permissions settings)
        binding.cardAllApps.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
        }

        // Card: Wallpaper permission
        binding.cardWallpaper.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SET_WALLPAPER)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SET_WALLPAPER),
                    REQ_WALLPAPER
                )
            }
        }

        // Card: Default Launcher
        binding.cardDefaultLauncher.setOnClickListener {
            openDefaultLauncherSettings()
        }

        // Card: Choose App
        binding.cardChooseApp.setOnClickListener {
            showChooseAppDialog()
        }

        // Continue button — always navigates to home
        binding.continueButton.setOnClickListener {
            goToHome()
        }
    }

    // ─── Choose App Dialog ────────────────────────────────────────────────────

    @SuppressLint("NotifyDataSetChanged")
    private fun showChooseAppDialog() {
        val installedApps = loadLaunchableApps()
        if (installedApps.isEmpty()) {
            Toast.makeText(this, "No apps found", Toast.LENGTH_SHORT).show()
            return
        }

        val currentSelected = prefs.getStringSet(KEY_SELECTED_APPS, null) ?: emptySet()
        // If never chosen before, default = all selected
        val initialSelected = if (currentSelected.isEmpty() && !prefs.getBoolean(KEY_HAS_CHOSEN_APPS, false)) {
            installedApps.map { it.packageName }.toSet()
        } else {
            currentSelected
        }

        val dialogBinding = DialogAppSelectionBinding.inflate(LayoutInflater.from(this))
        val adapter = AppSelectionAdapter(installedApps, initialSelected)

        dialogBinding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(this@PermissionActivity)
            this.adapter = adapter
            setHasFixedSize(true)
        }

        // Reflect initial "Select All" state
        dialogBinding.checkBoxSelectAll.isChecked = adapter.isAllSelected()

        dialogBinding.checkBoxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) adapter.selectAll() else adapter.deselectAll()
        }

        val dialog = AlertDialog.Builder(this, R.style.DarkDialogTheme)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnSaveApps.setOnClickListener {
            val selected = adapter.getSelectedPackages()
            prefs.edit()
                .putBoolean(KEY_HAS_CHOSEN_APPS, true)
                .putStringSet(KEY_SELECTED_APPS, selected)
                .apply()
            // Update status label
            refreshStatusLabels()
            dialog.dismiss()
            Toast.makeText(this, "${selected.size} app(s) selected", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
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

    // ─── Default Launcher ─────────────────────────────────────────────────────

    private fun openDefaultLauncherSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    // ─── Status Labels ────────────────────────────────────────────────────────

    private fun refreshStatusLabels() {
        // App access validation
        val appAccessGranted = checkAppAccessPermission()
        if (appAccessGranted) {
            binding.statusAllApps.text = "✓"
            binding.statusAllApps.setTextColor(getColor(R.color.permission_granted))
            binding.indicatorAllApps.visibility = View.VISIBLE
        } else {
            binding.statusAllApps.text = "Grant"
            binding.statusAllApps.setTextColor(getColor(R.color.launcher_accent))
            binding.indicatorAllApps.visibility = View.GONE
        }

        // Wallpaper permission validation
        val wallpaperGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SET_WALLPAPER
        ) == PackageManager.PERMISSION_GRANTED

        if (wallpaperGranted) {
            binding.statusWallpaper.text = "✓"
            binding.statusWallpaper.setTextColor(getColor(R.color.permission_granted))
            binding.indicatorWallpaper.visibility = View.VISIBLE
        } else {
            binding.statusWallpaper.text = "Grant"
            binding.statusWallpaper.setTextColor(getColor(R.color.launcher_accent))
            binding.indicatorWallpaper.visibility = View.GONE
        }

        // Default Launcher validation
        val isDefaultLauncher = checkIsDefaultLauncher()
        if (isDefaultLauncher) {
            binding.statusDefaultLauncher.text = "✓"
            binding.statusDefaultLauncher.setTextColor(getColor(R.color.permission_granted))
            binding.indicatorDefaultLauncher.visibility = View.VISIBLE
        } else {
            binding.statusDefaultLauncher.text = "Set"
            binding.statusDefaultLauncher.setTextColor(getColor(R.color.launcher_accent))
            binding.indicatorDefaultLauncher.visibility = View.GONE
        }

        // Choose App validation - Always true since handled in separate screen
        val hasChosen = prefs.getBoolean(KEY_HAS_CHOSEN_APPS, false)
        val count = prefs.getStringSet(KEY_SELECTED_APPS, null)?.size ?: 0
        if (hasChosen && count > 0) {
            binding.statusChooseApp.text = "$count ✓"
            binding.statusChooseApp.setTextColor(getColor(R.color.permission_granted))
            binding.indicatorChooseApp.visibility = View.VISIBLE
        } else {
            binding.statusChooseApp.text = "Already Selected"
            binding.statusChooseApp.setTextColor(getColor(R.color.permission_granted))
            binding.indicatorChooseApp.visibility = View.VISIBLE
        }

        // Update continue button state
        updateContinueButtonState()
    }

    private fun checkAppAccessPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            true // Android 10 and below don't need special permission
        } else {
            // Android 11+ - Check if we can query all packages
            try {
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA).isNotEmpty()
            } catch (e: SecurityException) {
                false
            }
        }
    }

    private fun checkIsDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        )
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun updateContinueButtonState() {
        val allPermissionsGranted = checkAppAccessPermission() &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_GRANTED &&
                checkIsDefaultLauncher() &&
                prefs.getBoolean(KEY_HAS_CHOSEN_APPS, false) &&
                (prefs.getStringSet(KEY_SELECTED_APPS, null)?.isNotEmpty() == true)

        binding.continueButton.isEnabled = allPermissionsGranted
        binding.continueButton.alpha = if (allPermissionsGranted) 1.0f else 0.5f
        
        if (allPermissionsGranted) {
            binding.continueButton.text = "Continue to Launcher"
        } else {
            binding.continueButton.text = "Complete All Permissions"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshStatusLabels()
    }

    private fun goToHome() {
        val allPermissionsGranted = checkAppAccessPermission() &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_GRANTED &&
                checkIsDefaultLauncher() &&
                prefs.getBoolean(KEY_HAS_CHOSEN_APPS, false) &&
                (prefs.getStringSet(KEY_SELECTED_APPS, null)?.isNotEmpty() == true)

        if (!allPermissionsGranted) {
            // Show warning dialog
            AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("Please complete all required permissions before continuing to the launcher.")
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show()
            return
        }

        // Mark first run as complete
        prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply()
        
        // Navigate to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}