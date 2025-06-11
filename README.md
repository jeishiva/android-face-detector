# Photo Finder

## 📱 App Overview

This app provides a smart and efficient way to manage user-captured camera images by automatically detecting faces, generating thumbnails, and allowing face tagging. It uses modern Android development practices and leverages key Jetpack and ML components for a clean, responsive, and optimized experience.

---

## ✅ **Core Functionality**

* Requests **local storage access** to identify camera images captured by the user.
* Automatically detects **faces** using **Google ML Kit (Face Detection)**.
* Generates and stores **thumbnails** (200x200, \~50KB JPEG) for images with at least one detected face.
* Displays all such filtered images in a **lazy-loading gallery screen** using **Jetpack Compose + Paging 3**.
* On tapping a gallery item, the **full-resolution image** (1280x720) is displayed.
* Users can tap on **face bounding boxes** to **add custom tags**.
* Tags are persisted using **Room** local database.

---

## 🏗️ **Architecture & Components**

* **Architecture**: MVVM (Model-View-ViewModel)
* **UI**: Jetpack Compose
* **Navigation**: Jetpack Navigation
* **Image Loading**: Coil
* **Face Detection**: Google ML Kit
* **Persistence**: Room Database
* **Background Processing**: WorkManager
* **Paging**: Paging 3 for lazy loading and memory-efficient gallery display

---

## ⚙️ **Performance & Optimization**

1. **Batch Processing**: Camera images are processed in batches for face detection and thumbnail generation.
2. **Bitmap Pooling**: Reuses bitmaps to reduce memory pressure and avoid out-of-memory (OOM) errors.
3. **Lazy Loading**: Paging 3 combined with Room ensures only visible data is loaded to minimize memory usage.
4. **Efficient Thumbnails**:Thumbnails are generated with a resolution of 200x200 and stored in JPEG format, ensuring broad device compatibility. While WebP offers smaller file sizes, JPEG was chosen due to broader support below API 30, aligning with compatibility requirements.
5. **Optimized Full Images**: Full images are rendered in 1280x720 resolution, balancing quality and performance.
6. **Local Persistence**: Face tags are stored locally using Room for offline availability and quick access.
7. **Jetpack Compose**: Modern declarative UI ensures better performance and maintainability compared to traditional XML-based UI.
8. **WorkManager**: Handles background syncing of added camera images, can be used to work in a battery- and system-aware manner.

---

