package com.filepickersample.bottomsheet

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.filepickersample.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

/**
 * Created by Dhaval Baldha on 21/12/2020.
 */

open class BaseFilePicker : BottomSheetDialogFragment(), OnShowListener {
    lateinit var mContext: Context
    private var expanded = false
    private var showKeyboard = false
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    @DrawableRes
    var themeId: Int? = null

    private val bottomSheetCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, themeId ?: R.style.PickerBottomSheetDialog)
        val dialog = dialog ?: return
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window
        if (window != null) {
            if (showKeyboard) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        return setExpanded(dialog)!!
    }

    open fun setExpanded(expanded: Boolean) {
        this.expanded = expanded
    }

    fun setKeyboard(isVisible: Boolean) {
        showKeyboard = isVisible
    }

    private fun setExpanded(dialog: Dialog?): Dialog? {
        if (dialog == null) return null
        val window = dialog.window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setDimAmount(0.25f)
            if (expanded) {
//                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
        dialog.setOnShowListener(this)
        return dialog
    }

    fun hideBottomSheet() {
        try {
            if (bottomSheetBehavior == null) return
            dismiss()
        } catch (e: Exception) {
        }
    }

    override fun onShow(dialog: DialogInterface) {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet =
            bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior?.addBottomSheetCallback(bottomSheetCallback)
            val layoutParams = bottomSheet.layoutParams
            if (!expanded) return
            bottomSheetBehavior?.peekHeight = Resources.getSystem().displayMetrics.heightPixels
            if (layoutParams != null) layoutParams.height = getWindowHeight()
            bottomSheet.layoutParams = layoutParams
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    protected fun isTablet(): Boolean {
        val xlarge =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == 4
        val large =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge || large
    }

    companion object {
        fun checkOnMainThread(): Boolean {
            return Looper.getMainLooper().thread == Thread.currentThread()
        }

        fun executeOnMain(runnable: Runnable) {
            Handler(Looper.getMainLooper()).post(runnable)
        }

        fun executeInBackground(runnable: Runnable?) {
            Thread(runnable).start()
        }

        fun executeDelay(runnable: () -> Unit, delay: Long) {
            Handler(Looper.getMainLooper()).postDelayed(runnable, delay)
        }

        fun requestPermissions(
            fragment: Fragment?,
            rationalMessage: String?,
            request_code: Int,
            perms: Array<String>
        ) {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(fragment!!, request_code, *perms)
                    .setRationale(rationalMessage)
//            .setTheme(R.style.AlertDialog)
                    .build()
            )
        }

        fun requestPermissions(
            activity: Activity,
            rationalMessage: String?,
            request_code: Int,
            perms: Array<String>
        ) {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(activity, request_code, *perms)
                    .setRationale(rationalMessage)
//            .setTheme(R.style.AlertDialog)
                    .build()
            )
        }
    }
}