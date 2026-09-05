// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.UUID

data class Note(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("imagePaths")
    val imagePaths: List<String> = emptyList(),
    @SerializedName("filePaths")
    val filePaths: List<String> = emptyList(),
    @SerializedName("links")
    val links: List<Link> = emptyList(),
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("reminderTime")
    val reminderTime: Long = 0
) : Serializable

data class Link(
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String
) : Serializable