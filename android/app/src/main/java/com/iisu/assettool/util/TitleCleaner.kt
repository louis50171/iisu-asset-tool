package com.iisu.assettool.util

import java.text.Normalizer

/**
 * Utility class for cleaning ROM filenames and folder names to extract clean game titles.
 * Removes region tags, version info, dump info, file extensions, file sizes, etc.
 * Matches the desktop Python version's clean_game_title() function.
 */
object TitleCleaner {

    // Common ROM file extensions
    private val ROM_EXTENSIONS = setOf(
        // Nintendo
        "nes", "nez", "unf", "unif", "smc", "sfc", "fig", "swc",
        "n64", "z64", "v64", "ndd", "iso", "gcm", "gcz", "rvz", "wbfs", "ciso",
        "wia", "wud", "wux", "rpx", "nsp", "xci", "nsz", "xcz",
        "gb", "gbc", "sgb", "gba", "agb", "nds", "dsi", "3ds", "cia", "cxi",
        "vb", "vboy",
        // Sony
        "bin", "cue", "img", "pbp", "chd", "cso", "pkg", "vpk", "mai",
        // Microsoft
        "xbe", "xex", "god",
        // Sega
        "sms", "sg", "md", "gen", "smd", "32x", "gdi", "cdi", "gg",
        // SNK
        "ngp", "ngc",
        // Atari
        "a26", "a52", "a78", "j64", "jag", "rom", "lnx", "lyx",
        // Other
        "col", "int", "pce", "sgx", "ws", "wsc", "scummvm",
        // Archives
        "zip", "7z", "rar"
    )

    // Region codes that appear in parentheses
    private val REGION_TAGS = setOf(
        "usa", "us", "europe", "eu", "japan", "jp", "world", "wld",
        "en", "fr", "de", "es", "it", "ja", "ko", "zh",
        "ntsc", "pal", "ntsc-u", "ntsc-j", "pal-e"
    )

    // Version/revision patterns
    private val VERSION_PATTERNS = listOf(
        Regex("""\s*\(Rev\s*[A-Z0-9]*\)""", RegexOption.IGNORE_CASE),
        Regex("""\s*\(v\d+[.\d]*\)""", RegexOption.IGNORE_CASE),
        Regex("""\s*v\d+(\.\d+)*""", RegexOption.IGNORE_CASE),
        Regex("""\s*version\s*\d+(\.\d+)*""", RegexOption.IGNORE_CASE)
    )

    // Special tags to remove
    private val SPECIAL_TAGS = setOf(
        "proto", "beta", "alpha", "demo", "sample", "unl", "pirate",
        "virtual console", "switch", "nsw", "ps4", "ps5", "xbox", "xb1", "pc"
    )

    /**
     * Clean a ROM filename/folder name to extract a clean game title.
     * Removes region tags, version info, dump info, file extensions, file sizes, etc.
     *
     * @param name The raw ROM filename or folder name
     * @return A cleaned game title suitable for display and searching
     */
    fun cleanGameTitle(name: String): String {
        var result = name

        // Remove file extension if present
        val extensionPattern = ROM_EXTENSIONS.joinToString("|") { Regex.escape(it) }
        result = result.replace(Regex("""\.(${extensionPattern})$""", RegexOption.IGNORE_CASE), "")

        // Remove square bracket tags like [!], [U], [E], [J], [h], [b], [T+Eng], etc.
        result = result.replace(Regex("""\s*\[[^\]]*\]"""), "")

        // Remove file size patterns like (6.01 GB), (1.2 MB), (500 KB), (123456789)
        result = result.replace(
            Regex("""\s*\(\s*\d+\.?\d*\s*(GB|MB|KB|B|bytes?)?\s*\)""", RegexOption.IGNORE_CASE),
            ""
        )
        // Also handle standalone numbers in parens (often file sizes without units)
        result = result.replace(Regex("""\s*\(\s*\d{6,}\s*\)"""), "")

        // Remove parenthetical region tags
        val regionPattern = REGION_TAGS.joinToString("|") { Regex.escape(it) }
        result = result.replace(
            Regex("""\s*\((${regionPattern})\)""", RegexOption.IGNORE_CASE),
            ""
        )

        // Remove multi-language region tags like (En,Fr,De)
        result = result.replace(
            Regex("""\s*\([A-Za-z]{2}(,[A-Za-z]{2})+\)"""),
            ""
        )

        // Remove special tags
        val specialPattern = SPECIAL_TAGS.joinToString("|") { Regex.escape(it) }
        result = result.replace(
            Regex("""\s*\((${specialPattern})\)""", RegexOption.IGNORE_CASE),
            ""
        )

        // Remove version patterns
        for (pattern in VERSION_PATTERNS) {
            result = result.replace(pattern, "")
        }

        // Remove parenthetical disc numbers like (Disc 1), (Disc 2 of 3)
        result = result.replace(
            Regex("""\s*\(Disc\s*\d+[^)]*\)""", RegexOption.IGNORE_CASE),
            ""
        )

        // Remove update/DLC/patch tags
        result = result.replace(
            Regex("""\s*\+?\s*(Update|DLC|Patch|Fix|Hotfix)\s*v?\d*(\.\d+)*""", RegexOption.IGNORE_CASE),
            ""
        )

        // Remove any remaining empty parentheses
        result = result.replace(Regex("""\s*\(\s*\)"""), "")

        // Normalize whitespace
        result = result.replace(Regex("""\s+"""), " ").trim()

        // Remove trailing dashes, underscores, or dots
        result = result.trimEnd('-', '_', '.', ' ')

        return result
    }

