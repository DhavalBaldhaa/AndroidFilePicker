package com.filepickersample.model

import android.content.Context
import android.util.Size
import com.filepickersample.enumeration.FileType
import com.filepickersample.utils.FileUtil
import java.io.File

class Thumb {
    var fileType: FileType
    var file: File
    var thumb: File? = null
    var bytes: ByteArray? = null

    constructor(fileType: FileType, file: File) {
        this.fileType = fileType
        this.file = file
    }

    constructor(fileType: FileType, file: File, thumb: File?, bytes: ByteArray?) {
        this.fileType = fileType
        this.file = file
        this.thumb = thumb
        this.bytes = bytes
    }

    companion object {
        const val THUMB_SIZE = 320
        val SIZE =  Size(THUMB_SIZE, THUMB_SIZE)

        fun generateThumb(context: Context, fileType: FileType, file: File): Thumb {
            return FileUtil.getThumb(context, fileType, file)
        }

        fun generateThumb(fileType: FileType, file: File): Thumb {
            return Thumb(fileType, file, null, null)
        }
    }
}