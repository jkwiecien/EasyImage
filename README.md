[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-EasyImage-green.svg?style=true)](https://android-arsenal.com/details/1/2725)
# What is it?
EasyImage allow you to eaisly take picture from gallery or camera without creating lots of boilerplate.
  
#How to use it?

##Taking straight to camera
- ```EasyImage.openCamera(Activity activity);```
- ```EasyImage.openCamera(Fragment fragment);```

##Taking straight to gallery
- ```EasyImage.openGalleryPicker(Activity activity);```
- ```EasyImage.openGalleryPicker(Fragment fragment);```

##Displaying system picker
- ```EasyImage.openChooser(Activity activity);```
- ```EasyImage.openChooser(Fragment fragment);```

Then there is only one thing left to do:

```java
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

##Runtime permissions
Library requires permission from your app. Declare it in your ```AndroidMnifest.xml```
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

**Please note** that for devices running API 23 (marshmallow) you have to request this permission in the runtime, beofre calling ```EasyImage.openCamera()```. It's demonstrated in the sample app.

**There is also one issue about runtime permissions**. According to the docs: 

``` if you app targets M and above and declares as using the CAMERA permission which is not granted, then atempting to use this action will result in a SecurityException.``` 

For this reason, if your app uses ```CAMERA``` permission, you should check it **aswell** as ```WRITE_EXTERNAL_STORAGE``` before calling ```EasyImage.openCamera()```

[This library](https://github.com/tajchert/Nammu) will help you manage runtime permissions.

##Gradle dependency
```groovy
repositories {
    maven { url "https://jitpack.io" }
}
    
dependencies {
    compile 'com.github.jkwiecien:EasyImage:1.0.8'
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
