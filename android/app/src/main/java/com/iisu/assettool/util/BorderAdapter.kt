package com.iisu.assettool.util

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iisu.assettool.databinding.ItemBorderBinding

/**
 * RecyclerView adapter for displaying border thumbnails.
 * Uses large touch targets for touch-friendly interaction.
 */
class BorderAdapter(
    private val borders: List<Bitmap>,
    private val onBorderSelected: (Bitmap) -> Unit
) : RecyclerView.Adapter<BorderAdapter.BorderViewHolder>() {

    private var selectedPosition = -1

    inner class BorderViewHolder(
        private val binding: ItemBorderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bitmap: Bitmap, position: Int) {
            binding.imageBorder.setImageBitmap(bitmap)

            // Highlight selected item
            binding.root.isChecked = position == selectedPosition

            binding.root.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = position

                // Update UI
                notifyItemChanged(previousSelected)
                notifyItemChanged(position)

                // Callback
                onBorderSelected(bitmap)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorderViewHolder {
        val binding = ItemBorderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BorderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BorderViewHolder, position: Int) {
        holder.bind(borders[position], position)
    }

    override fun getItemCount(): Int = borders.size
}
