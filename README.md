# 📝 Smart Notebook <sup>v1.0.4</sup>

[![GitHub top language](https://img.shields.io/github/languages/top/aixandrolab/smart-notebook)](https://github.com/aixandrolab/smart-notebook)
[![GitHub license](https://img.shields.io/github/license/aixandrolab/smart-notebook)](https://github.com/aixandrolab/smart-notebook/blob/master/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/aixandrolab/smart-notebook)](https://github.com/aixandrolab/smart-notebook/)
[![GitHub stars](https://img.shields.io/github/stars/aixandrolab/smart-notebook?style=social)](https://github.com/aixandrolab/smart-notebook/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/aixandrolab/smart-notebook?style=social)](https://github.com/aixandrolab/smart-notebook/network/members)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org)

---

A powerful, feature-rich note-taking application for Android built with Kotlin.

---

![Smart Notebook](https://github.com/aixandrolab/smart-notebook/blob/master/data/images/main_screen.jpg)

---

## ⚠️ Disclaimer

**By using this software, you agree to the full disclaimer terms.**

**Summary:** Software provided "AS IS" without warranty. You assume all risks.

**Full legal disclaimer:** See [DISCLAIMER.md](DISCLAIMER.md)

---

## ✨ Features

### 📝 Notes Management
- **Create, edit, and delete notes** with Markdown support
- **Drag & Drop sorting** – long press and drag the handle (≡) to reorder
- **Swipe to Edit** – swipe right on a note to quickly edit it (yellow background with pencil icon)
- **Swipe to Delete** – swipe left on a note to delete it with confirmation (red background with trash icon)

### ✏️ Markdown Editor
- **Rich toolbar** with formatting options:
  - Headers (H1, H2, H3)
  - Text styling (Bold, Italic, Underline, Strikethrough)
  - Lists (Bullet, Numbered, Checkbox/Task list)
  - Code (Inline code, Code block)
  - Quotes and Tables
  - Links
  - Horizontal rules
- **Live preview** – toggle between editor and preview modes
- **Wrap/No wrap** – toggle text wrapping for comfortable editing

### 📎 Attachments
- **Images** – capture from camera or choose from gallery
- **Files** – attach any file type (PDF, DOC, TXT, ZIP, APK, etc.)
- **Links** – add clickable URLs with custom titles
- **Audio Recording** – record voice notes directly in the app
  - High-quality AAC format (.m4a)
  - Real-time timer display during recording
  - Built-in audio player with playback controls
  - Play/Pause and Seek functionality
- **Full-screen preview** – tap images to view in full screen
- **Long press** on any attachment to delete it

### ⏰ Reminders
- Set date and time reminders for any note
- Notifications work even when app is closed
- Notification actions: Open Note and Dismiss
- Precise alarm timing with exact alarm permission

### 💾 Storage
- All data stored locally in `Documents/smart-notebook/`
- JSON format – `notes.json` and `order.json`
- No cloud dependency – complete privacy and offline access
- Images and files organized in separate folders

### 🎨 UI/UX
- Dark theme with orange accents
- Material Design components
- Smooth animations and transitions
- Intuitive swipe gestures
- Clean and minimal design

---

## 🚀 Quick Start

### Creating a Note
Tap **+** → Enter Title and Content → Tap **Save Note**

### Using Markdown Editor
- Use the **toolbar** to quickly format your text
- Tap **"Show Preview"** to see the rendered output
- Tap **"Wrap"** to toggle text wrapping

### Adding Attachments
Open note → Tap **Attach** → Choose from:
- 📸 Take Photo
- 🖼️ Choose Image from Gallery
- 📁 Choose File
- 🔗 Add Link
- 🎙️ Record Audio

### Playing Audio Notes
- Tap on any audio file in the note attachments
- Use the built-in audio player with Play/Pause controls
- Drag the seek bar to navigate through the recording

### Setting Reminder
Open note → Tap **Reminder** → Select Date and Time

### Reordering Notes
**Long press** the handle (≡) → Drag up/down → Order saved automatically

### Editing Notes
**Swipe RIGHT** on a note → Edit in Markdown editor → Save **OR** Open note → Tap Edit

### Deleting Note
**Swipe LEFT** → Confirm **OR** Open note → Tap Delete Note

---

## 📱 Requirements

- **Android 7.0 (API 24)** or higher
- **Google Play Services** (for speech recognition)
- **Microphone** (for audio recording and voice input)
- **Camera** (optional, for photo attachments)
- ~15 MB storage (increases with attachments)

---

## 📥 Download

You can download the application from the [Releases](https://github.com/aixandrolab/smart-notebook/releases/tag/v1.0.4) page

---

## 📂 Storage Structure

```
Documents/smart-notebook/
├── notes.json          # All note data
├── order.json          # Custom note order
├── images/             # Image attachments
│   └── IMG_*.jpg
├── files/              # File attachments
│   └── file_*.*
└── audio/              # Audio recordings
    └── AUDIO_*.m4a
```

---

## 🔒 Permissions

| Permission          | Purpose                                          |
|---------------------|--------------------------------------------------|
| Storage             | Read/write notes and attachments                 |
| Camera              | Capture photos for notes                         |
| Microphone          | Voice input and audio recording                  |
| Notifications       | Show reminders                                   |
| Exact Alarm         | Precise reminder timing                          |
| Post Notifications  | Display notification (Android 13+)               |

---

## 🛠️ Technologies

- **Kotlin** – Primary language
- **Android SDK 24+** – Compatible with older devices
- **Material Design** – Modern UI components
- **Gson** – JSON parsing and serialization
- **RecyclerView with ItemTouchHelper** – Drag & drop and swipe gestures
- **MediaRecorder/MediaPlayer** – Audio recording and playback
- **SpeechRecognizer API** – Voice input
- **Markwon** – Markdown rendering
- **FileProvider** – Secure file sharing

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on emulator or device

---

## 🐛 Bug Reports

Found a bug? Please open an issue with:
- Device model and Android version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable

---

## 📝 License

© 2026 Smart Notebook – All Rights Reserved

This project is proprietary software. All rights reserved by Alexander Suvorov.

---

## 👨‍💻 Author

**Alexander Suvorov (Aixandrolab)**  
[GitHub](https://github.com/aixandrolab)

**Official site of SmartLegionLab's Team** - [smartlegionlab.com](https://smartlegionlab.com)

---

**Made with ❤️ for everyone who loves to take notes**