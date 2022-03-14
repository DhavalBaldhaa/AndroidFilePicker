package com.filepickersample.enumeration

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.filepickersample.utils.FileUtil.getAudioDirectory
import com.filepickersample.utils.FileUtil.getDocumentDirectory
import com.filepickersample.utils.FileUtil.getImageDirectory
import com.filepickersample.utils.FileUtil.getThumbDirectory
import com.filepickersample.utils.FileUtil.getVideoDirectory
import com.filepickersample.utils.MediaLog
import java.io.File
import java.util.*

enum class FileType(var id: Int) : Parcelable {
    IMAGE(1),
    GIF(2),
    VIDEO(3),
    AUDIO(4),
    DOCUMENT(5),
    PDF(6),
    THUMB(7);

    companion object {
        private var enumMapId: Map<Int, FileType>? = null
        private var enumMapNames: Map<String, FileType>? = null

        init {
            val mapID: HashMap<Int, FileType> = HashMap()
            val mapName: HashMap<String, FileType> = HashMap()

            values().forEach { mediaType ->
                mapID[mediaType.id] = mediaType
                mapName[mediaType.name] = mediaType
            }

            enumMapId = Collections.unmodifiableMap(mapID)
            enumMapNames = Collections.unmodifiableMap(mapName)
        }

        operator fun get(id: Int?): FileType {
            if (id == null) return IMAGE
            return enumMapId!![id] ?: IMAGE
        }

        operator fun get(name: String?): FileType {
            if (name == null) return IMAGE
            return enumMapNames!![name] ?: IMAGE
        }

        @JvmField
        val CREATOR: Parcelable.Creator<FileType> = object : Parcelable.Creator<FileType> {
            override fun createFromParcel(`in`: Parcel): FileType {
                return values()[`in`.readInt()]
            }

            override fun newArray(size: Int): Array<FileType?> {
                return arrayOfNulls(size)
            }
        }

        fun getExtension(fileType: FileType): String {
            return when (fileType) {
                IMAGE,THUMB -> ".jpg"
                VIDEO -> ".mp4"
                AUDIO -> ".m4a"
                GIF -> ".gif"
                DOCUMENT -> ".doc"
                PDF -> ".pdf"
            }
        }

        fun getRootDirectory(context: Context?, fileType: FileType?): String {
            if (context == null) return ""
            if (fileType == null) return ""
            val DIRECTORY = when (fileType) {
                IMAGE -> getImageDirectory(context)
                THUMB -> getThumbDirectory(context)
                VIDEO -> getVideoDirectory(context)
                AUDIO -> getAudioDirectory(context)
                GIF -> getImageDirectory(context)
                DOCUMENT -> getDocumentDirectory(context)
                PDF -> getDocumentDirectory(context)
            }
            if (!DIRECTORY.exists()) {
                val isCreated = DIRECTORY.mkdirs()
                MediaLog.e("getRootDirectory isCreated $isCreated")
            }
            return DIRECTORY.path + File.separator
        }

        fun getMime(fileType: FileType): String {
            return when (fileType) {
                IMAGE -> "image/*"
                VIDEO -> "video/*"
                AUDIO -> "audio/*"
                GIF -> "image/*"
                PDF -> "pdf/*"
                DOCUMENT -> "doc/*"
                else -> "*/*"
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
    }
}