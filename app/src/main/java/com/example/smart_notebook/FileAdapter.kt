// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(
    private val filePaths: List<String>,
    private val onFileClick: (String) -> Unit,
    private val onFileLongClick: (String) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileIcon: ImageView = itemView.findViewById(R.id.fileIcon)
        val fileName: TextView = itemView.findViewById(R.id.fileName)
        val fileType: TextView = itemView.findViewById(R.id.fileType)
        val fileSize: TextView = itemView.findViewById(R.id.fileSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val path = filePaths[position]
        val file = File(path)

        holder.fileName.text = file.name
        holder.fileType.text = getFileType(file.extension)
        holder.fileSize.text = formatFileSize(file.length())
        holder.fileIcon.setImageDrawable(
            ContextCompat.getDrawable(holder.itemView.context, getFileIcon(file.extension))
        )

        holder.itemView.setOnClickListener { onFileClick(path) }
        holder.itemView.setOnLongClickListener {
            onFileLongClick(path)
            true
        }
    }

    override fun getItemCount(): Int = filePaths.size

    private fun getFileType(extension: String): String {
        return when (extension.lowercase()) {
            "txt" -> "Text File"
            "pdf" -> "PDF Document"
            "doc", "docx" -> "Word Document"
            "xls", "xlsx" -> "Excel Spreadsheet"
            "ppt", "pptx" -> "PowerPoint Presentation"
            "jpg", "jpeg", "png", "gif", "bmp" -> "Image"
            "mp3", "wav", "flac" -> "Audio"
            "mp4", "avi", "mkv" -> "Video"
            "zip", "rar", "7z" -> "Archive"
            "apk" -> "Android App"
            "json", "xml" -> "Data File"
            "html", "htm" -> "Web Page"
            "csv" -> "CSV File"
            "md" -> "Markdown"
            else -> "${extension.uppercase()} File"
        }
    }

    private fun getFileIcon(extension: String): Int {
        return when (extension.lowercase()) {
            "txt" -> android.R.drawable.ic_menu_edit
            "pdf" -> android.R.drawable.ic_menu_agenda
            "doc", "docx" -> android.R.drawable.ic_menu_agenda
            "xls", "xlsx" -> android.R.drawable.ic_menu_agenda
            "ppt", "pptx" -> android.R.drawable.ic_menu_agenda
            "jpg", "jpeg", "png", "gif", "bmp" -> android.R.drawable.ic_menu_gallery
            "mp3", "wav", "flac" -> android.R.drawable.ic_media_play
            "mp4", "avi", "mkv" -> android.R.drawable.ic_media_play
            "zip", "rar", "7z" -> android.R.drawable.ic_menu_upload
            "apk" -> android.R.drawable.ic_menu_agenda
            "json", "xml" -> android.R.drawable.ic_menu_agenda
            "html", "htm" -> android.R.drawable.ic_menu_agenda
            "csv" -> android.R.drawable.ic_menu_agenda
            "md" -> android.R.drawable.ic_menu_edit
            else -> android.R.drawable.ic_menu_upload
        }
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
}