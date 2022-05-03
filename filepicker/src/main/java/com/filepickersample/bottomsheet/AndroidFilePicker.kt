package com.filepickersample.bottomsheet

import android.Manifest.permission.*
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap.CompressFormat
import android.graphics.Bitmap.CompressFormat.JPEG
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.filepickersample.R
import com.filepickersample.databinding.BottomSheetFilePickerLayoutBinding
import com.filepickersample.enumeration.FileSelectionType
import com.filepickersample.enumeration.FileSelectionType.*
import com.filepickersample.enumeration.FileType
import com.filepickersample.enumeration.FileType.Companion.getRootDirectory
import com.filepickersample.enumeration.FileType.DOCUMENT
import com.filepickersample.listener.FilePickerCallback
import com.filepickersample.model.Media
import com.filepickersample.model.Thumb.Companion.generateThumb
import com.filepickersample.utils.FileUtil
import com.filepickersample.utils.FileUtil.UNDER_SCORE
import com.filepickersample.utils.FileUtil.getFileFromUri
import com.filepickersample.utils.FileUtil.getFileFromUriWithOriginal
import com.filepickersample.utils.FileUtil.imageCompress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yalantis.ucrop.UCrop
import id.zelory.compressor.constraint.ResolutionConstraint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.File

/**
 * Created by Dhaval Baldha on 22/12/2020.
 */

