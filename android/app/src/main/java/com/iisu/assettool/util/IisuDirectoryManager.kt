package com.iisu.assettool.util

import android.os.Environment
import android.util.Log
import java.io.File

/**
 * Manages access to the iiSU directory on the device.
 *
 * iiSU stores its data in Android/data folder:
 * /storage/emulated/0/Android/data/com.iisu.network/files/
 *
 * Directory structure:
 * files/
 * ├── platforms/           - Platform assets (icons, titles, list images)
 * │   ├── {platform}.png   - Main platform icon
 * │   ├── {platform}_list.png - List view icon
 * │   ├── {platform}_list_selected.png - Selected list icon
 * │   └── {platform}_title.png - Platform title header
 * ├── {platform}/          - Per-platform ROM folders
 * │   ├── media/           - Scraped media for ROMs
 * │   │   ├── icons/       - Game icons
 * │   │   ├── covers/      - Box art / covers
 * │   │   ├── screenshots/ - Screenshots
 * │   │   └── videos/      - Video previews
 * │   └── *.rom            - ROM files
 * └── .nomedia             - Prevents media scanner indexing
 */
object IisuDirectoryManager {

    private const val TAG = "IisuDirectoryManager"

    // Possible iiSU package names
    private val IISU_PACKAGE_NAMES = listOf(
        "com.iisulauncher",      // Discovered package name (no dot)
        "com.iisu.network",
        "com.iisu.launcher",
        "com.iisu",
        "network.iisu"
    )

    private const val PLATFORMS_DIR = "platforms"
    private const val MEDIA_DIR = "media"
    private const val ICONS_DIR = "icons"
    private const val COVERS_DIR = "covers"
    private const val SCREENSHOTS_DIR = "screenshots"

    // Cache the found root directory
    private var cachedRoot: File? = null

    // User-configured custom ROM path (persisted via SharedPreferences in the app)
    private var customRomPath: File? = null

    // Known platform folder names (common emulator/ROM organization names)
    private val KNOWN_PLATFORM_NAMES = setOf(
        // Nintendo
        "nes", "famicom", "fds", "snes", "sfc", "n64", "gc", "gamecube", "wii", "wiiu", "switch",
        "gb", "gbc", "gba", "nds", "3ds", "virtualboy",
        // Sony
        "psx", "ps1", "playstation", "ps2", "ps3", "psp", "vita",
        // Sega
        "genesis", "megadrive", "md", "sms", "mastersystem", "gamegear", "gg", "saturn", "dreamcast", "dc", "segacd", "scd", "32x",
        // Atari
        "atari2600", "atari5200", "atari7800", "atarilynx", "lynx", "jaguar",
        // Other
        "arcade", "mame", "fba", "neogeo", "neo-geo", "ngp", "ngpc", "pcengine", "pce", "tg16", "turbografx",
        "wonderswan", "ws", "wsc", "msx", "msx2", "coleco", "colecovision", "intellivision",
        "amstrad", "cpc", "zxspectrum", "spectrum", "c64", "commodore64", "amiga",
        "dos", "scummvm", "ports"
    )

    /**
     * Check if a folder name looks like a gaming platform folder.
     * Uses known platform names to validate.
     */
    fun looksLikePlatformFolder(name: String): Boolean {
        val normalized = name.lowercase().replace("-", "").replace("_", "").replace(" ", "")
        return KNOWN_PLATFORM_NAMES.any { platform ->
            normalized == platform || normalized.contains(platform)
        }
    }

    /**
     * Set a custom ROM folder path selected by the user.
     */
    fun setCustomRomPath(path: File?) {
        customRomPath = path
        cachedRoot = null  // Clear cache to force re-detection
        Log.d(TAG, "Custom ROM path set to: ${path?.absolutePath ?: "null"}")
    }

    /**
     * Get the current custom ROM path, if set.
     */
    fun getCustomRomPath(): File? = customRomPath

