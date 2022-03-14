package com.filepickersample.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.filepickersample.enumeration.FileType
import com.filepickersample.model.Media
import com.filepickersample.model.Thumb
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.ResolutionConstraint
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.DecimalFormat

object FileUtil {
    const val BULLETIN = "\u2022"
    const val DOT = "."
    const val UNDER_SCORE = "_"
    private const val ROOT_DIRECTORY = "/MediaFiles/"
    private const val IMAGE_DIRECTORY = "Images/"
    private const val VIDEO_DIRECTORY = "Videos/"
    private const val AUDIO_DIRECTORY = "Audios/"
    private const val DOCUMENT_DIRECTORY = "Documents/"
    private const val CONTACT_DIRECTORY = "Contacts/"
    private const val ASSET_DIRECTORY = "Assets/"
    private const val BACKUP_DIRECTORY = ".Backup/"
    private const val RESTORE_DIRECTORY = BACKUP_DIRECTORY + "Restore/"
    private const val THUMB_DIRECTORY = IMAGE_DIRECTORY + "Thumbs/"
    private const val PROFILE_PHOTO_DIRECTORY = IMAGE_DIRECTORY + "Profile Photos/"
    const val IMAGE_PREFIX = "IMG_"

    const val ORIGINAL_QUALITY = 100

    private fun getExternalStoragePath(context: Context): String {
        return context.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath + ROOT_DIRECTORY
    }

    @JvmStatic
    fun getRootDirectory(context: Context): File {
        val root = File(getExternalStoragePath(context))
        root.mkdirs()
        return root
    }

    fun getDirectory(context: Context, sub_directory: String): File {
        val root = File(getExternalStoragePath(context) + sub_directory)
        root.mkdirs()
        return root
    }

    @JvmStatic
    fun getImageDirectory(context: Context): File {
        return getDirectory(context, IMAGE_DIRECTORY)
    }

    @JvmStatic
    fun getThumbDirectory(context: Context): File {
        return getDirectory(context, THUMB_DIRECTORY)
    }

    @JvmStatic
    fun getVideoDirectory(context: Context): File {
        return getDirectory(context, VIDEO_DIRECTORY)
    }

    @JvmStatic
    fun getAudioDirectory(context: Context): File {
        return getDirectory(context, AUDIO_DIRECTORY)
    }

    @JvmStatic
    fun getDocumentDirectory(context: Context): File {
        return getDirectory(context, DOCUMENT_DIRECTORY)
    }

    @JvmStatic
    fun getContactDirectory(context: Context): File {
        return getDirectory(context, CONTACT_DIRECTORY)
    }

    fun getBackupDirectory(context: Context): File {
        return getDirectory(context, BACKUP_DIRECTORY)
    }

    fun getRestoreDirectory(context: Context): File {
        return getDirectory(context, RESTORE_DIRECTORY)
    }

    fun getURI(context: Context?, applicationId: String, file: File?): Uri {
        return FileProvider.getUriForFile(
            context!!,
            "$applicationId.provider",
            file!!
        )
    }

    fun createNewFile(context: Context?, fileType: FileType): File {
        val root = FileType.getRootDirectory(context, fileType)
        val extension = FileType.getExtension(fileType)
        return File(
            root,
            fileType.name + UNDER_SCORE + System.currentTimeMillis() + extension
        )
    }

    fun createNewFile(context: Context?, fileType: FileType, extension: String?): File {
        var ext = extension
        val root = FileType.getRootDirectory(context, fileType)
        ext = prefixIsNotThere(ext, DOT)
        ext = ext ?: FileType.getExtension(fileType)
        return File(root, fileType.name + UNDER_SCORE + System.currentTimeMillis() + ext)
    }

    fun createFile(context: Context?, file_name: String, fileType: FileType): File {
        val root = FileType.getRootDirectory(context, fileType)
        val extension = FileType.getExtension(fileType)
        return File(root, fileType.name + UNDER_SCORE + file_name + extension)
    }

    fun createNewFile(
        context: Context?,
        file_name: String,
        fileType: FileType?,
        extension: String?,
    ): File {
        var filename = file_name
        val root = FileType.getRootDirectory(context, fileType)
        return if (hasExtension(filename)) {
            filename = addUniqueness(filename)
            File(root, filename)
        } else File(
            root,
            filename + UNDER_SCORE + System.currentTimeMillis() + prefixIsNotThere(extension, DOT)
        )
    }

