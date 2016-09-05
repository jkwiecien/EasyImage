## 1.4.0

Features:

  - Library now uses the `FileProvider` class to fully support the [changes introduced in Android N](https://developer.android.com/about/versions/nougat/android-7.0-changes.html#sharing-files), thus preventing he apps targeting API level 24 to throw `FileUriExposedException`.
    **Behavior change**: pictures taken from camera are now saved in either the external cache directory (if available) or the internal cache directory and it's no longer possible to customize the subfolder.