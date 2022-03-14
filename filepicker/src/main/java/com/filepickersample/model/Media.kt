package com.filepickersample.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.filepickersample.enumeration.FileType
import com.filepickersample.enumeration.FileType.IMAGE
import com.filepickersample.utils.FileUtil
import java.io.File

class Media() : Parcelable {
    var url: String = ""
    var thumbUrl: String = ""
    var thumb: ByteArray? = null
    var fileType: FileType = IMAGE

    val hasThumb: Boolean get() = File(thumbUrl).exists()

    fun isExist(context: Context?): Boolean = FileUtil.isAvailable(context, this)

    fun getRootDirectory(context: Context?): String = FileType.getRootDirectory(context, fileType)

    val filename: String
        get() = FileUtil.getFileName(url)

    val mediaFile: File
        get() = File(url)

    val formattedFileName: String get() = FileUtil.getFormattedFilename(filename)

    val extension: String get() = FileUtil.getExtensionName(filename)

    constructor(parcel: Parcel) : this() {
        url = parcel.readString().orEmpty()
        thumbUrl = parcel.readString().orEmpty()
        thumb = parcel.createByteArray()
        fileType = parcel.readParcelable(FileType::class.java.classLoader) ?: IMAGE
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(thumbUrl)
        parcel.writeByteArray(thumb)
        parcel.writeParcelable(fileType, flags)
    }

    companion object CREATOR : Parcelable.Creator<Media> {
        override fun createFromParcel(parcel: Parcel): Media {
            return Media(parcel)
        }

        override fun newArray(size: Int): Array<Media?> {
            return arrayOfNulls(size)
        }

        fun create(thumb: Thumb): Media {
            val media = Media()
            media.fileType = thumb.fileType
            media.url = thumb.file.path
            if (thumb.thumb != null) {
                media.thumbUrl = thumb.thumb!!.path
                media.thumb = thumb.bytes
            }
            return media
        }
    }
}