    private fun addUniqueness(filename: String): String {
        val name = filename.substring(0, filename.lastIndexOf("."))
        val extension = filename.substring(filename.lastIndexOf("."))
        return name + UNDER_SCORE + System.currentTimeMillis() + extension
    }

    fun prefixIsNotThere(extension: String?, prefix: String): String? {
        if (extension == null) return extension
        return if (extension.contains(prefix)) extension else prefix + extension
    }

    fun createNewRandomFile(context: Context, prefix_: String?, extension: String?): File {
        var prefix = prefix_
        if (prefix == null || prefix.isEmpty()) prefix = IMAGE_PREFIX
        val root = getRootDirectory(context)
        return if (extension != null) {
            File(root, prefix + System.currentTimeMillis() + extension)
        } else {
            File(root, prefix + System.currentTimeMillis())
        }
    }

    fun createNewFile(context: Context, fileName: String, extension: String?): File {
        val root = getRootDirectory(context)
        return if (extension != null) {
            File(root, fileName + extension)
        } else {
            File(root, fileName)
        }
    }

    fun createNewFile(context: Context?, file_name: String, fileType: FileType): File {
        val root = FileType.getRootDirectory(context, fileType)
        return File(root, file_name)
    }

    @Throws(Exception::class)
    fun convertStreamToString(inputStream: InputStream?): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line).append("\n")
        }
        reader.close()
        return sb.toString()
    }

    @Throws(Exception::class)
    fun getStringFromFile(file_: File?): String {
        if (file_ == null) return ""
        val fin = FileInputStream(file_)
        val ret = convertStreamToString(fin)
        fin.close()
        return ret
    }

    fun isAvailable(context: Context?, media: Media?): Boolean {
        if (media == null) return false
        val base = FileType.getRootDirectory(context, media.fileType)
        return isFileExist(File(base, media.filename))
    }

    fun createNewFile(path: String?, prefix_: String?, extension: String?): File {
        var prefix = prefix_
        if (prefix == null || prefix.isEmpty()) {
            prefix = "IMG_"
        }
        return if (extension != null) {
            File(path, prefix + System.currentTimeMillis() + extension)
        } else {
            File(path, prefix + System.currentTimeMillis())
        }
    }

    fun createNewFile(
        context: Context,
        raw_data: ByteArray,
        prefix: String,
        extension: String?,
    ): File? {
        try {
            val file = createNewFile(context, prefix, extension)
            val bos = BufferedOutputStream(FileOutputStream(file))
            bos.write(raw_data)
            bos.flush()
            bos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("Recycle")
    fun getNewPath(context: Context, uri: Uri?, fileType: FileType): File? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri!!, projection, null, null, null)
            ?: return null
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return if (path == null) {
            null
        } else {
            copy(File(path), createNewFile(context, fileType))
        }
    }

    fun copy(src: File?, dst: File?): File? {
        if (src == null || dst == null) return null
        val inputStream: InputStream?
        try {
            inputStream = FileInputStream(src)
            val out: OutputStream = FileOutputStream(dst)

            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            inputStream.close()
            out.close()
            return dst
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun addNoMedia(context: Context) {
        val noMedia = createNewFile(context, ".nomedia", null)
        try {
            if (noMedia.mkdirs()) {
                val isCreated = noMedia.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    fun getThumb(context: Context?, fileType: FileType, file: File): Thumb {
        try {
            val bitmap: Bitmap? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (fileType == FileType.VIDEO)
                        ThumbnailUtils.createVideoThumbnail(file, Thumb.SIZE, null)
                    else ThumbnailUtils.createImageThumbnail(file, Thumb.SIZE, null)
                } else {
                    if (fileType == FileType.VIDEO)
                        ThumbnailUtils.createVideoThumbnail(
                            file.path,
                            MediaStore.Video.Thumbnails.MINI_KIND
                        )
                    else ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(file.path),
                        Thumb.THUMB_SIZE,
                        Thumb.THUMB_SIZE
                    )
                }

            val thumb = saveBitmapImage(context, bitmap, FileType.THUMB)
            bitmap?.recycle()
            return Thumb(fileType, file, thumb, null /*getCompressedByte(thumb)*/)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Thumb(fileType, file, null, null)
//        Bitmap compressed_bitmap = compress(thumb, THUMB_SIZE, THUMB_SIZE, 50);
//        byte[] bytes = getCompressedByte(compressed_bitmap == null ? bitmap : compressed_bitmap);
    }

    fun getFileFromUri(context: Context, uri: Uri?, fileType: FileType): File? {
        if (uri == null || uri.path == null) return null
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val filename = getFileName(context, uri)
            var extension: String? = getExtensionName(filename)
            if (extension == null || extension.isEmpty()) {
                extension = getMimeType(context, uri)
            }
            val file = filename?.let { createNewFile(context, it, fileType, extension) }
                ?: createNewFile(context, fileType, extension)
            writeStreamToFile(inputStream, file)
            return file
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun getFileFromUriWithOriginal(context: Context, uri: Uri?, fileType: FileType): File? {
        if (uri == null || uri.path == null) return null
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val filename = getFileName(context, uri)
            val file = createNewFile(context, filename.orEmpty(), fileType)
            writeStreamToFile(inputStream, file)
            return file
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun addContentToFile(context: Context, uri: Uri?, outputFile: File): File? {
        if (uri == null || uri.path == null) return null
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            writeStreamToFile(inputStream, outputFile)
            return outputFile
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    private fun hasExtension(filename: String?): Boolean {
        if (filename == null) return false
        return filename.contains(".")
    }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == null) return null
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) result =
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
            result = uri.path
        }
        if (result == null) return null
        val cut = result.lastIndexOf('/')
        if (cut != -1) result = result.substring(cut + 1)
        return result
    }

    private fun writeStreamToFile(inputStream: InputStream, file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                if (file.length() > 0) return
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        var out: OutputStream? = null
        try {
            out = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        val extension: String?
        extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
        return extension
    }


    suspend fun imageCompress(context: Context, file: File, fileType: FileType): File {
        try {
            val compressedFile = Compressor.compress(context, file) {
                quality(50)
                format(CompressFormat.JPEG)
                destination(
                    File(
                        FileType.getRootDirectory(
                            context,
                            fileType
                        ) + file.nameWithoutExtension + UNDER_SCORE + "compressed" + FileType.getExtension(
                            fileType
                        )
                    )
                )
            }
            if (file.exists()) file.delete()
            return compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    suspend fun imageCompress(
        context: Context,
        file: File,
        fileType: FileType,
        quality: Int = 50,
        format: CompressFormat = CompressFormat.JPEG,
        compressResolution: ResolutionConstraint? = null,
    ): File {
        try {
            val compressedFile = Compressor.compress(context, file) {
                quality(quality)
                format(format)
                if (compressResolution != null) constraint(compressResolution)
                destination(
                    File(
                        FileType.getRootDirectory(
                            context,
                            fileType
                        ) + file.nameWithoutExtension + UNDER_SCORE + "compressed" + FileType.getExtension(
                            fileType
                        )
                    )
                )
            }
            if (file.exists()) file.delete()
            return compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    private fun saveBitmapImage(context: Context?, bitmap: Bitmap?): File? {
        return saveBitmapImage(context, bitmap, FileType.IMAGE)
    }

    fun saveBitmapImage(context: Context?, bitmap: Bitmap?, fileType: FileType): File? {
        return if (bitmap == null) null
        else
            try {
                val file = createNewFile(context, fileType)
                saveBitmapImage(bitmap, file, 90)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    }


    fun saveBitmapImage(bitmap: Bitmap?, outputFile: File, quality: Int): File? {
        return if (bitmap == null) null
        else
            try {
                val fos = FileOutputStream(outputFile)
                val bos = ByteArrayOutputStream()
                bitmap.compress(CompressFormat.WEBP, quality, bos)
                fos.write(bos.toByteArray())
                fos.flush()
                fos.close()
                outputFile
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
    }

    fun saveOriginalBitmap(bitmap: Bitmap?, outputFile: File): File? {
        return if (bitmap == null) null
        else
            try {
                val fos = FileOutputStream(outputFile)
                val bos = ByteArrayOutputStream()
                bitmap.compress(CompressFormat.WEBP, 100, bos)
                fos.write(bos.toByteArray())
                fos.flush()
                fos.close()
                outputFile
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
    }

    fun saveString(file: File?, raw: String?): File? {
        return if (raw == null || file == null) null else try {
            val fos = FileOutputStream(file)
            fos.write(raw.toByteArray())
            fos.flush()
            fos.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getThumbnail(context: Context?, fileType: FileType, file: File): Thumb {
        return getThumb(context, fileType, file)
    }

    fun getCompressedByte(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 0, stream)
        return stream.toByteArray()
    }

    fun getCompressedByte(file: File?): ByteArray? {
        if (file == null) return null
        return getCompressedByte(BitmapFactory.decodeFile(file.path))
    }

    fun isFileExist(context: Context, name: String): Boolean {
        return createNewFile(context, name, null).exists()
    }

    fun isFileExist(file: File): Boolean {
        return file.exists()
    }

    fun getFileName(url: String?): String {
        if (url == null || url.isEmpty()) return System.currentTimeMillis()
            .toString() else if (url.contains("/")) return url.substring(url.lastIndexOf("/") + 1)
        return url
    }

    fun getExtensionName(url: String?): String {
        if (url == null || url.isEmpty() || !url.contains(".")) return "" else if (url.contains(".")) return url.substring(
            url.lastIndexOf(".") + 1
        )
        return url
    }

    fun getExtensionWithDot(url: String?): String {
        if (url == null || url.isEmpty() || !url.contains(".")) return "" else if (url.contains(".")) return url.substring(
            url.lastIndexOf(".")
        )
        return url
    }

    fun fileNameWithOutExtension(url: String?): String {
        return if (url == null || url.isEmpty()) {
            System.currentTimeMillis().toString()
        } else if (url.contains("/")) {
            if (url.contains(".")) url.substring(
                url.lastIndexOf("/"),
                url.lastIndexOf('.')
            ) else url.substring(url.lastIndexOf("/"))
        } else {
            if (url.contains(".")) url.substring(0, url.lastIndexOf('.')) else url
        }
    }

    fun humanReadableByteCountSI(bytes: Long): String {
        val s = if (bytes < 0) "-" else ""
        var b = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        return when {
            b < 1000L -> "$bytes B"
            b < 999950L -> String.format("%s%.1f KB", s, b / 1e3)
            1000.let { b /= it; b } < 999950L -> String.format("%s%.1f MB", s, b / 1e3)
            1000.let { b /= it; b } < 999950L -> String.format("%s%.1f GB", s, b / 1e3)
            1000.let { b /= it; b } < 999950L -> String.format("%s%.1f TB", s, b / 1e3)
            1000.let { b /= it; b } < 999950L -> String.format("%s%.1f PB", s, b / 1e3)
            else -> String.format("%s%.1f EB", s, b / 1e6)
        }
    }

    private val DECIMAL_SIZE_FORMAT = DecimalFormat("#,##0.#")
    private val units = arrayOf("B", "KB", "MB", "GB", "TB")
    fun getFileSize(size: Long): String {
        if (size <= 0) return "0"
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return "Size " + DECIMAL_SIZE_FORMAT.format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }

    fun intentMedia(context: Context, applicationId: String, media: Media) {
        try {
            val uri = getURI(context, applicationId, media.mediaFile)
            val mime = context.contentResolver.getType(uri)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, mime)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open file, Couldn't find any installed app to open this media",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun initGalleryScanner(context: Context, file: File) {
        MediaScannerClient(context, file).connect()
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(getRootDirectory(context))
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    fun getFormattedFilename(name_: String): String {
        var name = name_
        if (name.isEmpty()) return name
        val extension = getExtensionWithDot(name)
        if (name.contains(UNDER_SCORE)) {
            name = name.substring(0, name.lastIndexOf("_"))
            return name + extension
        }
        return name
    }

    fun openVCard(context: Context, file: File?) {
        try {
            val intent = Intent()
            intent.setDataAndType(
                Uri.fromFile(file),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("vcf")
            )
            try {
                if (context.packageManager != null) {
                    val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
                    for (resolveInfo in resolveInfos) {
                        val activityInfo = resolveInfo.activityInfo
                        if (activityInfo != null) {
                            val packageName = activityInfo.packageName
                            val name = activityInfo.name
                            if (packageName != null && packageName == "com.android.contacts" && name != null && name.contains(
                                    "ImportVCardActivity"
                                )
                            ) {
                                intent.setPackage(packageName)
                                break
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
            context.startActivity(intent)
        } catch (exception: Exception) {
        }
    }


    fun getBase64String(file: File): String? {
        try {
            var bytesRead: Int
            val buffer = ByteArray(8192)
            val inputStream: InputStream = FileInputStream(file)
            val output = ByteArrayOutputStream()
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            return Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getCompressFormat(extension: String): Bitmap.CompressFormat {
        return when {
            extension.contains("png", ignoreCase = true) -> Bitmap.CompressFormat.PNG
            extension.contains("webp", ignoreCase = true) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    Bitmap.CompressFormat.WEBP
                }
            }
            else -> Bitmap.CompressFormat.JPEG
        }
    }

}