// refer : https://developer.android.com/training/data-storage/shared/media#request-permissions
open class AndroidFilePicker(private val applicationId: String) : BaseFilePicker(),
    OnClickListener {

    private val activityLauncher = CustomActivityResult.registerActivityForResult(this)
    private lateinit var binding: BottomSheetFilePickerLayoutBinding

    @LayoutRes
    private var customLayoutRes: Int? = null

    private var callback: FilePickerCallback? = null

    private var directAction = false
    private var fileSelectionType = ALL
    private var directActionType: FileSelectionType? = null

    private var actionButtonBg: Int? = null
    private var cancelButtonBg: Int? = null
    private var cancelButtonTextColor: Int? = null
    private var actionButtonTextColor: Int? = null

    private var quality: Int = 50
    private var compressFormat: CompressFormat = JPEG
    private var compressResolution: ResolutionConstraint? = null

    // Crop Parameters
    private var cropX: Float = 0f
    private var cropY: Float = 0f
    private var crop: Boolean = false

    // Resize Parameters
    private var maxCropWidth: Int = 0
    private var maxCropHeight: Int = 0

    private var documentType = "*/*"
    private var isMultiSelection = false
    private var documentWithOriginalName = false

    constructor(applicationId: String, @StyleRes themeId: Int) : this(applicationId) {
        this.themeId = themeId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            if (customLayoutRes != null)
                BottomSheetFilePickerLayoutBinding.bind(inflater.inflate(customLayoutRes!!, null))
            else
                BottomSheetFilePickerLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (customLayoutRes == null) updateUiTheme()
        mapping()
        if (directAction) selectFile()
    }

    private fun updateUiTheme() {
        if (actionButtonBg != null) {
            with(binding) {
                btnCaptureImage.setBackgroundResource(actionButtonBg!!)
                btnChooseImage.setBackgroundResource(actionButtonBg!!)
                btnCaptureVideo.setBackgroundResource(actionButtonBg!!)
                btnChooseVideo.setBackgroundResource(actionButtonBg!!)
            }
        }
        if (actionButtonTextColor != null) {
            with(binding) {
                btnCaptureImage.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        actionButtonTextColor!!
                    )
                )
                btnChooseImage.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        actionButtonTextColor!!
                    )
                )
                btnCaptureVideo.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        actionButtonTextColor!!
                    )
                )
                btnChooseVideo.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        actionButtonTextColor!!
                    )
                )
            }
        }
        if (cancelButtonBg != null) {
            binding.btnCancel.setBackgroundResource(cancelButtonBg!!)
        }
        if (cancelButtonTextColor != null) {
            binding.btnCancel.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    cancelButtonTextColor!!
                )
            )
        }
    }

    private fun mapping() {
        with(binding) {
            btnCaptureImage.setText(R.string.take_a_photo)
            btnChooseImage.setText(R.string.choose_image_from_gallery)
            btnCaptureVideo.setText(R.string.take_a_video)
            btnChooseVideo.setText(R.string.choose_video_from_gallery)
            btnCancel.setText(R.string.cancel)
        }

        binding.btnCaptureImage.setOnClickListener(this)
        binding.btnChooseImage.setOnClickListener(this)
        binding.btnCaptureVideo.setOnClickListener(this)
        binding.btnChooseVideo.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)

        binding.btnCaptureImage.visibility = GONE
        binding.btnChooseImage.visibility = GONE
        binding.btnCaptureVideo.visibility = GONE
        binding.btnChooseVideo.visibility = GONE

        when (fileSelectionType) {
            ALL -> {
                binding.btnCaptureImage.visibility = VISIBLE
                binding.btnChooseImage.visibility = VISIBLE
                binding.btnCaptureVideo.visibility = VISIBLE
                binding.btnChooseVideo.visibility = VISIBLE
            }

            IMAGE -> {
                binding.btnCaptureImage.visibility = VISIBLE
                binding.btnChooseImage.visibility = VISIBLE
            }

            VIDEO -> {
                binding.btnCaptureVideo.visibility = VISIBLE
                binding.btnChooseVideo.visibility = VISIBLE
            }

            CAPTURE_IMAGE -> {
                binding.btnCaptureImage.visibility = VISIBLE
                directActionType = CAPTURE_IMAGE
                directAction = true
            }

            CAPTURE_VIDEO -> {
                binding.btnCaptureVideo.visibility = VISIBLE
                directActionType = CAPTURE_VIDEO
                directAction = true
            }

            TAKE_IMAGE_VIDEO -> {
                binding.btnCaptureImage.visibility = VISIBLE
                binding.btnCaptureVideo.visibility = VISIBLE
            }

            PICK_IMAGE -> {
                binding.btnChooseImage.visibility = VISIBLE
                directActionType = PICK_IMAGE
                directAction = true
            }

            PICK_VIDEO -> {
                binding.btnChooseVideo.visibility = VISIBLE
                directActionType = PICK_IMAGE
                directAction = true
            }

            PICK_IMAGE_VIDEO -> {
                binding.btnChooseImage.visibility = VISIBLE
                binding.btnChooseVideo.visibility = VISIBLE
            }

            else -> {
                directActionType = fileSelectionType
                directAction = true
            }
        }
    }

    override fun onClick(view: View) {
        when (view) {
            binding.btnCancel -> hideBottomSheet()
            binding.btnCaptureImage -> {
                directActionType = CAPTURE_IMAGE
                selectFile()
            }
            binding.btnCaptureVideo -> {
                directActionType = CAPTURE_VIDEO
                selectFile()
            }
            binding.btnChooseImage -> {
                directActionType = PICK_IMAGE
                selectFile()
            }
            binding.btnChooseVideo -> {
                directActionType = PICK_VIDEO
                selectFile()
            }
        }
    }

    private fun requestPermission(): Boolean {
        if (EasyPermissions.hasPermissions(mContext, *permissions)) return true
        requestPermissions(
            this,
            getString(R.string.permission_camera_rationale),
            RQ_FILE_PERMISSION,
            permissions
        )
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            permissionCallbacks
        )
    }

    private val permissionCallbacks = object : PermissionCallbacks {
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
        ) = Unit

        override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//            selectFile()
        }

        override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
            if (context == null || activity == null) {
                hideBottomSheet()
                return
            }
            if (EasyPermissions.somePermissionPermanentlyDenied(activity!!, perms)) {
                AlertDialog.Builder(context!!)
                    .setMessage(context?.getString(R.string.permission_camera_rationale))
                    .setCancelable(false)
                    .setPositiveButton(context?.getString(R.string.ok)) { dialog, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", applicationId, null)
                        intent.data = uri
                        activityLauncher.launch(intent) { result -> selectFile() }
                    }
                    .setNegativeButton(context?.getString(R.string.cancel)) { dialog, which ->
                        hideBottomSheet()
                    }
                    .create()
                    .show()
            } else {
                hideBottomSheet()
            }
        }
    }

    @AfterPermissionGranted(RQ_FILE_PERMISSION)
    private fun selectFile() {
        if (!requestPermission()) return

        when (directActionType) {
            CAPTURE_IMAGE -> takeImage()
            CAPTURE_VIDEO -> takeVideo()
            PICK_IMAGE -> pickImage()
            PICK_VIDEO -> pickVideo()
            PICK_DOCUMENT -> pickDocument()
            else -> {}
        }
    }

    private fun showProgressBar(enable: Boolean) {
        if (callback == null) return
        executeOnMain { callback?.onProgress(enable) }
    }

    override fun onShow(dialog: DialogInterface) {
        super.onShow(dialog)
        if (!directAction) return
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior?.setPeekHeight(10, true)
        }
    }

    /**
     * File actions
     * */
    // capture image using in-build camera
    private fun takeImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var file = FileUtil.createNewFile(context, FileType.IMAGE)
        val uri: Uri = FileUtil.getURI(context, applicationId, file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        activityLauncher.launch(Intent.createChooser(intent, "Capture Using")) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (crop) {
                    cropImage(file)
                } else {
                    var media: Media? = null
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            file = getCompressedImage(file)
                            media = Media.create(generateThumb(mContext, FileType.IMAGE, file))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            media = null
                        }
                    }.invokeOnCompletion {
                        sendCallBack(media)
                    }
                }
            } else {
                sendCancelCallBack()
            }
        }
    }

    // choose image from gallery
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        if (isMultiSelection) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        activityLauncher.launch(Intent.createChooser(intent, "Select Photo")) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (isMultiSelection) {
                    val imgList = ArrayList<Media>()
                    try {
                        if (result.data?.clipData != null) {
                            val count: Int = result.data?.clipData!!.itemCount
                            for (i in 0 until count) {
                                val imageUri: Uri = result.data?.clipData!!.getItemAt(i).uri
                                val file = getFileFromUri(mContext, imageUri, FileType.IMAGE)
                                if (file != null) {
                                    val media =
                                        Media.create(generateThumb(mContext, FileType.IMAGE, file))
                                    imgList.add(media)
                                }
                            }
                        } else if (result.data?.data != null) {
                            val imgUri: Uri = result.data?.data!!
                            val file = getFileFromUri(mContext, imgUri, FileType.IMAGE)
                            if (file != null) {
                                val media =
                                    Media.create(generateThumb(mContext, FileType.IMAGE, file))
                                imgList.add(media)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    sendCallBack(imgList)
                } else {
                    if (crop) {
                        cropImage(getFileFromUri(mContext, result.data?.data, FileType.IMAGE))
                    } else {
                        var media: Media? = null
                        showProgressBar(true)
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                var file =
                                    getFileFromUri(mContext, result.data?.data, FileType.IMAGE)
                                if (file != null) {
                                    file = getCompressedImage(file)
                                    media =
                                        Media.create(generateThumb(mContext, FileType.IMAGE, file))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                media = null
                            }
                        }.invokeOnCompletion { sendCallBack(media) }
                    }
                }
            } else {
                sendCancelCallBack()
            }
        }
    }

    // capture video using in-build camera
    private fun takeVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        val file = FileUtil.createNewFile(context, FileType.VIDEO)
        val uri: Uri = FileUtil.getURI(context, applicationId, file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        activityLauncher.launch(Intent.createChooser(intent, "Capture Using")) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val media: Media? = try {
                    Media.create(generateThumb(mContext, FileType.VIDEO, file))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                sendCallBack(media)
            } else {
                sendCancelCallBack()
            }
        }
    }

    // choose video from gallery
    private fun pickVideo() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")
        activityLauncher.launch(Intent.createChooser(intent, "Select Video")) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var media: Media? = null
                showProgressBar(true)
                try {
                    val file = getFileFromUri(mContext, result.data?.data, FileType.VIDEO)
                    if (file != null) {
                        media = Media.create(generateThumb(mContext, FileType.VIDEO, file))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    media = null
                }
                sendCallBack(media)
            } else {
                sendCancelCallBack()
            }
        }
    }

    // choose document file
    private fun pickDocument() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = documentType
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        activityLauncher.launch(Intent.createChooser(intent, "Pick File")) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var media: Media? = null
                showProgressBar(true)
                try {
                    if (result.data?.data != null) {
                        val file = if (documentWithOriginalName)
                            getFileFromUriWithOriginal(mContext, result.data?.data, DOCUMENT)
                        else getFileFromUri(mContext, result.data?.data, DOCUMENT)
                        if (file != null)
                            media = Media.create(generateThumb(DOCUMENT, file))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    media = null
                }
                sendCallBack(media)
            } else {
                sendCancelCallBack()
            }
        }
    }

    private suspend fun getCompressedImage(file: File): File {
        return imageCompress(
            mContext, file, FileType.IMAGE, quality, compressFormat, compressResolution
        )
    }

    private fun sendCallBack(media: Media?) {
        showProgressBar(false)
        executeOnMain {
            if (media == null) callback?.onError(null)
            else callback?.onSuccess(media)
        }
        hideBottomSheet()
    }

    private fun sendErrorCallBack() {
        showProgressBar(false)
        executeOnMain { callback?.onError(null) }
        hideBottomSheet()
    }

    private fun sendCallBack(mediaList: ArrayList<Media>?) {
        showProgressBar(false)
        executeOnMain {
            if (mediaList.isNullOrEmpty()) callback?.onError(null)
            else callback?.onSuccess(mediaList)
        }
        hideBottomSheet()
    }

    private fun sendCancelCallBack() {
        if (!directAction) return
        callback?.onError("User Cancelled")
        hideBottomSheet()
    }

    private fun cropImage(file: File?) {
        if (file == null) {
            sendErrorCallBack()
            return
        }
        var cropFile = File(
            getRootDirectory(context, FileType.IMAGE)
                    + file.nameWithoutExtension + UNDER_SCORE + "cropped." + file.extension
        )
        val options = UCrop.Options()
        options.setCompressionFormat(FileUtil.getCompressFormat(file.extension))

        val uCrop = UCrop.of(file.toUri(), Uri.fromFile(cropFile)).withOptions(options)
        if (cropX > 0 && cropY > 0) uCrop.withAspectRatio(cropX, cropY)
        if (maxCropWidth > 0 && maxCropHeight > 0)
            uCrop.withMaxResultSize(maxCropWidth, maxCropHeight)

        try {
            activityLauncher.launch(uCrop.getIntent(requireActivity())) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (file.exists()) file.delete()
                    var media: Media? = null
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            cropFile = getCompressedImage(cropFile)
                            media = Media.create(generateThumb(mContext, FileType.IMAGE, cropFile))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            media = null
                        }
                    }.invokeOnCompletion { sendCallBack(media) }
                } else sendCancelCallBack()
            }
        } catch (ex: ActivityNotFoundException) {
            println(
                "uCrop not specified in manifest file." +
                        "Add UCropActivity in Manifest" +
                        "<activity\n" +
                        "    android:name=\"com.yalantis.ucrop.UCropActivity\"\n" +
                        "    android:screenOrientation=\"portrait\"\n" +
                        "    android:theme=\"@style/Theme.AppCompat.Light.NoActionBar\"/>"
            )
            ex.printStackTrace()
        }
    }


    fun start(supportFragmentManager: FragmentManager) {
        show(supportFragmentManager, fileSelectionType.value)
    }

    /*
    * customize methods
    * */
    fun type(fileSelectionType: FileSelectionType): AndroidFilePicker {
        this.fileSelectionType = fileSelectionType
        return this
    }

    fun enableDirectAction(directActionType: FileSelectionType): AndroidFilePicker {
        this.directActionType = directActionType
        directAction = true
        return this
    }

    /**
     * Crop an image and let user set the aspect ratio.
     */
    fun enableCrop(): AndroidFilePicker {
        this.crop = true
        return this
    }

    fun crop(x: Float, y: Float): AndroidFilePicker {
        cropX = x
        cropY = y
        return enableCrop()
    }

    fun cropSquare(): AndroidFilePicker {
        return crop(1f, 1f)
    }

    fun maxCropResultSize(width: Int, height: Int): AndroidFilePicker {
        this.maxCropWidth = width
        this.maxCropHeight = height
        return this
    }

    fun callBack(callBack: FilePickerCallback): AndroidFilePicker {
        this.callback = callBack
        return this
    }

    fun compressQuality(quality: Int): AndroidFilePicker {
        this.quality = quality
        return this
    }

    fun compressedFormat(format: CompressFormat): AndroidFilePicker {
        this.compressFormat = format
        return this
    }

    fun setResolutionConstraint(width: Int, height: Int): AndroidFilePicker {
        compressResolution = ResolutionConstraint(width, height)
        return this
    }

    fun pickDocumentType(type: String): AndroidFilePicker {
        this.documentType = type
        return this
    }

    fun enableMultiSelection(): AndroidFilePicker {
        this.isMultiSelection = true
        return this
    }

    fun enableDocumentWithOriginalName(): AndroidFilePicker {
        this.documentWithOriginalName = true
        return this
    }

    fun updateTheme(
        @DrawableRes actionButtonBg: Int? = null,
        @ColorRes actionButtonTextColor: Int? = null,
        @DrawableRes cancelButtonBg: Int? = null,
        @ColorRes cancelButtonTextColor: Int? = null,
    ): AndroidFilePicker {
        this.actionButtonBg = actionButtonBg
        this.actionButtonTextColor = actionButtonTextColor
        this.cancelButtonBg = cancelButtonBg
        this.cancelButtonTextColor = cancelButtonTextColor
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        if (!isTablet()) return dialog

        dialog.setOnShowListener {
            val bottomDialog = it as BottomSheetDialog
            val bottomSheet =
                (bottomDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?)
                    ?: return@setOnShowListener
            val displayMetrics = requireActivity().resources.displayMetrics
            val height = displayMetrics.heightPixels
            val maxHeight = (height * 0.90).toInt()
            BottomSheetBehavior.from(bottomSheet).peekHeight = maxHeight
        }
        return dialog
    }

    companion object {
        private val permissions =
            if (VERSION.SDK_INT >= VERSION_CODES.Q)
                arrayOf(CAMERA, READ_EXTERNAL_STORAGE, ACCESS_MEDIA_LOCATION)
            else arrayOf(CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)

        private const val RQ_FILE_PERMISSION = 1001

        fun with(applicationId: String): AndroidFilePicker {
            return AndroidFilePicker(applicationId)
        }

        fun with(applicationId: String, @LayoutRes layoutRes: Int): AndroidFilePicker {
            val filePicker = with(applicationId)
            filePicker.customLayoutRes = layoutRes
            return filePicker
        }

        fun with(applicationId: String, fileSelectionType: FileSelectionType): AndroidFilePicker {
            val filePicker = with(applicationId)
            filePicker.fileSelectionType = fileSelectionType
            return filePicker
        }
    }
}