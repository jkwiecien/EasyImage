# What is it?
EasyPicker is an extended version of easy image library which allows you to easily capture images and videos from the gallery, camera or get files from documents based on specified file formats without creating lots of boilerplate.

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

For this reason, if your app uses `CAMERA` permission, you should check it along **with** `WRITE_EXTERNAL_STORAGE` before calling `EasyPicker.openCamera()`
<!-- 
## Gradle dependency
Get the latest version from jitpack

[![](https://jitpack.io/v/jkwiecien/EasyImage.svg)](https://jitpack.io/#jkwiecien/EasyImage) -->

# Usage
## Essentials

Create your EasyPickerInstance like this:
```java
EasyPicker easypicker = new EasyPicker.Builder(context)

// Chooser only
// Will appear as a system chooser title, DEFAULT empty string
//.setChooserTitle("Pick media")
// Will tell chooser that it should show documents or gallery or all three of them
//.setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)
//.setChooserType(ChooserType.CAMERA_AND_GALLERY)
//.setChooserType(ChooserType.CAMERA_AND_GALLERY_AND_DOCUMENTS)
// saving EasyImage state (as for now: last camera file link)
.setMemento(memento)

// Setting to true will cause taken pictures to show up in the device gallery, DEFAULT false
.setCopyImagesToPublicGalleryFolder(false)
// Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
.setFolderName("EasyImage sample")

// Allow multiple picking
.allowMultiple(true)

// You can also specify the file formats you need to show in documents
//.addSupportedFileFormats(new String[]{"pdf","image"})
.build();
```

### Taking image straight from camera
- `easyPicker.openCameraForImage(Activity activity,);`
- `easyPicker.openCameraForImage(Fragment fragment);`

### Capturing video
- `easyPicker.openCameraForVideo(Activity activity);`
- `easyPicker.openCameraForVideo(Fragment fragment);`

### Taking image from gallery or the gallery picker if there is more than 1 gallery app
- `easyPicker.openGallery(Activity activity);`
- `easyPicker.openGallery(Fragment fragment);`

### Taking image from documents
- `easyPicker.openDocuments(Activity activity);`
- `easyPicker.openDocuments(Fragment fragment);`

### Displaying system picker to chose from camera, documents, or gallery if no documents apps are available
- `easyPicker.openChooser(Activity activity);`
- `easyPicker.openChooser(Fragment fragment);`

### Getting the photo file

```java
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyPicker.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] mediaFiles, MediaSource source) {
                onPhotosReturned(mediaFiles);
            }

            @Override
            public void onPickerError(@NonNull Throwable error, @NonNull MediaSource source) {
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
