// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AboutActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val backButton = findViewById<ImageView>(R.id.backButton)
        val logoImage = findViewById<ImageView>(R.id.logoImage)
        val versionText = findViewById<TextView>(R.id.versionText)

        logoImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground))

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        versionText.text = "Version ${packageInfo.versionName}"

        backButton.setOnClickListener {
            finish()
        }
    }
}