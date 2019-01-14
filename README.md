[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-EasyImage-green.svg?style=true)](https://android-arsenal.com/details/1/2725) [![](https://jitpack.io/v/jkwiecien/EasyImage.svg)](https://jitpack.io/#jkwiecien/EasyImage)
# What is it?
EasyImage allows you to easily capture images from the gallery, camera or documents without creating lots of boilerplate.

# Setup

## Runtime permissions
This library requires specific runtime permissions. Declare it in your `AndroidMnifest.xml`:
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

**Please note**: for devices running API 23 (marshmallow) you have to request this permission in the runtime, before calling `EasyImage.openCamera()`. It's demonstrated in the sample app.

**There is also one issue about runtime permissions**. According to the docs: 

    If your app targets M and above and declares as using the CAMERA permission which is not granted, then attempting to use this action will result in a SecurityException.

For this reason, if your app uses `CAMERA` permission, you should check it along **with** `WRITE_EXTERNAL_STORAGE` before calling `EasyImage.openCamera()`

[This library](https://github.com/tajchert/Nammu) will help you manage runtime permissions.

## Gradle dependency
Get the latest version from jitpack

[![](https://jitpack.io/v/jkwiecien/EasyImage.svg)](https://jitpack.io/#jkwiecien/EasyImage)

Please keep in mind that support for SDK 14 & 15 ended on version 1.3.1. If you have to support one of those, use that version of the library:

```
dependencies {
    implementation 'com.github.jkwiecien:EasyImage:1.3.1'
}
```

# Usage
## Essentials
### Taking image straight from camera
- `EasyImage.openCamera(Activity activity, int type);`
- `EasyImage.openCamera(Fragment fragment, int type);`

### Taking image from gallery or the gallery picker if there is more than 1 gallery app
- `EasyImage.openGallery(Activity activity, int typee);`
- `EasyImage.openGallery(Fragment fragment, int type);`

### Taking image from documents
- `EasyImage.openDocuments(Activity activity, int type);`
- `EasyImage.openDocuments(Fragment fragment, int type);`

### Displaying system picker to chose from camera, documents, or gallery if no documents apps are available
- `EasyImage.openChooserWithDocuments(Activity activity, String chooserTitle, int type);`
- `EasyImage.openChooserWithDocuments(Fragment fragment, String chooserTitle, int type);`

### Displaying system picker to chose from camera or gallery app
- `EasyImage.openChooserWithGallery(Activity activity, String chooserTitle, int type);`
- `EasyImage.openChooserWithGallery(Fragment fragment, String chooserTitle, int type);`

The `type` parameter is there only if you want to return different kind of images on the same screen. Otherwise, pass any `int` like `0`.

### Getting the photo file

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
        @Override
        public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
            //Some error handling
        }

        @Override
        public void onImagesPicked(List<File> imagesFiles, EasyImage.ImageSource source, int type) {
            //Handle the images
            onPhotosReturned(imagesFiles);
        }
    });
}
```
## Additional features
### Removing canceled but captured photo
If the user takes photo using camera, but then cancels, you might wanna remove that photo from the device.
Sample app present's the usage:
```java
  @Override
  public void onCanceled(EasyImage.ImageSource source, int type) {
      // Cancel handling, you might wanna remove taken photo if it was canceled
      if (source == EasyImage.ImageSource.CAMERA) {
          File photoFile = EasyImage.lastlyTakenButCanceledPhoto(MainActivity.this);
          if (photoFile != null) photoFile.delete();
      }
  }
  ```
## Additional configuration
```java
  EasyImage.configuration(this)
          .setImagesFolderName("My app images") // images folder name, default is "EasyImage"
          .saveInAppExternalFilesDir() // if you want to use root internal memory for storying images
          .saveInRootPicturesDirectory(); // if you want to use internal memory for storying images - default
	  .setAllowMultiplePickInGallery(true) // allows multiple picking in galleries that handle it. Also only for phones with API 18+ but it won't crash lower APIs. False by default
```
Configuration is persisted by default, so if you wish to clear it before the next use call `EasyImage.clearConfiguration(Context context);`

# License

    Copyright 2015 Jacek Kwiecień.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
