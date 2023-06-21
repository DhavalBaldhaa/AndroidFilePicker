<h1 align="center">AndroidFilePicker</h1>
<p align="center">
  <a href="https://jitpack.io/#DhavalBaldhaa/AndroidFilePicker"> <img src="https://jitpack.io/v/DhavalBaldhaa/AndroidFilePicker/month.svg" /></a>
  <a href="https://jitpack.io/#DhavalBaldhaa/AndroidFilePicker"> <img src="https://jitpack.io/v/DhavalBaldhaa/AndroidFilePicker.svg" /></a>
</p>

AndroidFilePicker is an android library that will help you to pick any type of media file in your
application. No need to manage any kind of extra permission or result method override. Just create
a library class instance and use it or also modify ui as your requirement.

# Features:
* Supported for Android 13
* Capture Camera Image
* Pick Gallery Image Single or Multiple
* Crop Image (Crop image based on provided aspect ratio or let user choose one)
* Compress Image (Compress image based on provided compress quality, resolution and format)
* Handle all runtime permission for camera
* Does not require storage permission to pick gallery image or capture new image.
* Capture Camera Video
* Pick Video from Gallery
* Pick Specific type of document user need to specify pick document type

# Preview
<img src="https://github.com/DhavalBaldhaa/AndroidFilePicker/blob/master/app/screenshots/img1.JPEG" alt="screenshot" width="200" height="400"> 

# Usage

1. Add the JitPack repository to your project-level build file

 ```groovy
    allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency to your app level build file

```groovy
dependencies {
	  implementation 'com.github.DhavalBaldhaa:AndroidFilePicker:Tag'
}
```

3. Add Provider in manifest file

```dtd

<application android:requestLegacyExternalStorage="true">
    <provider android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.provider" android:exported="false"
        android:grantUriPermissions="true">
        <meta-data android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_picker_provider_paths" />
    </provider>
</application>
```

4. Create AndroidFilePicker instance and show bottom-sheet file picker, Add callback method for
   retrieve result or error message

```kotlin
AndroidFilePicker.with(APPLICATION_ID)
    .type(selectionType)
    .enableDocumentWithOriginalName()
    .callBack(object : FilePickerCallback {
        override fun onSuccess(media: Media?) {
            handleSelectedMedia(media)
        }

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
```

# Customization

### Choose required file type for your file picker selection options (Optional)

* ALL
    - For all option capture image, pick image, capture video and pick video<br/>Default selected,
      if not passed anything file picker works with default value
* IMAGE
    - For capture image from camera and pick image from gallery
* VIDEO
    - For capture video from camera and pick video from gallery
* CAPTURE_IMAGE
    - For capture image from camera
* CAPTURE_VIDEO
    - Capture video from camera
* PICK_IMAGE
    - Select image from gallery
* PICK_VIDEO
    - Select video from gallery
* TAKE_IMAGE_VIDEO
    - Capture image and video from camera
* PICK_IMAGE_VIDEO
    - Select image and video from gallery
* PICK_DOCUMENT
    - Select any type of file as document format

```kotlin
AndroidFilePicker.with(APPLICATION_ID)
    .type(selectionType)
    .start(supportFragmentManager)
```

### Theme Customization

* Added required color in your color.xml file

```dtd

<color name="picker_color_bg">#F4F4F4</color>
<color name="picker_button_bg">#ffffff</color>
<color name="picker_text_color">#000000</color>
<color name="picker_cancel_button_bg">#000000</color>
<color name="picker_cancel_text_color">#ffffff</color>
```

* Override bottomsheet theme as per your requirement

```dtd

<style name="PickerBottomSheetDialog" parent="@style/Theme.Design.Light.BottomSheetDialog">
    <item name="android:windowIsFloating">false</item>
    <item name="bottomSheetStyle">@style/PickerBottomSheet</item>
    <item name="android:statusBarColor">@android:color/transparent</item>
    <item name="android:navigationBarColor">@color/colorPrimaryDark</item>
    <item name="android:windowLightNavigationBar" tools:targetApi="o_mr1">true</item>
</style>
```

* Using custom layout xml file (You must need to add all ids with same view, Check demo for more
  details)

```Kotlin
AndroidFilePicker.with(APPLICATION_ID, R.layout.custom_file_picker_layout)
    .start(supportFragmentManager)
```

* Specify background resource and text colors for action buttons

```Kotlin
AndroidFilePicker.with(APPLICATION_ID)
    .updateTheme(
        R.drawable.button_bg,
        R.color.colorPrimary,
        R.drawable.button_bg_primary,
        R.color.white
    )
    .start(supportFragmentManager)
```

## Methods for enable additional functionality

|  Method Name             |         Description           |
:-------------------------:|:-----------------------------:|
|  type(type: FileSelectionType)  | Specify file selection option |
|  enableDirectAction()    | For required direct selection option, Like if you want to direct open camera for capure image and not required to show filepicker ui |
|  enableCrop              | Enable Cropping functionality and let user choose aspect ratio, By Default crop is disable<br/>(You can visit uCrop lib for customize cropping theme)      |
|  crop(x: Float, y: Float)   | Enable Cropping functionality with fixed Aspect Ratio     |
|  cropSquare()   |  Crop square image, its same as crop(1f, 1f)     |
|  maxCropResultSize(width: Int, height: Int)   |  Final image resolution will be less than width x height      |
|  compressQuality(quality: Int)    |  Specify image compress quality. In 0 to 100 value, 100 means original image  |
|  compressedFormat(format: CompressFormat)   |  Set compressed format, like JPEG, PNG, WEBP    |
|  setResolutionConstraint(width: Int, height: Int)   |  Final compressed image resolution will be less than width x height       |
|  pickDocumentType(type: String)    | Specify selection document type, "\*/\*" default value  Like <br/> "application/pdf - Only allow to select PDF File <br/> "image\/* - Only allow to select image file as document      |
|  enableMultiSelection()     |  Required when you want to selected multiple image from gallery    |
|  enableDocumentWithOriginalName()  |  Required when you want to select document file with original file name, Default document will select with unique name     |

## Contribution
[![GitHub contributors](https://img.shields.io/github/contributors/DhavalBaldhaa/AndroidFilePicker.svg)](https://github.com/DhavalBaldhaa/AndroidFilePicker/graphs/contributors)

* Bug reports and pull requests are welcome.
* Make sure you use [square/java-code-styles](https://github.com/square/java-code-styles) to format your code.
