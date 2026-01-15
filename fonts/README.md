# Fonts Directory

Place custom font files here for use in the iiSU Asset Tool GUI.

## Typography System

The app uses a dual-font typography system:

| Font | Usage | Style |
|------|-------|-------|
| **Continuum Bold** | Headers, buttons, short labels | Bold, accent text |
| **Cal Sans** | Body text, descriptions, longer sentences | Regular, readable |

## Continuum Bold (Primary - Headers)

The app uses **Continuum Bold** for headers, buttons, and short labels.

### How to Add Continuum Bold

1. **Get the font file:**
   - Purchase or obtain `ContinuumBold.ttf` or `ContinuumBold.otf`
   - Common sources: Adobe Fonts, MyFonts, or licensed font providers

2. **Place in this directory:**
   ```
   fonts/
   └── ContinuumBold.ttf   (or .otf)
   ```

## Cal Sans (Secondary - Body Text)

The app uses **Cal Sans** for body text, descriptions, and longer sentences.

### How to Add Cal Sans

1. **Get the font file:**
   - Download from [Cal.com's GitHub](https://github.com/calcom/font)
   - The file should be named `calsans-regular.ttf`

2. **Place in this directory:**
   ```
   fonts/
   └── calsans-regular.ttf
   ```

## Running the App

```bash
python run_gui.py
```

Both fonts will be automatically loaded and used throughout the interface.

## Supported Formats

- `.ttf` (TrueType Font)
- `.otf` (OpenType Font)

## Font Fallback

If fonts are not found, the app uses these fallback chains:

**For Headers (Continuum Bold):**
1. Continuum Bold (if in fonts/)
2. Continuum (system-installed)
3. Segoe UI Semibold (Windows)
4. system-ui (OS default)

**For Body Text (Cal Sans):**
1. Cal Sans (if in fonts/)
2. CalSans-Regular (variant name)
3. Segoe UI (Windows)
4. system-ui (OS default)

## Custom Fonts

You can add any `.ttf` or `.otf` font files to this directory. They will be loaded automatically.

To use a different font in the theme:

1. Add your font file here
2. Edit `iisu_theme.qss` or `iisu_theme_light.qss`
3. Change the `font-family` property:

```css
/* For body text */
QWidget {
    font-family: "Your Font Name", "Cal Sans", ...;
}

/* For headers/buttons */
QLabel, QPushButton {
    font-family: "Your Header Font", "Continuum Bold", ...;
}
```

## License Note

**Important:** Ensure you have proper licensing for any fonts you use, especially for distribution or commercial use.

- **Continuum** is a commercial font and requires a license
- **Cal Sans** is open source (SIL Open Font License)

## Free Alternatives

**For Continuum Bold (headers):**
- Montserrat Bold (Google Fonts)
- Outfit Bold (Google Fonts)
- Inter Bold (GitHub)

**For Cal Sans (body text):**
- Inter (Google Fonts)
- DM Sans (Google Fonts)
- Plus Jakarta Sans (Google Fonts)

Download from [Google Fonts](https://fonts.google.com/) and place in this directory.
