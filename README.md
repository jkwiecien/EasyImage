[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-EasyImage-green.svg?style=true)](https://android-arsenal.com/details/1/2725) [![](https://jitpack.io/v/jkwiecien/EasyImage.svg)](https://jitpack.io/#jkwiecien/EasyImage)
# What is it?
EasyImage allows you to easily capture images and videos from the gallery, camera or documents without creating lots of boilerplate.

# Setup

## Runtime permissions
No additional permisions are required if you DO NOT `use setCopyImagesToPublicGalleryFolder()` setting. But if you do:

### For devices running Android 10 and newer:
Nothing is required

### For devices running Android 9 or lower:
Permission need to be specified in Manifest:
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Also you'll need to ask for this permission in the runtime in the moment of your choice. Sample app does that.

**There is also one issue about runtime permissions**. According to the docs: 

    If your app targets M and above and declares as using the CAMERA permission which is not granted, then attempting to use this action will result in a SecurityException.

For this reason, if your app uses `CAMERA` permission, you should check it along **with** `WRITE_EXTERNAL_STORAGE` before calling `EasyImage.openCamera()`

## Gradle dependency
Get the latest version from jitpack

[![](https://jitpack.io/v/jkwiecien/EasyImage.svg)](https://jitpack.io/#jkwiecien/EasyImage)

# Usage
## Essentials

Create your EasyImageInstance like this:
```java
EasyImage easyImage = new EasyImage.Builder(context)

// Chooser only
// Will appear as a system chooser title, DEFAULT empty string
//.setChooserTitle("Pick media")
// Will tell chooser that it should show documents or gallery apps
//.setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)  you can use this or the one below
//.setChooserType(ChooserType.CAMERA_AND_GALLERY)
// saving EasyImage state (as for now: last camera file link)
.setMemento(memento)

// Setting to true will cause taken pictures to show up in the device gallery, DEFAULT false
.setCopyImagesToPublicGalleryFolder(false)
// Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
.setFolderName("EasyImage sample")

// Allow multiple picking
.allowMultiple(true)
.build();
```

### Taking image straight from camera
- `easyImage.openCameraForImage(Activity activity,);`
- `easyImage.openCameraForImage(Fragment fragment);`

### Capturing video
- `easyImage.openCameraForVideo(Activity activity);`
- `easyImage.openCameraForVideo(Fragment fragment);`

### Taking image from gallery or the gallery picker if there is more than 1 gallery app
- `easyImage.openGallery(Activity activity);`
- `easyImage.openGallery(Fragment fragment);`

### Taking image from documents
- `easyImage.openDocuments(Activity activity);`
- `easyImage.openDocuments(Fragment fragment);`

### Displaying system picker to chose from camera, documents, or gallery if no documents apps are available
- `easyImage.openChooser(Activity activity);`
- `easyImage.openChooser(Fragment fragment);`

### Getting the photo file

```java
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });
    }
```

# Known issues
Library was pretty much rewritten from scratch in kotlin on 29.03.2019. Initial version 3.0.0 might be unstable. In case of problems fallback to version 2.1.1
Also version 3.0.0 is not backward compatible and will require some changes of those who used previous versions. These are not big tho. Updated readme explains it all.

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
