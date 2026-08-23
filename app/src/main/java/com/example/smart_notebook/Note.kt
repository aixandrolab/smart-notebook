// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

data class Note(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePaths: List<String> = emptyList(),
    val filePaths: List<String> = emptyList(),
    val reminderTime: Long? = null
)