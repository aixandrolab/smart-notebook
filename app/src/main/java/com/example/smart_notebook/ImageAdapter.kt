// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ImageAdapter(
    private val imagePaths: List<String>,
    private val onImageClick: (String) -> Unit,
    private val onImageLongClick: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val path = imagePaths[position]
        try {
            val file = File(path)
            if (file.exists()) {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeFile(path, this)

                    val scale = (outWidth / 200).coerceAtLeast(outHeight / 200).coerceAtLeast(1)

                    inSampleSize = scale
                    inJustDecodeBounds = false
                }

                val bitmap = BitmapFactory.decodeFile(path, options)
                holder.imageView.setImageBitmap(bitmap)
            } else {
                holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } catch (_: Exception) {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.imageView.setOnClickListener { onImageClick(path) }
        holder.imageView.setOnLongClickListener {
            onImageLongClick(path)
            true
        }
    }

    override fun getItemCount(): Int = imagePaths.size
}