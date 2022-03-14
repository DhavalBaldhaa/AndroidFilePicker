package com.filepickersample.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import java.io.File

class MediaScannerClient(context: Context?, private val file: File) : MediaScannerConnectionClient {
    private var mediaScannerConnection: MediaScannerConnection?
    override fun onMediaScannerConnected() {
        if (mediaScannerConnection == null) return
        mediaScannerConnection!!.scanFile(file.path, "*/*")
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        if (mediaScannerConnection == null) return
        mediaScannerConnection!!.disconnect()
    }

    fun setScanner(mediaScannerConnection: MediaScannerConnection?) {
        this.mediaScannerConnection = mediaScannerConnection
    }

    fun connect() {
        if (mediaScannerConnection == null) return
        mediaScannerConnection!!.connect()
    }

    init {
        mediaScannerConnection = MediaScannerConnection(context, this)
    }
}