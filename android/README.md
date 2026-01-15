# iiSU Asset Tool - Android

Native Android port of iiSU Asset Tool with a touch-friendly interface.

## Features

This Android version includes:

- **Icon Generator**: Search and download game icons
- **Cover Generator**: Search and download cover artwork
- **Custom Image**: Apply borders to your own images
- **Settings**: Theme selection (Dark/Light/System)

### Removed PC-Exclusive Features

The following features are only available in the desktop version:
- ADB device browsing (requires PC connection)
- ROM file browser (uses PC file system)
- Direct device file management

## Building

### Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Build Steps

1. Open the `android` folder in Android Studio
2. Sync Gradle files
3. Build > Build APK(s)

### Signed Release Build

```bash
./gradlew assembleRelease
```

## Touch-Friendly UI

The app is designed for touch interaction:

- **Minimum touch targets**: 48dp (Material Design guidelines)
- **Large buttons**: 56dp height for easy tapping
- **Bottom navigation**: Optimized for thumb reach
- **Swipeable tabs**: Navigate between features easily
- **Large text**: 16sp minimum for readability

## Assets

Copy the following from the main project to `app/src/main/assets/`:

- `borders/` - Border PNG files
- `platform_icons/` - Platform icon PNGs
- `fonts/` - TTF font files

## Architecture

```
com.iisu.assettool/
├── MainActivity.kt          # Main entry point
├── ui/
│   ├── IconGeneratorFragment.kt
│   ├── CoverGeneratorFragment.kt
│   ├── CustomImageFragment.kt
│   └── SettingsFragment.kt
├── data/
│   ├── Platform.kt          # Gaming platform enum
│   ├── ArtworkResult.kt     # Search result model
│   └── ArtworkScraper.kt    # Web scraping logic
└── util/
    ├── ImageProcessor.kt    # Bitmap manipulation
    └── BorderAdapter.kt     # RecyclerView adapter
```

## License

Same license as the main iiSU Asset Tool project.
