// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.ext.tables.TablePlugin
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

class NoteDetailActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var dateText: TextView
    private lateinit var contentText: TextView
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var imageSectionTitle: TextView
    private lateinit var fileRecyclerView: RecyclerView
    private lateinit var fileSectionTitle: TextView
    private lateinit var linkRecyclerView: RecyclerView
    private lateinit var linkSectionTitle: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnAttachment: Button
    private lateinit var btnReminder: Button
    private lateinit var btnDelete: Button
    private lateinit var permissionWarning: TextView

    private var noteId: String = ""
    private var currentNote: Note? = null
    private var imageAdapter: ImageAdapter? = null
    private var fileAdapter: FileAdapter? = null
    private var linkAdapter: LinkAdapter? = null
    private var isInitialized = false
    private var currentPhotoPath: String? = null
    private var pendingEditText: TextInputEditText? = null
    private lateinit var markwon: Markwon

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_GALLERY = 2
        private const val REQUEST_FILE_PICKER = 3
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_STORAGE_PERMISSION = 101
        private const val REQUEST_NOTIFICATION_PERMISSION = 102
        private const val REQUEST_AUDIO_PERMISSION = 103
        private const val REQUEST_SPEECH = 1000
        private const val CHANNEL_ID = "smart_notebook_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_note_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initMarkwon()
        noteId = intent.getStringExtra("note_id") ?: ""
        initViews()
        checkAllPermissions()
    }

    private fun initMarkwon() {
        markwon = Markwon.builder(this)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(this))
            .usePlugin(TablePlugin.create(this))
            .build()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)
        dateText = findViewById(R.id.dateText)
        contentText = findViewById(R.id.contentText)
        imageRecyclerView = findViewById(R.id.imageRecyclerView)
        imageSectionTitle = findViewById(R.id.imageSectionTitle)
        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        fileSectionTitle = findViewById(R.id.fileSectionTitle)
        linkRecyclerView = findViewById(R.id.linkRecyclerView)
        linkSectionTitle = findViewById(R.id.linkSectionTitle)
        btnEdit = findViewById(R.id.btnEdit)
        btnAttachment = findViewById(R.id.btnAttachment)
        btnReminder = findViewById(R.id.btnReminder)
        btnDelete = findViewById(R.id.btnDelete)
        permissionWarning = findViewById(R.id.permissionWarning)

        backButton.setOnClickListener {
            finish()
        }

        btnEdit.setOnClickListener {
            if (hasStoragePermission()) {
                openNoteEditor()
            } else {
                showToast("Storage permission required")
                checkStoragePermission()
            }
        }

        btnAttachment.setOnClickListener {
            if (hasStoragePermission()) {
                showAttachmentOptions()
            } else {
                showToast("Storage permission required")
                checkStoragePermission()
            }
        }

        btnReminder.setOnClickListener {
            if (hasNotificationPermission()) {
                showDateTimePicker()
            } else {
                showToast("Notification permission required")
                checkNotificationPermission()
            }
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }

        imageRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        linkRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun openNoteEditor() {
        currentNote?.let { note ->
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, note.id)
            intent.putExtra(NoteEditorActivity.EXTRA_NOTE_TITLE, note.title)
            intent.putExtra(NoteEditorActivity.EXTRA_NOTE_CONTENT, note.content)
            startActivity(intent)
        } ?: run {
            showToast("Note not found")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkAllPermissions() {
        val hasStorage = hasStoragePermission()
        val hasCamera = hasCameraPermission()
        val hasNotification = hasNotificationPermission()

        if (!hasStorage || !hasCamera || !hasNotification) {
            permissionWarning.visibility = View.VISIBLE
            btnEdit.isEnabled = false
            btnAttachment.isEnabled = false
            btnReminder.isEnabled = false
            btnDelete.isEnabled = false

            val missingPermissions = mutableListOf<String>()
            if (!hasStorage) missingPermissions.add("Storage")
            if (!hasCamera) missingPermissions.add("Camera")
            if (!hasNotification) missingPermissions.add("Notifications")

            permissionWarning.text = "Permissions needed: ${missingPermissions.joinToString(", ")}\nTap to grant"

            permissionWarning.setOnClickListener {
                requestAllPermissions()
            }
        } else {
            permissionWarning.visibility = View.GONE
            btnEdit.isEnabled = true
            btnAttachment.isEnabled = true
            btnReminder.isEnabled = true
            btnDelete.isEnabled = true
            loadNoteDetails()
        }
    }

    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (!hasStoragePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Uri.fromParts("package", packageName, null)
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, intent)
                    startActivityForResult(settingsIntent, REQUEST_STORAGE_PERMISSION)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent, REQUEST_STORAGE_PERMISSION)
                }
                return
            } else {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (!hasCameraPermission()) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (!hasNotificationPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            checkAllPermissions()
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun checkStoragePermission() {
        if (!hasStoragePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Uri.fromParts("package", packageName, null)
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, intent)
                    startActivityForResult(settingsIntent, REQUEST_STORAGE_PERMISSION)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent, REQUEST_STORAGE_PERMISSION)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_STORAGE_PERMISSION
                )
            }
        }
    }

    private fun checkCameraPermission() {
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun checkNotificationPermission() {
        if (!hasNotificationPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showAttachmentOptions()
                } else {
                    showToast("Camera permission denied")
                    checkAllPermissions()
                }
            }
            REQUEST_STORAGE_PERMISSION, REQUEST_NOTIFICATION_PERMISSION -> {
                checkAllPermissions()
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
            return
        }

        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                checkAllPermissions()
            }
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    handleCameraResult()
                } else {
                    showToast("Camera cancelled")
                }
            }
            REQUEST_GALLERY -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { uri ->
                        handleGalleryResult(uri)
                    }
                }
            }
            REQUEST_FILE_PICKER -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { uri ->
                        handleFilePickerResult(uri)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasStoragePermission()) {
            loadNoteDetails()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadNoteDetails() {
        try {
            val notes = StorageHelper.loadNotes(this)
            currentNote = notes.find { it.id == noteId }

            currentNote?.let { note ->
                titleText.text = note.title
                dateText.text = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    .format(Date(note.timestamp))

                markwon.setMarkdown(contentText, note.content)

                if (note.imagePaths.isNotEmpty()) {
                    imageSectionTitle.visibility = View.VISIBLE
                    imageRecyclerView.visibility = View.VISIBLE
                    imageAdapter = ImageAdapter(
                        note.imagePaths,
                        onImageClick = { imagePath ->
                            showImageFullScreen(imagePath)
                        },
                        onImageLongClick = { imagePath ->
                            showImageDeleteDialog(imagePath)
                        }
                    )
                    imageRecyclerView.adapter = imageAdapter
                } else {
                    imageSectionTitle.visibility = View.GONE
                    imageRecyclerView.visibility = View.GONE
                }

                if (note.filePaths.isNotEmpty()) {
                    fileSectionTitle.visibility = View.VISIBLE
                    fileRecyclerView.visibility = View.VISIBLE
                    fileAdapter = FileAdapter(
                        note.filePaths,
                        onFileClick = { filePath ->
                            openFile(filePath)
                        },
                        onFileLongClick = { filePath ->
                            showFileDeleteDialog(filePath)
                        }
                    )
                    fileRecyclerView.adapter = fileAdapter
                } else {
                    fileSectionTitle.visibility = View.GONE
                    fileRecyclerView.visibility = View.GONE
                }

                if (note.links.isNotEmpty()) {
                    linkSectionTitle.visibility = View.VISIBLE
                    linkRecyclerView.visibility = View.VISIBLE
                    linkAdapter = LinkAdapter(
                        note.links,
                        onLinkClick = { link ->
                            openLink(link.url)
                        },
                        onLinkLongClick = { link ->
                            showLinkDeleteDialog(link)
                        }
                    )
                    linkRecyclerView.adapter = linkAdapter
                } else {
                    linkSectionTitle.visibility = View.GONE
                    linkRecyclerView.visibility = View.GONE
                }

                isInitialized = true
            } ?: run {
                showToast("Note not found")
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error loading note: ${e.message}")
            finish()
        }
    }

    private fun openLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            showToast("Cannot open link: ${e.message}")
        }
    }

    private fun showLinkDeleteDialog(link: Link) {
        AlertDialog.Builder(this)
            .setTitle("Delete Link")
            .setMessage("Are you sure you want to delete \"${link.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                currentNote?.let { note ->
                    val updatedLinks = note.links.filter { it != link }
                    val updatedNote = note.copy(links = updatedLinks)
                    StorageHelper.updateNote(this, updatedNote)
                    loadNoteDetails()
                    showToast("Link deleted")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showImageFullScreen(imagePath: String) {
        try {
            val file = File(imagePath)
            if (!file.exists()) {
                showToast("Image not found")
                return
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_full_image, null)
            val imageView = dialogView.findViewById<ImageView>(R.id.fullImageView)
            val closeButton = dialogView.findViewById<Button>(R.id.btnCloseImage)

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(imagePath, this)

                val scale = (outWidth / 800).coerceAtLeast(outHeight / 800).coerceAtLeast(1)

                inSampleSize = scale
                inJustDecodeBounds = false
            }

            val bitmap = BitmapFactory.decodeFile(imagePath, options)
            imageView.setImageBitmap(bitmap)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            closeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error showing image: ${e.message}")
        }
    }

    private fun showImageDeleteDialog(imagePath: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Delete") { _, _ ->
                currentNote?.let { note ->
                    val newImagePaths = note.imagePaths.filter { it != imagePath }
                    val updatedNote = note.copy(imagePaths = newImagePaths)
                    StorageHelper.updateNote(this, updatedNote)
                    loadNoteDetails()
                    showToast("Image deleted")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showFileDeleteDialog(filePath: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete this file?")
            .setPositiveButton("Delete") { _, _ ->
                currentNote?.let { note ->
                    val newFilePaths = note.filePaths.filter { it != filePath }
                    val updatedNote = note.copy(filePaths = newFilePaths)
                    StorageHelper.updateNote(this, updatedNote)
                    loadNoteDetails()
                    showToast("File deleted")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showAttachmentOptions() {
        val options = arrayOf("Take Photo", "Choose Image", "Choose File", "Add Link")

        AlertDialog.Builder(this)
            .setTitle("Add Attachment")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (hasCameraPermission()) {
                            openCamera()
                        } else {
                            checkCameraPermission()
                        }
                    }
                    1 -> {
                        if (hasStoragePermission()) {
                            openGallery()
                        } else {
                            showToast("Storage permission required")
                            checkStoragePermission()
                        }
                    }
                    2 -> {
                        if (hasStoragePermission()) {
                            openFilePicker()
                        } else {
                            showToast("Storage permission required")
                            checkStoragePermission()
                        }
                    }
                    3 -> {
                        showAddLinkDialog()
                    }
                }
            }
            .create()
            .show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        try {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile = createImageFile()
                photoFile?.let {
                    currentPhotoPath = it.absolutePath
                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                } ?: run {
                    showToast("Error creating image file")
                }
            } else {
                showToast("Camera not available")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error opening camera: ${e.message}")
        }
    }

    private fun createImageFile(): File? {
        return try {
            val imagesDir = StorageHelper.getImagesDir(this)
            if (imagesDir == null) {
                showToast("Cannot access storage")
                return null
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_${timeStamp}.jpg"
            val file = File(imagesDir, fileName)
            file.createNewFile()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_GALLERY)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error opening gallery: ${e.message}")
        }
    }

    private fun openFilePicker() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(intent, REQUEST_FILE_PICKER)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error opening file picker: ${e.message}")
        }
    }

    private fun showAddLinkDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_link, null)
        val editTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editLinkTitle)
        val editUrl = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editLinkUrl)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddLink)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnAdd.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val url = editUrl.text.toString().trim()

            if (title.isEmpty()) {
                showToast("Please enter a title")
                return@setOnClickListener
            }

            if (url.isEmpty()) {
                showToast("Please enter a URL")
                return@setOnClickListener
            }

            if (!isValidUrl(url)) {
                showToast("Please enter a valid URL (http:// or https://)")
                return@setOnClickListener
            }

            currentNote?.let { note ->
                val newLink = Link(title, url)
                val updatedLinks = note.links.toMutableList()
                updatedLinks.add(newLink)
                val updatedNote = note.copy(links = updatedLinks)
                StorageHelper.updateNote(this, updatedNote)
                loadNoteDetails()
                dialog.dismiss()
                showToast("Link added: $title")
            }
        }

        dialog.show()
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun handleCameraResult() {
        try {
            currentPhotoPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    currentNote?.let { note ->
                        val existingPaths = note.imagePaths.toMutableList()
                        if (!existingPaths.contains(path)) {
                            existingPaths.add(path)
                            val updatedNote = note.copy(imagePaths = existingPaths)
                            StorageHelper.updateNote(this, updatedNote)
                            loadNoteDetails()
                            showToast("Image added")
                        } else {
                            showToast("Image already exists")
                        }
                    }
                } else {
                    showToast("Image file not found")
                }
            } ?: run {
                showToast("No image captured")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error adding image: ${e.message}")
        } finally {
            currentPhotoPath = null
        }
    }

    private fun handleGalleryResult(uri: Uri) {
        try {
            val extension = getFileExtensionFromUri(uri)

            if (isImageFile(extension)) {
                val existingImagePath = findExistingImage(uri)
                if (existingImagePath != null) {
                    currentNote?.let { note ->
                        if (!note.imagePaths.contains(existingImagePath)) {
                            val newImagePaths = note.imagePaths.toMutableList()
                            newImagePaths.add(existingImagePath)
                            val updatedNote = note.copy(imagePaths = newImagePaths)
                            StorageHelper.updateNote(this, updatedNote)
                            loadNoteDetails()
                            showToast("Image added")
                        } else {
                            showToast("Image already exists")
                        }
                    }
                    return
                }

                val file = saveImageFromUri(uri)
                file?.let {
                    currentNote?.let { note ->
                        val newImagePaths = note.imagePaths.toMutableList()
                        newImagePaths.add(it.absolutePath)
                        val updatedNote = note.copy(imagePaths = newImagePaths)
                        StorageHelper.updateNote(this, updatedNote)
                        loadNoteDetails()
                        showToast("Image added")
                    }
                }
            } else {
                val existingFilePath = findExistingFile(uri)
                if (existingFilePath != null) {
                    currentNote?.let { note ->
                        if (!note.filePaths.contains(existingFilePath)) {
                            val newFilePaths = note.filePaths.toMutableList()
                            newFilePaths.add(existingFilePath)
                            val updatedNote = note.copy(filePaths = newFilePaths)
                            StorageHelper.updateNote(this, updatedNote)
                            loadNoteDetails()
                            showToast("File added")
                        } else {
                            showToast("File already exists")
                        }
                    }
                    return
                }

                val file = saveFileFromUri(uri)
                file?.let {
                    currentNote?.let { note ->
                        val newFilePaths = note.filePaths.toMutableList()
                        newFilePaths.add(it.absolutePath)
                        val updatedNote = note.copy(filePaths = newFilePaths)
                        StorageHelper.updateNote(this, updatedNote)
                        loadNoteDetails()
                        showToast("File added: ${it.name}")
                    }
                }
            }
        } catch (e: Exception) {
            showToast("Error adding file: ${e.message}")
        }
    }

    private fun handleFilePickerResult(uri: Uri) {
        try {
            val extension = getFileExtensionFromUri(uri)

            if (isImageFile(extension)) {
                val existingImagePath = findExistingImage(uri)
                if (existingImagePath != null) {
                    currentNote?.let { note ->
                        if (!note.imagePaths.contains(existingImagePath)) {
                            val newImagePaths = note.imagePaths.toMutableList()
                            newImagePaths.add(existingImagePath)
                            val updatedNote = note.copy(imagePaths = newImagePaths)
                            StorageHelper.updateNote(this, updatedNote)
                            loadNoteDetails()
                            showToast("Image added")
                        } else {
                            showToast("Image already exists")
                        }
                    }
                    return
                }

                val file = saveImageFromUri(uri)
                file?.let {
                    currentNote?.let { note ->
                        val newImagePaths = note.imagePaths.toMutableList()
                        newImagePaths.add(it.absolutePath)
                        val updatedNote = note.copy(imagePaths = newImagePaths)
                        StorageHelper.updateNote(this, updatedNote)
                        loadNoteDetails()
                        showToast("Image added")
                    }
                }
            } else {
                val existingFilePath = findExistingFile(uri)
                if (existingFilePath != null) {
                    currentNote?.let { note ->
                        if (!note.filePaths.contains(existingFilePath)) {
                            val newFilePaths = note.filePaths.toMutableList()
                            newFilePaths.add(existingFilePath)
                            val updatedNote = note.copy(filePaths = newFilePaths)
                            StorageHelper.updateNote(this, updatedNote)
                            loadNoteDetails()
                            showToast("File added")
                        } else {
                            showToast("File already exists")
                        }
                    }
                    return
                }

                val file = saveFileFromUri(uri)
                file?.let {
                    currentNote?.let { note ->
                        val newFilePaths = note.filePaths.toMutableList()
                        newFilePaths.add(it.absolutePath)
                        val updatedNote = note.copy(filePaths = newFilePaths)
                        StorageHelper.updateNote(this, updatedNote)
                        loadNoteDetails()
                        showToast("File added: ${it.name}")
                    }
                }
            }
        } catch (e: Exception) {
            showToast("Error adding file: ${e.message}")
        }
    }

    private fun getFileExtensionFromUri(uri: Uri): String {
        try {
            val fileName = getFileNameFromUri(uri)
            if (fileName != null && fileName.contains(".")) {
                return fileName.substringAfterLast(".").lowercase()
            }

            val mimeType = contentResolver.getType(uri)
            if (mimeType != null) {
                return when {
                    mimeType.startsWith("image/") -> "jpg"
                    mimeType.startsWith("video/") -> "mp4"
                    mimeType.startsWith("audio/") -> "mp3"
                    mimeType == "application/pdf" -> "pdf"
                    mimeType == "application/zip" -> "zip"
                    else -> ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun isImageFile(extension: String): Boolean {
        return when (extension.lowercase()) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif" -> true
            else -> false
        }
    }

    private fun findExistingImage(uri: Uri): String? {
        try {
            val fileName = getFileNameFromUri(uri)
            if (fileName != null) {
                val imagesDir = StorageHelper.getImagesDir(this)
                imagesDir?.let { dir ->
                    val existingFile = File(dir, fileName)
                    if (existingFile.exists()) {
                        return existingFile.absolutePath
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun findExistingFile(uri: Uri): String? {
        try {
            val fileName = getFileNameFromUri(uri)
            if (fileName != null) {
                val filesDir = StorageHelper.getFilesDir(this)
                filesDir?.let { dir ->
                    val existingFile = File(dir, fileName)
                    if (existingFile.exists()) {
                        return existingFile.absolutePath
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            cursor = contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                cursor.getString(columnIndex)
            } else {
                null
            }
        } finally {
            cursor?.close()
        }
    }

    private fun saveImageFromUri(uri: Uri): File? {
        return try {
            val imagesDir = StorageHelper.getImagesDir(this) ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val originalName = getFileNameFromUri(uri)
            val fileName = if (originalName != null) {
                val existingFile = File(imagesDir, originalName)
                if (existingFile.exists()) {
                    val nameWithoutExt = originalName.substringBeforeLast(".")
                    val ext = originalName.substringAfterLast(".")
                    "${nameWithoutExt}_${timeStamp}.$ext"
                } else {
                    originalName
                }
            } else {
                "IMG_${timeStamp}.jpg"
            }

            val file = File(imagesDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveFileFromUri(uri: Uri): File? {
        return try {
            val filesDir = StorageHelper.getFilesDir(this) ?: return null

            var originalName = getFileNameFromUri(uri)
            if (originalName == null) {
                originalName = "file_${System.currentTimeMillis()}"
            }

            val existingFile = File(filesDir, originalName)
            if (existingFile.exists()) {
                val nameWithoutExt = originalName.substringBeforeLast(".")
                val ext = originalName.substringAfterLast(".")
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                originalName = "${nameWithoutExt}_${timeStamp}.$ext"
            }

            val file = File(filesDir, originalName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun openFile(filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                showToast("File not found")
                return
            }

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val mimeType = getMimeType(file.extension)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Cannot open file: ${e.message}")
        }
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "txt" -> "text/plain"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "zip" -> "application/zip"
            "rar" -> "application/vnd.rar"
            "7z" -> "application/x-7z-compressed"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "apk" -> "application/vnd.android.package-archive"
            "html", "htm" -> "text/html"
            "csv" -> "text/csv"
            "md" -> "text/markdown"
            else -> "*/*"
        }
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

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                showTimePicker(selectedYear, selectedMonth, selectedDay)
            },
            year, month, day
        )
        datePickerDialog.setTitle("Select Date")
        datePickerDialog.show()
    }

    private fun showTimePicker(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                setReminderWithDateTime(year, month, day, selectedHour, selectedMinute)
            },
            hour, minute, true
        )
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    private fun setReminderWithDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, hour, minute, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val reminderTime = calendar.timeInMillis
            val currentTime = System.currentTimeMillis()

            if (reminderTime <= currentTime) {
                showToast("Please select a future date and time")
                return
            }

            createNotificationChannel()

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val requestCode = (reminderTime % Int.MAX_VALUE).toInt()

            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("note_title", currentNote?.title ?: "Note")
                putExtra("note_content", currentNote?.content ?: "No content")
                putExtra("note_id", currentNote?.id ?: "")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                } else {
                    val intentSettings = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intentSettings)
                    showToast("Please allow exact alarm permission")
                    return
                }
            } else
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)

            val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(reminderTime))

            currentNote?.let { note ->
                val updatedNote = note.copy(reminderTime = reminderTime)
                StorageHelper.updateNote(this, updatedNote)
                loadNoteDetails()
            }

            showToast("Reminder set for $formattedDate")

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error setting reminder: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Smart Notebook Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle reminders for your important notes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 1000)
                lightColor = "#FF9800".toColorInt()
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                StorageHelper.deleteNote(this, noteId)
                finish()
                showToast("Note deleted")
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}