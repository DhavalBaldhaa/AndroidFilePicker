package com.filepickersample.enumeration

enum class FileSelectionType(val id: Int, val value: String) {
    ALL(1, "ALL"),  // All option provides
    IMAGE(2, "IMAGE"),  // Image from gallery and camera
    VIDEO(3, "VIDEO"),  // Video from gallery and video
    CAPTURE_IMAGE(4, "TAKE_IMAGE"), // Capture image from camera
    CAPTURE_VIDEO(5, "TAKE_VIDEO"), // Capture video from camera
    PICK_IMAGE(6, "PICK_IMAGE"), // Select image from gallery
    PICK_VIDEO(7, "PICK_VIDEO"), // Select video from gallery
    TAKE_IMAGE_VIDEO(8, "TAKE_IMAGE_VIDEO"), // Capture image and video from camera
    PICK_IMAGE_VIDEO(9, "PICK_IMAGE_VIDEO"), // Select image and video from gallery
    PICK_DOCUMENT(10, "PICK_DOCUMENT"), // Select any file
}