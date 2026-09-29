// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private var notes: List<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onViewClick: (Note) -> Unit,
    private val onItemLongClick: (Note) -> Boolean = { false },
    private val onItemMove: (Int, Int) -> Unit,
    private val onItemDismiss: (Int) -> Unit,
    private val isSelectionMode: () -> Boolean = { false },
    private val selectedNoteIds: Set<String> = emptySet(),
    private val onDragStart: (RecyclerView.ViewHolder) -> Unit = {}
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val viewButton: ImageView = itemView.findViewById(R.id.viewButton)
        val imageCountText: TextView = itemView.findViewById(R.id.imageCountText)
        val fileCountText: TextView = itemView.findViewById(R.id.fileCountText)
        val imageCountIcon: ImageView = itemView.findViewById(R.id.imageCountIcon)
        val fileCountIcon: ImageView = itemView.findViewById(R.id.fileCountIcon)
        val selectionCheck: ImageView = itemView.findViewById(R.id.selectionCheck)
        val dragHandle: ImageView = itemView.findViewById(R.id.dragHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        val selectionMode = isSelectionMode()
        val isSelected = selectedNoteIds.contains(note.id)

        holder.titleText.text = note.title

        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        holder.dateText.text = dateFormat.format(Date(note.timestamp))

        val imageCount = note.imagePaths.size
        val fileCount = note.filePaths.size

        holder.imageCountText.text = imageCount.toString()
        holder.fileCountText.text = fileCount.toString()

        holder.imageCountIcon.visibility = if (imageCount > 0) View.VISIBLE else View.GONE
        holder.imageCountText.visibility = if (imageCount > 0) View.VISIBLE else View.GONE
        holder.fileCountIcon.visibility = if (fileCount > 0) View.VISIBLE else View.GONE
        holder.fileCountText.visibility = if (fileCount > 0) View.VISIBLE else View.GONE

        // Галочка в стиле приложения
        if (selectionMode) {
            holder.selectionCheck.visibility = View.VISIBLE
            holder.selectionCheck.setImageResource(
                if (isSelected) R.drawable.ic_check_filled
                else R.drawable.ic_check_empty
            )
        } else {
            holder.selectionCheck.visibility = View.GONE
        }

        // Обычные обработчики
        holder.itemView.setOnClickListener { onItemClick(note) }
        holder.itemView.setOnLongClickListener { onItemLongClick(note) }
        holder.viewButton.setOnClickListener { onViewClick(note) }

        holder.dragHandle.setOnLongClickListener {
            if (!selectionMode) {
                onDragStart(holder)
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = notes.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        onItemMove(fromPosition, toPosition)
        val item = notes[fromPosition]
        val mutableList = notes.toMutableList()
        mutableList.removeAt(fromPosition)
        mutableList.add(toPosition, item)
        notes = mutableList
        notifyItemMoved(fromPosition, toPosition)
    }
}