    /**
     * Get the root iiSU directory by searching known locations.
     *
     * iiSU may store data in various locations:
     * - /Android/data/{package}/files/ - Standard app storage
     * - /Android/data/{package}/files/iiSU/ - Subfolder within files
     * - /Android/data/{package}/ - Directly in package folder
     * - /iiSU/ - Legacy root location
     */
    fun getIisuRoot(): File {
        // Return cached if valid
        cachedRoot?.let {
            if (it.exists() && it.isDirectory) return it
        }

        // First priority: user-configured custom ROM path
        customRomPath?.let { customPath ->
            if (customPath.exists() && customPath.isDirectory) {
                // Just check if it has subdirectories that look like platform folders
                val hasPlatformFolders = customPath.listFiles()?.any { dir ->
                    dir.isDirectory && !dir.name.startsWith(".") && looksLikePlatformFolder(dir.name)
                } == true

                if (hasPlatformFolders) {
                    Log.d(TAG, "Using custom ROM path: ${customPath.absolutePath}")
                    cachedRoot = customPath
                    return customPath
                }
            }
        }

        // Check the known iiSU Launcher media path first (most common location)
        val iisuMediaPath = File(
            Environment.getExternalStorageDirectory(),
            "Android/media/com.iisulauncher/iiSULauncher/assets/media/roms/consoles"
        )
        if (iisuMediaPath.exists() && iisuMediaPath.isDirectory) {
            val hasPlatformFolders = iisuMediaPath.listFiles()?.any { dir ->
                dir.isDirectory && !dir.name.startsWith(".") && looksLikePlatformFolder(dir.name)
            } == true

            if (hasPlatformFolders) {
                Log.d(TAG, "Found iiSU ROMs at media path: ${iisuMediaPath.absolutePath}")
                cachedRoot = iisuMediaPath
                return iisuMediaPath
            }
        }

        // Search in Android/data for iiSU package folders
        val androidDataDir = File(Environment.getExternalStorageDirectory(), "Android/data")

        if (androidDataDir.exists() && androidDataDir.canRead()) {
            // First try known package names
            for (packageName in IISU_PACKAGE_NAMES) {
                val packageDir = File(androidDataDir, packageName)
                val found = findIisuDataInPackage(packageDir)
                if (found != null) {
                    Log.d(TAG, "Found iiSU root at known package: ${found.absolutePath}")
                    cachedRoot = found
                    return found
                }
            }

            // Search for any folder containing "iisu" (case insensitive)
            androidDataDir.listFiles()?.forEach { packageDir ->
                if (packageDir.name.contains("iisu", ignoreCase = true)) {
                    Log.d(TAG, "Found iiSU package: ${packageDir.name}")
                    val found = findIisuDataInPackage(packageDir)
                    if (found != null) {
                        Log.d(TAG, "Found iiSU root via search: ${found.absolutePath}")
                        cachedRoot = found
                        return found
                    }
                }
            }
        }

        // Fallback: check legacy location /storage/emulated/0/iiSU
        val legacyRoot = File(Environment.getExternalStorageDirectory(), "iiSU")
        if (legacyRoot.exists() && legacyRoot.isDirectory) {
            Log.d(TAG, "Found iiSU at legacy location: ${legacyRoot.absolutePath}")
            cachedRoot = legacyRoot
            return legacyRoot
        }

        // Check if media/assets are stored in shared locations (common with iiSU/ES-DE setups)
        val sharedLocations = listOf(
            File(Environment.getExternalStorageDirectory(), "ROMs"),
            File(Environment.getExternalStorageDirectory(), "ES-DE/ROMs"),
            File(Environment.getExternalStorageDirectory(), "roms")
        )

        for (location in sharedLocations) {
            if (location.exists() && location.isDirectory) {
                // Check if it has platform-named subdirectories
                val hasPlatforms = location.listFiles()?.any { platformDir ->
                    platformDir.isDirectory && !platformDir.name.startsWith(".") &&
                    looksLikePlatformFolder(platformDir.name)
                } == true

                if (hasPlatforms) {
                    Log.d(TAG, "Found assets at shared location: ${location.absolutePath}")
                    cachedRoot = location
                    return location
                }
            }
        }

        // Return default path even if it doesn't exist
        Log.d(TAG, "iiSU root not found, returning default path")
        return File(Environment.getExternalStorageDirectory(), "Android/data/com.iisu.network/files")
    }

