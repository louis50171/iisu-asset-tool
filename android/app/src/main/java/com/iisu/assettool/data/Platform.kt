package com.iisu.assettool.data

/**
 * Supported gaming platforms for artwork scraping.
 * Matches the platforms from the desktop Python version.
 */
enum class Platform(val displayName: String, val searchId: String) {
    // Nintendo
    NES("NES", "nes"),
    SNES("SNES", "snes"),
    N64("Nintendo 64", "n64"),
    GAMECUBE("GameCube", "gamecube"),
    WII("Wii", "wii"),
    GAMEBOY("Game Boy", "gb"),
    GBA("Game Boy Advance", "gba"),
    DS("Nintendo DS", "nds"),
    THREEDS("Nintendo 3DS", "3ds"),
    SWITCH("Nintendo Switch", "switch"),

    // Sony
    PS1("PlayStation", "ps1"),
    PS2("PlayStation 2", "ps2"),
    PS3("PlayStation 3", "ps3"),
    PS4("PlayStation 4", "ps4"),
    PS5("PlayStation 5", "ps5"),
    PSP("PSP", "psp"),
    VITA("PS Vita", "vita"),

    // Microsoft
    XBOX("Xbox", "xbox"),
    XBOX360("Xbox 360", "xbox360"),
    XBOXONE("Xbox One", "xboxone"),
    XBOXSERIES("Xbox Series X|S", "xboxseries"),

    // Sega
    GENESIS("Sega Genesis", "genesis"),
    SATURN("Sega Saturn", "saturn"),
    DREAMCAST("Dreamcast", "dreamcast"),
    GAMEGEAR("Game Gear", "gamegear"),

    // Other
    ARCADE("Arcade", "arcade"),
    PC("PC", "pc"),
    ATARI2600("Atari 2600", "atari2600"),
    NEOGEO("Neo Geo", "neogeo"),
    TURBOGRAFX("TurboGrafx-16", "tg16"),
}
