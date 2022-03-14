package com.filepickersample.listener

import com.filepickersample.model.Media

interface FilePickerCallback {
    fun onSuccess(media: Media?) {}
    fun onSuccess(mediaList: ArrayList<Media>?) {}
    fun onError(error: String?) {}
    fun onProgress(enable: Boolean) {}
}