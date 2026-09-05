// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.ext.tables.TablePlugin

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var titleEditText: TextInputEditText
    private lateinit var editText: EditText
    private lateinit var previewText: TextView
    private lateinit var previewScrollView: ScrollView
    private lateinit var btnTogglePreview: Button
    private lateinit var btnToggleWrap: Button

    private lateinit var btnH1: Button
    private lateinit var btnH2: Button
    private lateinit var btnH3: Button
    private lateinit var btnBold: Button
    private lateinit var btnItalic: Button
    private lateinit var btnUnderline: Button
    private lateinit var btnStrikethrough: Button
    private lateinit var btnBulletList: Button
    private lateinit var btnNumberedList: Button
    private lateinit var btnCheckboxList: Button
    private lateinit var btnQuote: Button
    private lateinit var btnCode: Button
    private lateinit var btnCodeBlock: Button
    private lateinit var btnLink: Button
    private lateinit var btnImage: Button
    private lateinit var btnTable: Button
    private lateinit var btnHorizontalRule: Button
    private lateinit var btnSave: Button

    private var noteId: String? = null
    private var isEditMode = false
    private var markwon: Markwon? = null
    private var isWrapEnabled = true

    companion object {
        const val EXTRA_NOTE_ID = "note_id"
        const val EXTRA_NOTE_TITLE = "note_title"
        const val EXTRA_NOTE_CONTENT = "note_content"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initMarkwon()
        loadNoteData()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        titleEditText = findViewById(R.id.titleEditText)
        editText = findViewById(R.id.editText)
        previewText = findViewById(R.id.previewText)
        previewScrollView = findViewById(R.id.previewScrollView)
        btnTogglePreview = findViewById(R.id.btnTogglePreview)
        btnToggleWrap = findViewById(R.id.btnToggleWrap)

        btnH1 = findViewById(R.id.btnH1)
        btnH2 = findViewById(R.id.btnH2)
        btnH3 = findViewById(R.id.btnH3)
        btnBold = findViewById(R.id.btnBold)
        btnItalic = findViewById(R.id.btnItalic)
        btnUnderline = findViewById(R.id.btnUnderline)
        btnStrikethrough = findViewById(R.id.btnStrikethrough)
        btnBulletList = findViewById(R.id.btnBulletList)
        btnNumberedList = findViewById(R.id.btnNumberedList)
        btnCheckboxList = findViewById(R.id.btnCheckboxList)
        btnQuote = findViewById(R.id.btnQuote)
        btnCode = findViewById(R.id.btnCode)
        btnCodeBlock = findViewById(R.id.btnCodeBlock)
        btnLink = findViewById(R.id.btnLink)
        btnImage = findViewById(R.id.btnImage)
        btnTable = findViewById(R.id.btnTable)
        btnHorizontalRule = findViewById(R.id.btnHorizontalRule)
        btnSave = findViewById(R.id.btnSave)

        btnImage.visibility = View.GONE
    }

    private fun initMarkwon() {
        markwon = Markwon.builder(this)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(this))
            .usePlugin(TablePlugin.create(this))
            .build()

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (previewScrollView.visibility == View.VISIBLE) {
                    renderPreview(s.toString())
                }
            }
        })
    }

    private fun loadNoteData() {
        noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        val title = intent.getStringExtra(EXTRA_NOTE_TITLE) ?: ""
        val content = intent.getStringExtra(EXTRA_NOTE_CONTENT) ?: ""

        if (noteId != null) {
            isEditMode = true
            supportActionBar?.title = "Edit Note"
            titleEditText.setText(title)
            editText.setText(content)
            renderPreview(content)
        } else {
            supportActionBar?.title = "New Note"
            titleEditText.requestFocus()
        }
    }

    private fun renderPreview(markdown: String) {
        markwon?.setMarkdown(previewText, markdown)
    }

    private fun setupListeners() {
        btnTogglePreview.setOnClickListener {
            val isPreviewVisible = previewScrollView.visibility == View.VISIBLE
            if (isPreviewVisible) {
                previewScrollView.visibility = View.GONE
                btnTogglePreview.text = "Show Preview"
            } else {
                previewScrollView.visibility = View.VISIBLE
                btnTogglePreview.text = "Hide Preview"
                renderPreview(editText.text.toString())
            }
        }

        btnToggleWrap.setOnClickListener {
            isWrapEnabled = !isWrapEnabled
            btnToggleWrap.text = if (isWrapEnabled) "Wrap" else "No Wrap"
            editText.setHorizontallyScrolling(!isWrapEnabled)
        }

        btnH1.setOnClickListener { insertHeading(1) }
        btnH2.setOnClickListener { insertHeading(2) }
        btnH3.setOnClickListener { insertHeading(3) }

        btnBold.setOnClickListener { wrapSelection("**", "**") }
        btnItalic.setOnClickListener { wrapSelection("*", "*") }
        btnUnderline.setOnClickListener { wrapSelection("__", "__") }
        btnStrikethrough.setOnClickListener { wrapSelection("~~", "~~") }

        btnBulletList.setOnClickListener { insertBulletList() }
        btnNumberedList.setOnClickListener { insertNumberedList() }
        btnCheckboxList.setOnClickListener { insertCheckboxList() }

        btnQuote.setOnClickListener { insertQuote() }
        btnCode.setOnClickListener { wrapSelection("`", "`") }
        btnCodeBlock.setOnClickListener { wrapSelection("```\n", "\n```") }

        btnLink.setOnClickListener { showAddLinkDialog() }
        btnImage.setOnClickListener {
            Toast.makeText(this, "Image support disabled", Toast.LENGTH_SHORT).show()
        }

        btnTable.setOnClickListener { insertTable() }
        btnHorizontalRule.setOnClickListener { insertHorizontalRule() }

        btnSave.setOnClickListener { saveNote() }
    }

    private fun insertHeading(level: Int) {
        val text = editText.text
        val start = editText.selectionStart
        val lineStart = findLineStart(text, start)
        val lineEnd = findLineEnd(text, start)
        val lineText = text.substring(lineStart, lineEnd)

        val hashes = "#".repeat(level)
        val newLine = if (lineText.startsWith("#")) {
            val cleanText = lineText.replace(Regex("^#+\\s*"), "")
            "$hashes $cleanText"
        } else {
            "$hashes $lineText"
        }

        val newText = text.replace(lineStart, lineEnd, newLine)
        editText.setText(newText)
        editText.setSelection(lineStart + newLine.length)
    }

    private fun insertBulletList() {
        insertList("- ")
    }

    private fun insertNumberedList() {
        val text = editText.text
        val start = editText.selectionStart
        val lineStart = findLineStart(text, start)
        val lineEnd = findLineEnd(text, start)
        val lineText = text.substring(lineStart, lineEnd)

        val previousLines = text.substring(0, lineStart).split("\n")
        val numberedItems = previousLines.filter { it.matches(Regex("^\\d+\\. .*")) }
        val nextNumber = numberedItems.size + 1

        val newLine = if (lineText.matches(Regex("^\\d+\\. .*"))) {
            lineText.replace(Regex("^\\d+\\. "), "")
        } else {
            "$nextNumber. $lineText"
        }

        val newText = text.replace(lineStart, lineEnd, newLine)
        editText.setText(newText)
        editText.setSelection(lineStart + newLine.length)
    }

    private fun insertCheckboxList() {
        insertList("- [ ] ")
    }

    private fun insertList(prefix: String) {
        val text = editText.text
        val start = editText.selectionStart
        val lineStart = findLineStart(text, start)
        val lineEnd = findLineEnd(text, start)
        val lineText = text.substring(lineStart, lineEnd)

        val newLine = if (lineText.startsWith("- ") || lineText.startsWith("- [ ] ")) {
            lineText.drop(2)
        } else {
            "$prefix$lineText"
        }

        val newText = text.replace(lineStart, lineEnd, newLine)
        editText.setText(newText)
        editText.setSelection(lineStart + newLine.length)
    }

    private fun insertQuote() {
        val text = editText.text
        val start = editText.selectionStart
        val lineStart = findLineStart(text, start)
        val lineEnd = findLineEnd(text, start)
        val lineText = text.substring(lineStart, lineEnd)

        val newLine = if (lineText.startsWith("> ")) {
            lineText.drop(2)
        } else {
            "> $lineText"
        }

        val newText = text.replace(lineStart, lineEnd, newLine)
        editText.setText(newText)
        editText.setSelection(lineStart + newLine.length)
    }

    private fun insertTable() {
        val cursorPos = editText.selectionStart
        val table = """
            
            | Header 1 | Header 2 | Header 3 |
            |----------|----------|----------|
            | Cell 1   | Cell 2   | Cell 3   |
            | Cell 4   | Cell 5   | Cell 6   |
            
        """.trimIndent()

        val newText = editText.text.insert(cursorPos, table)
        editText.setText(newText)
        editText.setSelection(cursorPos + table.length)
    }

    private fun insertHorizontalRule() {
        val cursorPos = editText.selectionStart
        val hr = "\n---\n"
        val newText = editText.text.insert(cursorPos, hr)
        editText.setText(newText)
        editText.setSelection(cursorPos + hr.length)
    }

    private fun wrapSelection(prefix: String, suffix: String) {
        val text = editText.text
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start == end) {
            val newText = text.insert(start, "$prefix$suffix")
            editText.setText(newText)
            editText.setSelection(start + prefix.length)
        } else {
            val selectedText = text.substring(start, end)
            val newText = text.replace(start, end, "$prefix$selectedText$suffix")
            editText.setText(newText)
            editText.setSelection(start + prefix.length, end + prefix.length)
        }
    }

    private fun findLineStart(text: Editable, position: Int): Int {
        var i = position
        while (i > 0 && text[i - 1] != '\n') i--
        return i
    }

    private fun findLineEnd(text: Editable, position: Int): Int {
        var i = position
        while (i < text.length && text[i] != '\n') i++
        return i
    }

    private fun showAddLinkDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_link, null)
        val editTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editLinkTitle)
        val editUrl = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editLinkUrl)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddLink)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnAdd.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val url = editUrl.text.toString().trim()

            if (title.isEmpty() || url.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                Toast.makeText(this, "Please enter a valid URL (http:// or https://)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val linkText = "[$title]($url)"
            val cursorPos = editText.selectionStart
            val newText = editText.text.insert(cursorPos, linkText)
            editText.setText(newText)
            editText.setSelection(cursorPos + linkText.length)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveNote() {
        val title = titleEditText.text.toString().trim()
        val content = editText.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (isEditMode && noteId != null) {
                val existingNote = StorageHelper.loadNotes(this).find { it.id == noteId }
                existingNote?.let {
                    val updatedNote = it.copy(
                        title = title,
                        content = content
                    )
                    StorageHelper.updateNote(this, updatedNote)
                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newNote = Note(
                    title = title,
                    content = content,
                    imagePaths = emptyList(),
                    filePaths = emptyList(),
                    links = emptyList()
                )
                StorageHelper.addNote(this, newNote)
                Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show()
            }
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving note: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_clear -> {
                editText.text.clear()
                renderPreview("")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}