    /**
     * Normalize a game title for search - handles accented characters,
     * special characters, and common variations.
     * Returns a search-friendly version of the name.
     *
     * @param name The game title to normalize
     * @return A search-friendly normalized title
     */
    fun normalizeForSearch(name: String): String {
        // First clean the title
        var result = cleanGameTitle(name)

        // Normalize unicode - decompose accented characters and remove combining marks
        result = Normalizer.normalize(result, Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")

        // Common character substitutions
        val replacements = mapOf(
            '&' to "and",
            '+' to "plus",
            '@' to "at",
            '\u2122' to "",  // TM
            '\u00ae' to "",  // (R)
            '\u00a9' to "",  // (C)
            '\u2018' to "'", // left single quote
            '\u2019' to "'", // right single quote
            '\u201c' to "\"", // left double quote
            '\u201d' to "\"", // right double quote
            '\u2013' to "-", // en dash
            '\u2014' to "-", // em dash
            '\u2026' to "..." // ellipsis
        )
        for ((old, new) in replacements) {
            result = result.replace(old.toString(), new)
        }

        // Remove most punctuation but keep apostrophes and hyphens for names
        result = result.replace(Regex("""[^\w\s'-]"""), " ")

        // Normalize whitespace
        result = result.replace(Regex("""\s+"""), " ").trim()

        return result
    }

    /**
     * Generate multiple search variants for a game title.
     * Useful for trying different search terms if the first doesn't match.
     *
     * @param name The original game title
     * @return A list of search variants to try
     */
    fun getSearchVariants(name: String): List<String> {
        val variants = mutableListOf<String>()

        // Original cleaned name
        val clean = cleanGameTitle(name)
        if (clean.isNotEmpty()) {
            variants.add(clean)
        }

        // Normalized (no accents) version
        val normalized = normalizeForSearch(name)
        if (normalized.isNotEmpty() && normalized != clean) {
            variants.add(normalized)
        }

        // Try without subtitles (text after : or -)
        if (':' in clean) {
            val mainTitle = clean.split(':')[0].trim()
            if (mainTitle.isNotEmpty() && mainTitle !in variants) {
                variants.add(mainTitle)
            }
        }

        if (" - " in clean) {
            val mainTitle = clean.split(" - ")[0].trim()
            if (mainTitle.isNotEmpty() && mainTitle !in variants) {
                variants.add(mainTitle)
            }
        }

        // Handle roman numerals vs numbers (e.g., "III" vs "3")
        val romanMap = listOf(
            """\bIII\b""" to "3",
            """\bII\b""" to "2",
            """\bIV\b""" to "4",
            """\bVI\b""" to "6",
            """\bVII\b""" to "7",
            """\bVIII\b""" to "8",
            """\bIX\b""" to "9",
            """\bXI\b""" to "11",
            """\bXII\b""" to "12"
        )
        for ((pattern, replacement) in romanMap) {
            if (Regex(pattern).containsMatchIn(clean)) {
                val variant = clean.replace(Regex(pattern), replacement)
                if (variant !in variants) {
                    variants.add(variant)
                }
            }
        }

        return variants
    }

    /**
     * Clean a title specifically for display purposes.
     * This is less aggressive than search normalization.
     *
     * @param name The raw title
     * @return A clean display title
     */
    fun cleanForDisplay(name: String): String {
        return cleanGameTitle(name)
    }
}
