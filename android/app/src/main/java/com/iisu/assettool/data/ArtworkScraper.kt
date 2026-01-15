package com.iisu.assettool.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Simple artwork scraper for the standalone Icon/Cover generator tabs.
 * Uses Libretro thumbnails as the primary source (no API key required).
 */
class ArtworkScraper {

    companion object {
        private const val LIBRETRO_BASE_URL = "https://thumbnails.libretro.com"
        private const val TIMEOUT = 15000
    }

    /**
     * Search for game icons.
     */
    suspend fun searchIcons(query: String, platform: Platform): List<ArtworkResult> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<ArtworkResult>()

            // Try Libretro thumbnails
            try {
                results.addAll(searchLibretro(query, platform, ArtworkType.ICON))
            } catch (e: Exception) {
                // Silently fail
            }

            results
        }
    }

    /**
     * Search for game cover art.
     */
    suspend fun searchCovers(query: String, platform: Platform): List<ArtworkResult> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<ArtworkResult>()

            try {
                results.addAll(searchLibretro(query, platform, ArtworkType.COVER))
            } catch (e: Exception) {
                // Silently fail
            }

            results
        }
    }

    /**
     * Search Libretro thumbnail repository.
     */
    private fun searchLibretro(
        query: String,
        platform: Platform,
        type: ArtworkType
    ): List<ArtworkResult> {
        val results = mutableListOf<ArtworkResult>()

        // Map platform to Libretro naming convention
        val platformDir = when (platform) {
            Platform.NES -> "Nintendo - Nintendo Entertainment System"
            Platform.SNES -> "Nintendo - Super Nintendo Entertainment System"
            Platform.N64 -> "Nintendo - Nintendo 64"
            Platform.GAMEBOY -> "Nintendo - Game Boy"
            Platform.GBA -> "Nintendo - Game Boy Advance"
            Platform.DS -> "Nintendo - Nintendo DS"
            Platform.PS1 -> "Sony - PlayStation"
            Platform.PS2 -> "Sony - PlayStation 2"
            Platform.PSP -> "Sony - PlayStation Portable"
            Platform.GENESIS -> "Sega - Mega Drive - Genesis"
            Platform.DREAMCAST -> "Sega - Dreamcast"
            Platform.ARCADE -> "MAME"
            else -> return results
        }

        val artType = when (type) {
            ArtworkType.ICON -> "Named_Boxarts"
            ArtworkType.COVER -> "Named_Boxarts"
            else -> "Named_Snaps"
        }

        // Construct thumbnail URL
        val cleanQuery = query.replace(" ", "_")
            .replace(":", "")
            .replace("?", "")
            .replace("/", "_")
            .replace("\\", "_")

        val baseUrl = "$LIBRETRO_BASE_URL/${URLEncoder.encode(platformDir, "UTF-8")}/$artType/${URLEncoder.encode(cleanQuery, "UTF-8")}.png"

        // Check if the URL is valid by making a HEAD request
        try {
            val url = URL(baseUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                results.add(
                    ArtworkResult(
                        url = baseUrl,
                        title = query,
                        platform = platform,
                        type = type
                    )
                )
            }
            connection.disconnect()
        } catch (e: Exception) {
            // URL doesn't exist or network error
        }

        // Also try some common variations
        val variations = listOf(
            cleanQuery,
            "${cleanQuery}_(USA)",
            "${cleanQuery}_(Europe)",
            "${cleanQuery}_(Japan)",
            "${cleanQuery}_(World)"
        )

        for (variation in variations.drop(1)) { // Skip first since we already tried it
            try {
                val varUrl = "$LIBRETRO_BASE_URL/${URLEncoder.encode(platformDir, "UTF-8")}/$artType/${URLEncoder.encode(variation, "UTF-8")}.png"
                val url = URL(varUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    results.add(
                        ArtworkResult(
                            url = varUrl,
                            title = "$query ($variation)",
                            platform = platform,
                            type = type
                        )
                    )
                }
                connection.disconnect()
            } catch (e: Exception) {
                // Continue with next variation
            }
        }

        return results
    }
}