    /**
     * Search within a package directory for iiSU data.
     * Checks multiple possible locations where iiSU might store its data.
     */
    private fun findIisuDataInPackage(packageDir: File): File? {
        if (!packageDir.exists() || !packageDir.isDirectory) return null

        Log.d(TAG, "Searching package: ${packageDir.absolutePath}")

        // List all contents for debugging
        val contents = packageDir.listFiles()
        Log.d(TAG, "  Package contents: ${contents?.map { it.name }}")

        // Check various possible data locations in order of preference
        val possibleLocations = listOf(
            File(packageDir, "files"),           // Standard: /files/
            File(packageDir, "files/iiSU"),      // Subfolder: /files/iiSU/
            File(packageDir, "files/iisu"),      // Lowercase variant
            File(packageDir, "iiSU"),            // Direct: /iiSU/
            File(packageDir, "iisu"),            // Lowercase direct
            packageDir                            // Package root itself
        )

        for (location in possibleLocations) {
            if (location.exists() && location.isDirectory) {
                Log.d(TAG, "  Checking location: ${location.absolutePath}")
                val locationContents = location.listFiles()
                Log.d(TAG, "    Contents: ${locationContents?.map { it.name }}")

                // Check if this location has iiSU content (platforms folder or platform-named directories)
                val hasPlatforms = File(location, PLATFORMS_DIR).exists()
                val hasPlatformDirs = locationContents?.any { file ->
                    file.isDirectory &&
                    !file.name.startsWith(".") &&
                    file.name != PLATFORMS_DIR &&
                    file.name != "cache" &&
                    file.name != "shared_prefs" &&
                    file.name != "databases" &&
                    // Check if folder name looks like a platform (nes, snes, psx, etc.)
                    looksLikePlatformFolder(file.name)
                } == true

                if (hasPlatforms || hasPlatformDirs) {
                    Log.d(TAG, "  Found valid iiSU data at: ${location.absolutePath}")
                    return location
                }
            }
        }

        // If no content found but files dir exists, still return it (might be newly installed)
        val filesDir = File(packageDir, "files")
        if (filesDir.exists() && filesDir.isDirectory) {
            Log.d(TAG, "  Returning files dir (empty but exists): ${filesDir.absolutePath}")
            return filesDir
        }

        return null
    }

    /**
     * Check if iiSU is installed (directory exists with content).
     * Returns true if we can find any ROM directories with content.
     */
    fun isIisuInstalled(): Boolean {
        val root = getIisuRoot()
        val exists = root.exists() && root.isDirectory

        if (exists) {
            // Check if it has platform directories with ROMs
            val platforms = getPlatformsWithRoms()
            val hasPlatforms = platforms.isNotEmpty()

            // Also check for any non-system content
            val hasContent = root.listFiles()?.any {
                it.isDirectory &&
                !it.name.startsWith(".") &&
                it.name !in listOf("cache", "shared_prefs", "databases", "lib", "code_cache")
            } == true

            Log.d(TAG, "isIisuInstalled: exists=$exists, hasPlatforms=$hasPlatforms, platforms=$platforms, hasContent=$hasContent, path=${root.absolutePath}")
            return hasPlatforms || hasContent
        }

        Log.d(TAG, "isIisuInstalled: false (path=${root.absolutePath})")
        return false
    }

    /**
     * Clear the cached root directory (call if user changes iiSU installation).
     */
    fun clearCache() {
        cachedRoot = null
    }

    /**
     * Get the platforms directory containing platform assets.
     */
    fun getPlatformsDir(): File {
        return File(getIisuRoot(), PLATFORMS_DIR)
    }

