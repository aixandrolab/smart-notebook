// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.content.Context
import android.os.Environment
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object StorageHelper {
    private const val DIR_NAME = "smart-notebook"
    private const val FILE_NAME = "notes.json"
    private const val ORDER_FILE_NAME = "order.json"
    private const val IMAGES_DIR = "images"
    private const val FILES_DIR = "files"

    private val gson = GsonBuilder()
        .setLenient()
        .disableHtmlEscaping()
        .create()

    fun getAppDir(context: Context): File? {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val appDir = File(documentsDir, DIR_NAME)

        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        return appDir
    }

    fun getNotesFile(context: Context): File? {
        val appDir = getAppDir(context) ?: return null
        return File(appDir, FILE_NAME)
    }

    fun getOrderFile(context: Context): File? {
        val appDir = getAppDir(context) ?: return null
        return File(appDir, ORDER_FILE_NAME)
    }

    fun getImagesDir(context: Context): File? {
        val appDir = getAppDir(context) ?: return null
        val imagesDir = File(appDir, IMAGES_DIR)

        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        return imagesDir
    }

    fun getFilesDir(context: Context): File? {
        val appDir = getAppDir(context) ?: return null
        val filesDir = File(appDir, FILES_DIR)

        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }

        return filesDir
    }

    fun loadNotes(context: Context): List<Note> {
        val file = getNotesFile(context) ?: return emptyList()

        return try {
            if (!file.exists()) {
                saveNotes(context, emptyList())
                return emptyList()
            }

            val json = FileReader(file).use { it.readText() }
            if (json.isBlank() || json == "[]") {
                return emptyList()
            }

            val notes = parseNotesFromJson(json)
            notes ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                saveNotes(context, emptyList())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            emptyList()
        }
    }

    private fun parseNotesFromJson(json: String): List<Note>? {
        return try {
            val jsonArray = gson.fromJson(json, JsonArray::class.java)
            val notes = mutableListOf<Note>()

            for (element in jsonArray) {
                val obj = element.asJsonObject
                val id = obj.get("id")?.asString ?: java.util.UUID.randomUUID().toString()
                val title = obj.get("title")?.asString ?: ""
                val content = obj.get("content")?.asString ?: ""
                val timestamp = obj.get("timestamp")?.asLong ?: System.currentTimeMillis()
                val reminderTime = obj.get("reminderTime")?.asLong

                val imagePaths = mutableListOf<String>()
                val imagesArray = obj.get("imagePaths")?.asJsonArray
                if (imagesArray != null) {
                    for (imgElement in imagesArray) {
                        imgElement.asString?.let { imagePaths.add(it) }
                    }
                }

                val filePaths = mutableListOf<String>()
                val filesArray = obj.get("filePaths")?.asJsonArray
                if (filesArray != null) {
                    for (fileElement in filesArray) {
                        fileElement.asString?.let { filePaths.add(it) }
                    }
                }

                notes.add(Note(
                    id = id,
                    title = title,
                    content = content,
                    timestamp = timestamp,
                    imagePaths = imagePaths,
                    filePaths = filePaths,
                    reminderTime = reminderTime
                ))
            }

            notes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveNotes(context: Context, notes: List<Note>) {
        val file = getNotesFile(context) ?: return

        try {
            val jsonArray = JsonArray()

            for (note in notes) {
                val obj = JsonObject()
                obj.addProperty("id", note.id)
                obj.addProperty("title", note.title)
                obj.addProperty("content", note.content)
                obj.addProperty("timestamp", note.timestamp)
                note.reminderTime?.let { obj.addProperty("reminderTime", it) }

                val imagesArray = JsonArray()
                for (path in note.imagePaths) {
                    imagesArray.add(path)
                }
                obj.add("imagePaths", imagesArray)

                val filesArray = JsonArray()
                for (path in note.filePaths) {
                    filesArray.add(path)
                }
                obj.add("filePaths", filesArray)

                jsonArray.add(obj)
            }

            val json = gson.toJson(jsonArray)
            FileWriter(file).use { it.write(json) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addNote(context: Context, note: Note) {
        val notes = loadNotes(context).toMutableList()
        notes.add(0, note)
        saveNotes(context, notes)

        val order = loadOrder(context).toMutableList()
        order.add(0, note.id)
        saveOrder(context, order)
    }

    fun updateNote(context: Context, updatedNote: Note) {
        val notes = loadNotes(context).toMutableList()
        val index = notes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            notes[index] = updatedNote
            saveNotes(context, notes)
        }
    }

    fun deleteNote(context: Context, noteId: String) {
        val notes = loadNotes(context).toMutableList()
        notes.removeAll { it.id == noteId }
        saveNotes(context, notes)

        val order = loadOrder(context).toMutableList()
        order.removeAll { it == noteId }
        saveOrder(context, order)
    }

    fun loadOrder(context: Context): List<String> {
        val file = getOrderFile(context) ?: return emptyList()

        return try {
            if (!file.exists()) {
                val notes = loadNotes(context)
                val order = notes.map { it.id }
                saveOrder(context, order)
                return order
            }

            val json = FileReader(file).use { it.readText() }
            if (json.isBlank() || json == "[]") {
                return emptyList()
            }

            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveOrder(context: Context, order: List<String>) {
        val file = getOrderFile(context) ?: return

        try {
            val json = gson.toJson(order)
            FileWriter(file).use { it.write(json) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reorderNotes(context: Context, fromPosition: Int, toPosition: Int) {
        val order = loadOrder(context).toMutableList()
        if (fromPosition < 0 || fromPosition >= order.size || toPosition < 0 || toPosition >= order.size) {
            return
        }

        val item = order.removeAt(fromPosition)
        order.add(toPosition, item)
        saveOrder(context, order)
    }

    fun clearData(context: Context) {
        val file = getNotesFile(context)
        file?.delete()
        saveNotes(context, emptyList())

        val orderFile = getOrderFile(context)
        orderFile?.delete()
        saveOrder(context, emptyList())
    }
}