# N Launcher

A minimalist, beautiful Android launcher with smooth animations and modern design.

![N Launcher](screenshot.png)
*Note: Replace screenshot.png with actual app screenshots. See SCREENSHOT_INSTRUCTIONS.md for details.*

## Features

### 🚀 Core Launcher Features
- **Minimalist Design**: Clean, distraction-free home screen
- **Smooth Animations**: Beautiful entrance and touch animations
- **App Grid Layout**: 4xN grid layout with adaptive spacing
- **Dynamic Colors**: Extracts dominant colors from app icons
- **Search Functionality**: Quick app search with real-time filtering

### 🎨 Visual Features
- **Animated App Icons**: Scale animations on touch with ripple effects
- **Staggered Entrance**: Items appear with smooth staggered animation
- **Custom Ripple Effects**: Designed ripple backgrounds for better visual feedback
- **Fullscreen Mode**: True immersive launcher experience
- **Transparent System Bars**: Seamless integration with wallpapers

### 🛡️ Security & Privacy
- **Permission Management**: Granular permission requests for app access
- **App Selection**: Choose which apps to display in launcher
- **System App Filtering**: Automatically hides system apps
- **No Tracking**: Privacy-focused with no analytics or tracking

### ⚙️ Advanced Features
- **Default Launcher Support**: Full HOME intent integration
- **Task Management**: Proper task affinity and launch modes
- **Wallpaper Integration**: Show when locked and turn screen on capabilities
- **System UI Control**: Blocks status bar expansion and system keys

## Screenshots

### Main Features
- **Home Screen**: Clean app grid with smooth animations
- **App Selection**: Choose which apps appear in launcher
- **Permission Setup**: Guided permission flow
- **Splash Screen**: Beautiful branded entry point

## Architecture

### Project Structure
```
app/
├── src/main/java/com/example/demolauncharapp/
│   ├── ui/
│   │   ├── MainActivity.kt          # Main launcher screen
│   │   ├── SplashActivity.kt        # Splash screen
│   │   ├── PermissionActivity.kt    # Permission management
│   │   └── AppSelectionActivity.kt  # App selection screen
│   ├── adapter/
│   │   ├── AppAdapter.kt            # Main app grid adapter
│   │   └── AppSelectionAdapter.kt   # Selection screen adapter
│   └── helper/
│       └── AppInfo.kt               # App data model
├── src/main/res/
│   ├── layout/                      # XML layouts
│   ├── drawable/                    # Custom drawables and ripples
│   ├── anim/                        # Animation definitions
│   └── values/                      # Strings, themes, colors
└── build.gradle.kts                 # App build configuration
```

### Key Components

#### MainActivity
- **Purpose**: Main launcher home screen
- **Features**: App grid, system UI control, launcher mode enforcement
- **Animations**: RecyclerView entrance, item animations

#### AppAdapter
- **Purpose**: RecyclerView adapter for app grid
- **Features**: Touch animations, staggered entrance, ripple effects
- **Performance**: DiffUtil for efficient list updates

#### PermissionActivity
- **Purpose**: Onboarding and permission management
- **Features**: Guided setup, app selection, launcher verification

## Technical Specifications

### Requirements
- **Android Version**: API 24+ (Android 7.0)
- **Target SDK**: API 36 (Android 15)
- **Kotlin Version**: Latest stable
- **Architecture**: MVVM with ViewBinding

### Dependencies
```kotlin
// Core Android
androidx.core.ktx
androidx.appcompat
androidx.constraintlayout

// UI Components
androidx.recyclerview
androidx.cardview
com.google.android.material
androidx.palette

// ViewBinding
buildFeatures { viewBinding = true }
```

### Permissions Used
```xml
<!-- Core launcher permissions -->
<uses-permission android:name="android.permission.EXPAND_STATUS_BAR" />
<uses-permission android:name="android.permission.GET_TASKS" />
<uses-permission android:name="android.permission.REORDER_TASKS" />
<uses-permission android:name="android.permission.SET_WALLPAPER" />
<uses-permission android:name="android.permission.SET_WALLPAPER_HINTS" />

<!-- Android 11+ app visibility -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

## Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 36
- Kotlin plugin

### Build Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device/emulator

### Setting as Default Launcher
1. Install the app
2. Press home button
3. Select "N Launcher" as default
4. Grant necessary permissions

## Customization

### Theme Colors
Edit `res/values/colors.xml` to customize:
- Primary colors
- Ripple effects
- System bar colors

### Grid Layout
Modify `MainActivity.kt`:
```kotlin
// Change grid columns
layoutManager = GridLayoutManager(this, 4) // Change 4 to desired columns
```

### Animations
Animation files in `res/anim/`:
- `scale_down.xml` - Press animation
- `scale_up.xml` - Release animation  
- `item_entrance.xml` - Entrance animation
- `item_entrance_delayed.xml` - Staggered entrance

## Performance Optimizations

### Memory Management
- ViewBinding for view reference management
- DiffUtil for efficient RecyclerView updates
- Proper lifecycle management

### Animation Performance
- Hardware acceleration enabled
- Optimized animation durations
- Minimal overdraw with transparent backgrounds

### Battery Optimization
- No background services
- Efficient app loading with caching
- Minimal wake locks

## Troubleshooting

### Common Issues

#### Launcher Not Setting as Default
- Check if other launchers are set as default
- Clear defaults of current launcher
- Re-enable N Launcher in system settings

#### Apps Not Showing
- Grant QUERY_ALL_PACKAGES permission
- Check app selection screen
- Verify app has launch intent

#### Animation Lag
- Enable hardware acceleration
- Reduce animation scale in developer options
- Check device performance

### Debug Mode
Enable debug logging in `MainActivity.kt`:
```kotlin
companion object {
    private const val TAG = "MainActivity"
    private const val DEBUG = true // Set to true for debug logs
}
```

## Contributing

### Development Guidelines
1. Follow Kotlin coding conventions
2. Use meaningful variable names
3. Add comments for complex logic
4. Test on multiple API levels

### Pull Request Process
1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request with description

## License

This project is open source. See LICENSE file for details.

## Credits

- **Material Design**: Google Design System
- **AndroidX**: Android Jetpack components
- **Kotlin**: Programming language

## Version History

### v1.0.0 (Current)
- Initial release
- Core launcher functionality
- Smooth animations
- Permission management
- App selection

---

**N Launcher** - Minimalist. Beautiful. Fast.
