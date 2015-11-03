# What is it?
EasyImage allow you to eaisly take picture from gallery or camera without creating lots of boilerplate.
  
#How to use it?

Here are buttons click listeners for picking picture from gallery or taking with camera:

```
    @OnClick(R.id.camera_button)
    protected void onTakePhotoClicked() {
        EasyImage.openCamera(this);
    }

    @OnClick(R.id.gallery_button)
    protected void onPickFromGaleryClicked() {
        EasyImage.openGalleryPicker(this);
    }
```

Now there is only one thinh left to do:

```
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                //Handle the image
                onPhotoReturned(imageFile);
            }
        });
    }
```


#Setup

Library requires permissions from your app:
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```

Disclaimer: I'm planning to handle runtime permissions as soon I get hands on the Marshmallow device.

```
repositories {
    maven { url "https://jitpack.io" }
}
    
dependencies {
    compile 'com.github.jkwiecien:EasyImage:1.0.0'
}
```


License
=======

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
