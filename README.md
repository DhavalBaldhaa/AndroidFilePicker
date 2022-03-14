<h1 align="center">MediaFilePicker</h1>
<p align="center">
  <a href="https://jitpack.io/#dhaval-baldha1812/mediafilepicker"> <img src="https://jitpack.io/v/dhaval-baldha1812/mediafilepicker/month.svg" /></a>
  <a href="https://jitpack.io/#dhaval-baldha1812/mediafilepicker"> <img src="https://jitpack.io/v/dhaval-baldha1812/mediafilepicker.svg" /></a>
</p>

MediaFilePicker is android library which will help you to pick any type
of media file in your application. No need to manage any kind of extra
permission or result method override. Just create library class instance
and use it or also medify ui as your requirement.

<img src="https://github.com/DhavalBaldhaa/MediaFilePicker/blob/master/app/screenshots/img1.png" alt="screenshot" width="200" height="400"> 

# Installation
Step 1. Add the JitPack repository to your build file
```
allprojects {
    repositories {
	...
	maven { url 'https://jitpack.io' }
}
```
Step 2. Add the dependency
```
dependencies {
    implementation 'com.github.DhavalBaldhaa:MediaFilePicker:release_version'
}
```

Step 3. Add Provider in manifest file
```
<application
...
android:requestLegacyExternalStorage="true">
    ...
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.provider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_picker_provider_paths" />
    </provider>
    ....
</application>
```

## Usage

To **initialize** the sdk class, Use below code and setListeners to
receive the callback.

```kotlin
val bottomSheetFilePicker = BottomSheetFilePicker(BuildConfig.APPLICATION_ID)

bottomSheetFilePicker.setMediaListenerCallback(BottomSheetFilePicker.TAKE_ALL /*file pick action*/, object : MediaPickerCallback {
    override fun onPickedSuccess(media: Media?) {
      /*use media object for get your file information like path, image url, thumb url*/
    }

    override fun onPickedError(error: String?) {
        /*handle file pick error*/
    }

    override fun showProgressBar(enable: Boolean) {
        /*show progressbar if you want*/
    }
})

/*show file picker dialog in bottom*/
bottomSheetFilePicker.show(supportFragmentManager, "take_all")
```

**Set Direct Action**
```
bottomSheetFilePicker.setAction(BottomSheetFilePicker.TAKE_PHOTO)
```

**UI Customization** Use this method for customize of default library ui

```kotlin
// change action button background using custom drawable file 
bottomSheetFilePicker.actionButtonBg = R.drawable.button_bg

// change cancel button background using custom drawable file 
bottomSheetFilePicker.cancelButtonBg = R.drawable.button_bg_filled

// change action button text color 
bottomSheetFilePicker.actionButtonTextColor = R.color.purple_500

// change cancel button text color
bottomSheetFilePicker.cancelButtonTextColor = R.color.white
```

**Set Custom theme**
You can set your custom theme and change your bottomsheet background
```
// Create theme
<style name="BaseBottomSheetDialog" parent="@style/Theme.Design.Light.BottomSheetDialog">
    <item name="android:windowIsFloating">false</item>
    <item name="bottomSheetStyle">@style/BottomSheet</item>
</style>

<style name="BottomSheet" parent="@style/Widget.Design.BottomSheet.Modal">
    <item name="android:background">@drawable/background</item>
</style>

// set your theme
val bottomSheetFilePicker = BottomSheetFilePicker(BuildConfig.APPLICATION_ID, R.style.BaseBottomSheetDialog)
```

## Contribution
[![GitHub contributors](https://img.shields.io/github/contributors/dhaval-baldha1812/MediaFilePicker.svg)](https://github.com/dhaval-baldha1812/MediaFilePicker/graphs/contributors)

* Bug reports and pull requests are welcome.
* Make sure you use [square/java-code-styles](https://github.com/square/java-code-styles) to format your code.
