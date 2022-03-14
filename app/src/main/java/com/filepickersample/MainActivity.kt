package com.filepickersample

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.filepickersample.BuildConfig.APPLICATION_ID
import com.filepickersample.bottomsheet.AndroidFilePicker
import com.filepickersample.databinding.ActivityMainBinding
import com.filepickersample.enumeration.FileSelectionType
import com.filepickersample.listener.FilePickerCallback
import com.filepickersample.model.Media

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityMainBinding
    private var selectionType: FileSelectionType = FileSelectionType.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpSpinner()
    }

    private fun setUpSpinner() {
        val spinnerItems =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, FileSelectionType.values())
        spinnerItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spFileSelectionType.adapter = spinnerItems

        binding.spFileSelectionType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    long: Long
                ) {
                    selectionType = FileSelectionType.values()[position]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnOpenFilePicker -> {
                openFilePicker()
            }
        }
    }

    private fun openFilePicker() {
        AndroidFilePicker.with(APPLICATION_ID, R.layout.custom_file_picker_layout)
            .type(selectionType)
//            .enableDirectAction(CAPTURE_IMAGE)
            .enableCrop()
            .enableMultiSelection()
            .compressQuality(100)
            .pickDocumentType("application/pdf")
            .enableDocumentWithOriginalName()
//            .updateTheme(
//                R.drawable.button_bg, R.color.colorPrimary,
//                R.drawable.button_bg_primary, R.color.white
//            )
            .callBack(object : FilePickerCallback {
                override fun onSuccess(media: Media?) = handleSelectedMedia(media)

                override fun onSuccess(mediaList: ArrayList<Media>?) {
                    handleSelectedMedia(mediaList)
                }

                override fun onError(error: String?) {
                    val errorMessage = error ?: "Something Went Wrong"
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            })
            .start(supportFragmentManager)

    }

    private fun handleSelectedMedia(media: Media?) {
        if (media == null) return
        Glide
            .with(this@MainActivity)
            .load(media.url)
            .centerCrop()
            .into(binding.imgMedia)

        binding.txtMediaDetails.text =
            String.format(
                "Media type ->  ${media.fileType.name} \n\n" +
                        "Media file name -> ${media.filename} \n\n" +
                        "Media file path -> ${media.url}"
            )
    }

    private fun handleSelectedMedia(mediaList: ArrayList<Media>?) {
        if (mediaList.isNullOrEmpty()) return
        Glide
            .with(this@MainActivity)
            .load(mediaList[0].mediaFile)
            .centerCrop()
            .into(binding.imgMedia)

        binding.txtMediaDetails.text =
            String.format(
                "Media Size : ${mediaList.size}"
            )
    }
}