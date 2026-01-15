package com.iisu.assettool.util

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iisu.assettool.R
import com.iisu.assettool.databinding.ItemGameBinding

/**
 * RecyclerView adapter for displaying games in a platform.
 * Shows game icon, name, asset status, and action buttons.
 * Buttons are always enabled - tap to generate/replace artwork.
 */
class GameAdapter(
    private val onGenerateIcon: (GameInfo) -> Unit,
    private val onGenerateHero: ((GameInfo) -> Unit)? = null,
    private val onGenerateLogo: ((GameInfo) -> Unit)? = null
) : ListAdapter<GameInfo, GameAdapter.GameViewHolder>(GameDiffCallback()) {

    inner class GameViewHolder(
        private val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameInfo) {
            // Set game name (use cleaned display name)
            binding.textGameName.text = game.displayName

            // Set game icon (supports both PNG and JPG)
            if (game.hasIcon && game.iconFile != null) {
                val bitmap = BitmapFactory.decodeFile(game.iconFile.absolutePath)
                if (bitmap != null) {
                    binding.imageGameIcon.setImageBitmap(bitmap)
                } else {
                    binding.imageGameIcon.setImageResource(R.drawable.ic_missing_icon)
                }
            } else {
                binding.imageGameIcon.setImageResource(R.drawable.ic_missing_icon)
            }

            // Set icon status - differentiate between app-generated (PNG) and external (JPG)
            if (game.hasIcon) {
                if (game.iconGeneratedByApp) {
                    binding.textIconStatus.text = "Icon: ✓"  // App-generated
                    binding.textIconStatus.setTextColor(
                        binding.root.context.getColor(R.color.accent_cyan)
                    )
                } else {
                    binding.textIconStatus.text = "Icon: ext"  // External/pre-existing
                    binding.textIconStatus.setTextColor(
                        binding.root.context.getColor(R.color.iisu_purple)
                    )
                }
                binding.iconMissingIcon.visibility = View.GONE
            } else {
                binding.textIconStatus.text = "Icon: ✗"
                binding.textIconStatus.setTextColor(
                    binding.root.context.getColor(R.color.accent_magenta)
                )
                binding.iconMissingIcon.visibility = View.VISIBLE
            }

            // Configure icon button - always enabled, shows replace indicator if exists
            binding.btnGenerateIcon.apply {
                isEnabled = true
                alpha = 1.0f
                // Change tint to indicate replace vs generate
                setColorFilter(
                    binding.root.context.getColor(
                        if (game.hasIcon) R.color.iisu_purple else R.color.accent_cyan
                    )
                )
                contentDescription = if (game.hasIcon) "Replace Icon" else "Generate Icon"
                setOnClickListener {
                    onGenerateIcon(game)
                }
            }

            // Set hero status
            binding.textHeroStatus.apply {
                if (game.hasHero) {
                    if (game.heroGeneratedByApp) {
                        text = "Hero: ✓"
                        setTextColor(binding.root.context.getColor(R.color.accent_cyan))
                    } else {
                        text = "Hero: ext"
                        setTextColor(binding.root.context.getColor(R.color.iisu_purple))
                    }
                } else {
                    text = "Hero: ✗"
                    setTextColor(binding.root.context.getColor(R.color.accent_magenta))
                }
            }

            // Set logo status
            binding.textLogoStatus.apply {
                if (game.hasLogo) {
                    if (game.logoGeneratedByApp) {
                        text = "Logo: ✓"
                        setTextColor(binding.root.context.getColor(R.color.accent_cyan))
                    } else {
                        text = "Logo: ext"
                        setTextColor(binding.root.context.getColor(R.color.iisu_purple))
                    }
                } else {
                    text = "Logo: ✗"
                    setTextColor(binding.root.context.getColor(R.color.accent_magenta))
                }
            }

            // Configure hero button - always enabled if callback provided
            binding.btnGenerateHero.apply {
                if (onGenerateHero != null) {
                    visibility = View.VISIBLE
                    isEnabled = true
                    alpha = 1.0f
                    setColorFilter(
                        binding.root.context.getColor(
                            if (game.hasHero) R.color.iisu_purple else R.color.accent_cyan
                        )
                    )
                    contentDescription = if (game.hasHero) "Replace Hero" else "Generate Hero"
                    setOnClickListener { onGenerateHero.invoke(game) }
                } else {
                    visibility = View.GONE
                }
            }

            // Configure logo button - always enabled if callback provided
            binding.btnGenerateLogo.apply {
                if (onGenerateLogo != null) {
                    visibility = View.VISIBLE
                    isEnabled = true
                    alpha = 1.0f
                    setColorFilter(
                        binding.root.context.getColor(
                            if (game.hasLogo) R.color.iisu_purple else R.color.accent_magenta
                        )
                    )
                    contentDescription = if (game.hasLogo) "Replace Logo" else "Generate Logo"
                    setOnClickListener { onGenerateLogo.invoke(game) }
                } else {
                    visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class GameDiffCallback : DiffUtil.ItemCallback<GameInfo>() {
    override fun areItemsTheSame(oldItem: GameInfo, newItem: GameInfo): Boolean {
        return oldItem.folder.absolutePath == newItem.folder.absolutePath
    }

    override fun areContentsTheSame(oldItem: GameInfo, newItem: GameInfo): Boolean {
        return oldItem == newItem
    }
}
