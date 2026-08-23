// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Collections
import java.util.Locale
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabRequestPermission: FloatingActionButton
    private lateinit var navMenu: ImageView
    private lateinit var adapter: NoteAdapter
    private var notes = mutableListOf<Note>()
    private lateinit var prefs: android.content.SharedPreferences
    private var itemTouchHelper: ItemTouchHelper? = null

    private var pendingEditText: TextInputEditText? = null

    companion object {
        private const val REQUEST_SPEECH = 1000
        private const val REQUEST_AUDIO_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        if (!prefs.getBoolean("data_cleared", false)) {
            StorageHelper.clearData(this)
            prefs.edit { putBoolean("data_cleared", true) }
        }

        initViews()
        checkStoragePermission()
        setupDragAndDrop()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        emptyTextView = findViewById(R.id.emptyTextView)
        fabAdd = findViewById(R.id.fabAdd)
        fabRequestPermission = findViewById(R.id.fabRequestPermission)
        navMenu = findViewById(R.id.navMenu)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NoteAdapter(
            notes,
            onItemClick = { note ->
                openNoteDetail(note.id)
            },
            onViewClick = { note ->
                openNoteDetail(note.id)
            },
            onItemMove = { fromPosition, toPosition ->
                StorageHelper.reorderNotes(this, fromPosition, toPosition)
                val item = notes.removeAt(fromPosition)
                notes.add(toPosition, item)
            },
            onItemDismiss = { position ->
                val note = notes[position]
                StorageHelper.deleteNote(this, note.id)
                notes.removeAt(position)
                adapter.notifyItemRemoved(position)
                checkEmptyState()
            }
        )
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener {
            if (hasStoragePermission()) {
                showAddNoteDialog()
            } else {
                showToast("Please grant storage permission first")
                checkStoragePermission()
            }
        }

        fabRequestPermission.setOnClickListener {
            checkStoragePermission()
        }

        navMenu.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun setupDragAndDrop() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(notes, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(notes, i, i - 1)
                    }
                }
                adapter.notifyItemMoved(fromPosition, toPosition)

                val order = notes.map { it.id }
                StorageHelper.saveOrder(this@MainActivity, order)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = notes[position]

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete \"${note.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        StorageHelper.deleteNote(this@MainActivity, note.id)
                        notes.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        checkEmptyState()
                        showToast("Note deleted")
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

            override fun getDragDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return ItemTouchHelper.UP or ItemTouchHelper.DOWN
            }
        }

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    private fun openNoteDetail(noteId: String) {
        try {
            val intent = Intent(this, NoteDetailActivity::class.java)
            intent.putExtra("note_id", noteId)
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Error opening note: ${e.message}")
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.nav_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_about -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_help -> {
                    val intent = Intent(this, HelpActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkStoragePermission() {
        if (hasStoragePermission()) {
            fabRequestPermission.visibility = View.GONE
            loadNotes()
        } else {
            fabRequestPermission.visibility = View.VISIBLE
            emptyTextView.text = "Storage permission required"
            emptyTextView.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Uri.fromParts("package", packageName, null)
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, intent)
                    startActivity(settingsIntent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fabRequestPermission.visibility = View.GONE
                    loadNotes()
                    showToast("Storage permission granted")
                } else {
                    showToast("Storage permission denied")
                    fabRequestPermission.visibility = View.VISIBLE
                }
            }
            REQUEST_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("Microphone ready")
                    startSpeechRecognition()
                } else {
                    showToast("Microphone permission denied")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                pendingEditText?.setText(results[0])
                pendingEditText = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasStoragePermission()) {
            fabRequestPermission.visibility = View.GONE
            loadNotes()
        } else {
            fabRequestPermission.visibility = View.VISIBLE
        }
    }

    private fun loadNotes() {
        try {
            val allNotes = StorageHelper.loadNotes(this)
            val order = StorageHelper.loadOrder(this)

            notes.clear()
            if (order.isNotEmpty()) {
                val sortedNotes = order.mapNotNull { id ->
                    allNotes.find { it.id == id }
                }
                notes.addAll(sortedNotes)
                val missingNotes = allNotes.filter { note ->
                    !order.contains(note.id)
                }
                notes.addAll(missingNotes)
            } else {
                notes.addAll(allNotes)
            }

            adapter.updateNotes(notes)
            checkEmptyState()
        } catch (e: Exception) {
            showToast("Error loading notes: ${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkEmptyState() {
        if (notes.isEmpty()) {
            emptyTextView.text = "No notes in notebook"
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showAddNoteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val editTitle = dialogView.findViewById<TextInputEditText>(R.id.editTitle)
        val editContent = dialogView.findViewById<TextInputEditText>(R.id.editContent)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)

        val titleLayout = editTitle.parent.parent as TextInputLayout
        val contentLayout = editContent.parent.parent as TextInputLayout

        titleLayout.setEndIconOnClickListener {
            pendingEditText = editTitle
            checkAudioPermissionAndStartSpeech()
        }

        contentLayout.setEndIconOnClickListener {
            pendingEditText = editContent
            checkAudioPermissionAndStartSpeech()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSave.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val content = editContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                showToast("Please fill all fields")
                return@setOnClickListener
            }

            try {
                val note = Note(
                    title = title,
                    content = content,
                    imagePaths = emptyList(),
                    filePaths = emptyList()
                )
                StorageHelper.addNote(this, note)
                loadNotes()
                dialog.dismiss()
                showToast("Note added successfully")
            } catch (e: Exception) {
                showToast("Error adding note: ${e.message}")
            }
        }

        dialog.show()
    }

    private fun checkAudioPermissionAndStartSpeech() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_AUDIO_PERMISSION)
            return
        }
        startSpeechRecognition()
    }

    private fun startSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            showToast("Speech recognition not available")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            startActivityForResult(intent, REQUEST_SPEECH)
        } catch (e: Exception) {
            showToast("Error: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}