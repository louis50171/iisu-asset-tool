package com.iisu.assettool.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.iisu.assettool.R
import com.iisu.assettool.databinding.DialogArtworkPickerBinding
import com.iisu.assettool.util.ArtworkOption
import com.iisu.assettool.util.ArtworkSearchResult

/**
 * Dialog for selecting artwork from multiple options.
 * Shows current image (if any), available options with thumbnails,
 * and allows user to select one to save.
 */
class ArtworkPickerDialog(
    context: Context,
    private val artworkType: ArtworkType,
    private val searchResult: ArtworkSearchResult,
    private val onOptionSelected: (ArtworkOption) -> Unit
) : Dialog(context) {

    enum class ArtworkType {
        ICON, HERO, LOGO
    }

    private lateinit var binding: DialogArtworkPickerBinding
    private var selectedOption: ArtworkOption? = null
    private var selectedView: MaterialCardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DialogArtworkPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set dialog size based on orientation
        val isLandscape = context.resources.configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            // In landscape, limit height to 85% of screen height
            val displayMetrics = context.resources.displayMetrics
            val maxHeight = (displayMetrics.heightPixels * 0.85).toInt()
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                maxHeight
            )
        } else {
            // In portrait, wrap content
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        setupUI()
        displayOptions()
    }

    private fun setupUI() {
        // Set title
        val typeText = when (artworkType) {
            ArtworkType.ICON -> "Icon"
            ArtworkType.HERO -> "Hero"
            ArtworkType.LOGO -> "Logo"
        }
        binding.textTitle.text = "Select $typeText for ${searchResult.gameName}"

        // Set subtitle
        val optionCount = searchResult.options.size
        val sources = searchResult.options.map { it.source }.distinct()
        if (optionCount > 0) {
            binding.textSubtitle.text = "Found $optionCount option(s) from ${sources.size} source(s)"
        } else {
            binding.textSubtitle.text = "Searching..."
        }

        // Show current image if exists
        if (searchResult.currentImage != null) {
            binding.layoutCurrentImage.visibility = View.VISIBLE
            binding.imageCurrentArtwork.setImageBitmap(searchResult.currentImage)
        } else {
            binding.layoutCurrentImage.visibility = View.GONE
        }

        // Cancel button
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // Save button
        binding.btnSave.setOnClickListener {
            selectedOption?.let { option ->
                onOptionSelected(option)
                dismiss()
            }
        }
    }

    private fun displayOptions() {
        binding.layoutOptions.removeAllViews()

        if (searchResult.options.isEmpty()) {
            binding.scrollOptions.visibility = View.GONE
            binding.textNoOptions.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            return
        }

        binding.scrollOptions.visibility = View.VISIBLE
        binding.textNoOptions.visibility = View.GONE

        for (option in searchResult.options) {
            val optionView = createOptionView(option)
            binding.layoutOptions.addView(optionView)
        }
    }

    private fun createOptionView(option: ArtworkOption): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_artwork_option, binding.layoutOptions, false)

        val card = view.findViewById<MaterialCardView>(R.id.cardOption)
        val thumbnail = view.findViewById<ImageView>(R.id.imageThumbnail)
        val progress = view.findViewById<ProgressBar>(R.id.progressThumbnail)
        val iconSelected = view.findViewById<ImageView>(R.id.iconSelected)
        val textSource = view.findViewById<TextView>(R.id.textSource)
        val textDimensions = view.findViewById<TextView>(R.id.textDimensions)

        // Set source label
        textSource.text = option.source

        // Set dimensions if available
        if (option.width > 0 && option.height > 0) {
            textDimensions.visibility = View.VISIBLE
            textDimensions.text = "${option.width}x${option.height}"
        } else {
            textDimensions.visibility = View.GONE
        }

        // Set thumbnail
        if (option.thumbnail != null) {
            thumbnail.setImageBitmap(option.thumbnail)
            progress.visibility = View.GONE
        } else {
            // Show placeholder
            thumbnail.setImageResource(R.drawable.ic_missing_icon)
            progress.visibility = View.GONE
        }

        // Click handler
        card.setOnClickListener {
            selectOption(option, card, iconSelected)
        }

        return view
    }

    private fun selectOption(option: ArtworkOption, card: MaterialCardView, iconSelected: ImageView) {
        // Deselect previous
        selectedView?.let { prevCard ->
            prevCard.strokeColor = ContextCompat.getColor(context, R.color.surface_variant)
            prevCard.strokeWidth = 2.dpToPx()
            // Hide previous selection indicator
            prevCard.findViewById<ImageView>(R.id.iconSelected)?.visibility = View.GONE
        }

        // Select new
        selectedOption = option
        selectedView = card

        card.strokeColor = ContextCompat.getColor(context, R.color.accent_cyan)
        card.strokeWidth = 3.dpToPx()
        iconSelected.visibility = View.VISIBLE

        // Enable save button
        binding.btnSave.isEnabled = true
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        /**
         * Show the artwork picker dialog.
         */
        fun show(
            context: Context,
            artworkType: ArtworkType,
            searchResult: ArtworkSearchResult,
            onOptionSelected: (ArtworkOption) -> Unit
        ): ArtworkPickerDialog {
            val dialog = ArtworkPickerDialog(context, artworkType, searchResult, onOptionSelected)
            dialog.show()
            return dialog
        }
    }
}
