package com.iisu.assettool.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.iisu.assettool.R
import com.iisu.assettool.databinding.FragmentCustomImageBinding
import com.iisu.assettool.util.BorderAdapter
import com.iisu.assettool.util.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

/**
 * Custom Image Fragment
 *
 * Touch-friendly interface for selecting custom images and applying borders.
 * Features:
 * - Large image picker button
 * - Grid of available borders with large touch targets
 * - Real-time preview
 * - One-tap save to gallery
 */
class CustomImageFragment : Fragment() {

    private var _binding: FragmentCustomImageBinding? = null
    private val binding get() = _binding!!

    private var selectedImageBitmap: Bitmap? = null
    private var selectedBorderBitmap: Bitmap? = null
    private var processedBitmap: Bitmap? = null
    private val imageProcessor = ImageProcessor()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSelectImageButton()
        setupBorderGrid()
        setupApplyButton()
        setupSaveButton()
    }

    private fun setupSelectImageButton() {
        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }
    }

    private fun setupBorderGrid() {
        val borders = loadBordersFromAssets()
        val adapter = BorderAdapter(borders) { borderBitmap ->
            selectedBorderBitmap = borderBitmap
            binding.buttonApply.isEnabled = selectedImageBitmap != null
            Toast.makeText(context, "Border selected", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewBorders.apply {
            layoutManager = GridLayoutManager(context, 3)
            this.adapter = adapter
        }
    }

    private fun loadBordersFromAssets(): List<Bitmap> {
        val borders = mutableListOf<Bitmap>()
        try {
            val assetManager = requireContext().assets
            val borderFiles = assetManager.list("borders") ?: emptyArray()

            for (filename in borderFiles) {
                if (filename.endsWith(".png")) {
                    assetManager.open("borders/$filename").use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                            borders.add(bitmap)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error loading borders
        }
        return borders
    }

    private fun loadImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }

                bitmap?.let {
                    selectedImageBitmap = it
                    binding.imagePreview.setImageBitmap(it)
                    binding.buttonApply.isEnabled = selectedBorderBitmap != null
                    Toast.makeText(context, "Image loaded", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupApplyButton() {
        binding.buttonApply.setOnClickListener {
            val image = selectedImageBitmap
            val border = selectedBorderBitmap

            if (image != null && border != null) {
                applyBorder(image, border)
            }
        }
    }

    private fun applyBorder(image: Bitmap, border: Bitmap) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    imageProcessor.applyBorder(image, border)
                }

                processedBitmap = result
                binding.imagePreview.setImageBitmap(result)
                binding.buttonSave.isEnabled = true
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to apply border", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            processedBitmap?.let { bitmap ->
                saveToGallery(bitmap)
            } ?: Toast.makeText(context, "No image to save", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToGallery(bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val filename = "iiSU_Custom_${System.currentTimeMillis()}.png"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/iiSU Asset Tool")
                        }
                    }

                    val resolver = requireContext().contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                    uri?.let {
                        val outputStream: OutputStream? = resolver.openOutputStream(it)
                        outputStream?.use { stream ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        }
                    }
                }
                Toast.makeText(context, R.string.success_saved, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