    /**
     * Get all available platforms by scanning the platforms directory.
     */
    fun getAvailablePlatforms(): List<String> {
        val platformsDir = getPlatformsDir()
        if (!platformsDir.exists()) return emptyList()

        return platformsDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".png") && !it.name.contains("_") }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()
    }

    /**
     * Get all platforms that have media/asset directories.
     * Looks for folders with platform names (nes, snes, psx, etc.).
     */
    fun getPlatformsWithRoms(): List<String> {
        val root = getIisuRoot()
        if (!root.exists()) return emptyList()

        // System directories to ignore
        val systemDirs = setOf(
            PLATFORMS_DIR, "cache", "shared_prefs", "databases", "lib",
            "code_cache", "files", "no_backup", "app_webview"
        )

        return root.listFiles()
            ?.filter { dir ->
                dir.isDirectory &&
                !dir.name.startsWith(".") &&
                dir.name.lowercase() !in systemDirs &&
                // Check if folder name looks like a platform
                looksLikePlatformFolder(dir.name)
            }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }

    /**
     * Get the directory for a specific platform's ROMs.
     */
    fun getPlatformDir(platform: String): File {
        return File(getIisuRoot(), platform)
    }

    /**
     * Get the media directory for a specific platform.
     */
    fun getPlatformMediaDir(platform: String): File {
        return File(getPlatformDir(platform), MEDIA_DIR)
    }

    /**
     * Get the icons directory for a platform.
     */
    fun getPlatformIconsDir(platform: String): File {
        return File(getPlatformMediaDir(platform), ICONS_DIR)
    }

    /**
     * Get the covers directory for a platform.
     */
    fun getPlatformCoversDir(platform: String): File {
        return File(getPlatformMediaDir(platform), COVERS_DIR)
    }

    /**
     * Get the screenshots directory for a platform.
     */
    fun getPlatformScreenshotsDir(platform: String): File {
        return File(getPlatformMediaDir(platform), SCREENSHOTS_DIR)
    }

    /**
     * Get platform icon file.
     */
    fun getPlatformIcon(platform: String): File {
        return File(getPlatformsDir(), "$platform.png")
    }

    /**
     * Get platform list icon.
     */
    fun getPlatformListIcon(platform: String): File {
        return File(getPlatformsDir(), "${platform}_list.png")
    }

    /**
     * Get platform list selected icon.
     */
    fun getPlatformListSelectedIcon(platform: String): File {
        return File(getPlatformsDir(), "${platform}_list_selected.png")
    }

    /**
     * Get platform title image.
     */
    fun getPlatformTitle(platform: String): File {
        return File(getPlatformsDir(), "${platform}_title.png")
    }

    /**
     * Create necessary directories for a platform's media.
     */
    fun ensurePlatformMediaDirs(platform: String): Boolean {
        return try {
            getPlatformIconsDir(platform).mkdirs()
            getPlatformCoversDir(platform).mkdirs()
            getPlatformScreenshotsDir(platform).mkdirs()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the icon file path for a specific game.
     * In iiSU, each game has its own folder inside the platform folder.
     */
    fun getGameIconPath(platform: String, gameName: String): File {
        // Icon is inside the game's folder
        return File(File(getPlatformDir(platform), gameName), "icon.png")
    }

    /**
     * Get the screenshot file path for a specific game.
     */
    fun getGameScreenshotPath(platform: String, gameName: String): File {
        return File(File(getPlatformDir(platform), gameName), "screenshot.png")
    }

    /**
     * List all games for a platform.
     * In iiSU, each game is represented by a folder (not a ROM file).
     * The folder name is the game title used for database searches.
     *
     * External asset naming (iiSU default):
     * - icon: icon.jpg
     * - hero: hero_1.jpg, hero_2.jpg, etc.
     * - logo: title.jpg
     * - screenshots: slide_1.png, slide_2.png, etc.
     *
     * App-generated asset naming:
     * - icon: icon.png
     * - hero: hero_1.png
     * - logo: title.png
     */
    fun getGamesForPlatform(platform: String): List<GameInfo> {
        val platformDir = getPlatformDir(platform)
        if (!platformDir.exists()) return emptyList()

        return platformDir.listFiles()
            ?.filter { file ->
                file.isDirectory &&
                !file.name.startsWith(".") &&
                file.name != MEDIA_DIR &&
                file.name != "cache"
            }
            ?.map { gameFolder ->
                // Check for icon: icon.png (app) or icon.jpg (external)
                val iconFile = findAssetFile(gameFolder, "icon")

                // Check for screenshot/slides: slide_1.png, etc. (external) or screenshot.png (app)
                val screenshotFile = findAssetFile(gameFolder, "screenshot")
                    ?: findSlideFile(gameFolder)

                // Check for hero: hero_1.png (app) or hero_1.jpg, hero_2.jpg, etc. (external)
                val heroFile = findAssetFile(gameFolder, "hero")
                    ?: findHeroFile(gameFolder)

                // Check for logo: title.png (app) or title.jpg (external)
                val logoFile = findAssetFile(gameFolder, "logo")
                    ?: findAssetFile(gameFolder, "title")

                GameInfo(
                    name = gameFolder.name,
                    folder = gameFolder,
                    hasIcon = iconFile != null,
                    hasScreenshot = screenshotFile != null,
                    hasHero = heroFile != null,
                    hasLogo = logoFile != null,
                    iconFile = iconFile,
                    screenshotFile = screenshotFile,
                    heroFile = heroFile,
                    logoFile = logoFile
                )
            }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    /**
     * Find an asset file with the given base name (checks png, jpg, jpeg extensions).
     */
    private fun findAssetFile(folder: File, baseName: String): File? {
        val extensions = listOf("png", "jpg", "jpeg")
        for (ext in extensions) {
            val file = File(folder, "$baseName.$ext")
            if (file.exists()) return file
        }
        return null
    }

    /**
     * Find external hero file (hero_1.jpg, hero_2.jpg, etc.).
     * Returns the first one found.
     */
    private fun findHeroFile(folder: File): File? {
        val extensions = listOf("jpg", "jpeg", "png")
        for (i in 1..10) {
            for (ext in extensions) {
                val file = File(folder, "hero_$i.$ext")
                if (file.exists()) return file
            }
        }
        return null
    }

    /**
     * Find external slide/screenshot file (slide_1.png, slide_2.png, etc.).
     * Returns the first one found.
     */
    private fun findSlideFile(folder: File): File? {
        val extensions = listOf("png", "jpg", "jpeg")
        for (i in 1..10) {
            for (ext in extensions) {
                val file = File(folder, "slide_$i.$ext")
                if (file.exists()) return file
            }
        }
        return null
    }

    /**
     * Get games that are missing icons.
     */
    fun getGamesMissingIcons(platform: String): List<GameInfo> {
        return getGamesForPlatform(platform).filter { !it.hasIcon }
    }

    /**
     * Get games that are missing heroes.
     */
    fun getGamesMissingHeroes(platform: String): List<GameInfo> {
        return getGamesForPlatform(platform).filter { !it.hasHero }
    }

    /**
     * Get games that are missing logos.
     */
    fun getGamesMissingLogos(platform: String): List<GameInfo> {
        return getGamesForPlatform(platform).filter { !it.hasLogo }
    }

    // Legacy compatibility - these call the new game-based methods
    @Deprecated("Use getGamesForPlatform instead", ReplaceWith("getGamesForPlatform(platform)"))
    fun getRomsForPlatform(platform: String): List<File> {
        return getGamesForPlatform(platform).map { it.folder }
    }

    @Deprecated("Use getGamesMissingIcons instead", ReplaceWith("getGamesMissingIcons(platform)"))
    fun getRomsMissingIcons(platform: String): List<File> {
        return getGamesMissingIcons(platform).map { it.folder }
    }
}

/**
 * Information about a game in iiSU.
 * Each game has its own folder with assets inside.
 *
 * External asset naming (iiSU default):
 * - icon: icon.jpg
 * - hero: hero_1.jpg, hero_2.jpg, etc.
 * - logo: title.jpg
 * - screenshots: slide_1.png, slide_2.png, etc.
 *
 * App-generated asset naming:
 * - icon: icon.png
 * - hero: hero_1.png
 * - logo: title.png
 */
data class GameInfo(
    val name: String,           // Game title (folder name) - raw name for file operations
    val folder: File,           // The game's folder
    val hasIcon: Boolean,       // Whether icon file exists
    val hasScreenshot: Boolean = false, // Whether screenshot file exists
    val hasHero: Boolean = false,     // Whether hero file exists
    val hasLogo: Boolean = false,     // Whether logo file exists
    val iconFile: File? = null,       // The actual icon file (png or jpg)
    val screenshotFile: File? = null, // The actual screenshot file (png or jpg)
    val heroFile: File? = null,       // The actual hero file (png or jpg)
    val logoFile: File? = null        // The actual logo file (png or jpg)
) {
    /**
     * Whether the icon was generated by this app.
     * App saves as icon.png, external is icon.jpg
     */
    val iconGeneratedByApp: Boolean
        get() = iconFile?.name == "icon.png"

    /**
     * Whether the hero was generated by this app.
     * App saves as hero_1.png, external is hero_1.jpg, hero_2.jpg, etc.
     */
    val heroGeneratedByApp: Boolean
        get() = heroFile?.name?.let { it.startsWith("hero_") && it.endsWith(".png") } == true

    /**
     * Whether the logo was generated by this app.
     * App saves as title.png, external is title.jpg
     */
    val logoGeneratedByApp: Boolean
        get() = logoFile?.name == "title.png"

    /**
     * Whether the screenshot was generated by this app.
     * App saves as screenshot.png, external is slide_1.png, slide_2.png, etc.
     */
    val screenshotGeneratedByApp: Boolean
        get() = screenshotFile?.name == "screenshot.png"

    /**
     * Cleaned display name with region tags, version info, etc. removed.
     * Use this for UI display.
     */
    val displayName: String by lazy {
        TitleCleaner.cleanForDisplay(name)
    }

    /**
     * Normalized name for searching APIs.
     * More aggressive cleaning for better search results.
     */
    val searchName: String by lazy {
        TitleCleaner.normalizeForSearch(name)
    }

    /**
     * Get multiple search variants to try if first search fails.
     */
    fun getSearchVariants(): List<String> {
        return TitleCleaner.getSearchVariants(name)
    }
}
