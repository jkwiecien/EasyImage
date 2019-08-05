package pl.aprilapps.easyphotopicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.fragment.app.Fragment
import java.io.IOException


class EasyImage private constructor(
        private val context: Context,
        private val chooserTitle: String,
        private val folderName: String,
        private val allowMultiple: Boolean,
        private val chooserType: ChooserType,
        private val copyImagesToPublicGalleryFolder: Boolean
) {

    private var lastCameraFile: MediaFile? = null

    interface Callbacks {
        fun onImagePickerError(error: Throwable, source: MediaSource)

        fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource)

        fun onCanceled(source: MediaSource)
    }

    private class ActivityCaller(
            val fragment: Fragment? = null,
            val activity: Activity? = null,
            val deprecatedFragment: android.app.Fragment? = null
    ) {
        val context: Context
            get() = (activity ?: fragment?.activity ?: deprecatedFragment?.activity)!!

        fun startActivityForResult(intent: Intent, chooser: Int) {
            activity?.startActivityForResult(intent, chooser)
                    ?: fragment?.startActivityForResult(intent, chooser)
                    ?: deprecatedFragment?.startActivityForResult(intent, chooser)
        }
    }

    private fun getCallerActivity(caller: Any): ActivityCaller? = when (caller) {
        is Activity -> ActivityCaller(activity = caller)
        is Fragment -> ActivityCaller(fragment = caller)
        is android.app.Fragment -> ActivityCaller(deprecatedFragment = caller)
        else -> null
    }

    private fun startChooser(caller: Any) {
        cleanup()
        getCallerActivity(caller)?.let { activityCaller ->
            try {
                lastCameraFile = Files.createCameraPictureFile(context)
                val intent = Intents.createChooserIntent(
                        context = activityCaller.context,
                        chooserTitle = chooserTitle,
                        chooserType = chooserType,
                        cameraFileUri = lastCameraFile!!.uri,
                        allowMultiple = allowMultiple
                )
                activityCaller.startActivityForResult(intent, RequestCodes.PICK_PICTURE_FROM_CHOOSER)
            } catch (error: IOException) {
                error.printStackTrace()
                cleanup()
            }
        }
    }

    private fun startDocuments(caller: Any) {
        cleanup()
        getCallerActivity(caller)?.let { activityCaller ->
            val intent = Intents.createDocumentsIntent()
            activityCaller.startActivityForResult(intent, RequestCodes.PICK_PICTURE_FROM_DOCUMENTS)
        }
    }

    private fun startGallery(caller: Any) {
        cleanup()
        getCallerActivity(caller)?.let { activityCaller ->
            val intent = Intents.createGalleryIntent(allowMultiple)
            activityCaller.startActivityForResult(intent, RequestCodes.PICK_PICTURE_FROM_GALLERY)
        }
    }

    private fun startCameraForImage(caller: Any) {
        cleanup()
        getCallerActivity(caller)?.let { activityCaller ->
            lastCameraFile = Files.createCameraPictureFile(context)
            val takePictureIntent = Intents.createCameraForImageIntent(activityCaller.context, lastCameraFile!!.uri)
            val capableComponent = takePictureIntent.resolveActivity(context.packageManager)
                    ?.also {
                        activityCaller.startActivityForResult(takePictureIntent, RequestCodes.TAKE_PICTURE)
                    }

            if (capableComponent == null) {
                Log.e(EASYIMAGE_LOG_TAG, "No app capable of handling camera intent")
                cleanup()
            }
        }
    }

    private fun startCameraForVideo(caller: Any) {
        cleanup()
        getCallerActivity(caller)?.let { activityCaller ->
            lastCameraFile = Files.createCameraVideoFile(context)
            val recordVideoIntent = Intents.createCameraForVideoIntent(activityCaller.context, lastCameraFile!!.uri)
            val capableComponent = recordVideoIntent.resolveActivity(context.packageManager)
                    ?.also {
                        activityCaller.startActivityForResult(recordVideoIntent, RequestCodes.CAPTURE_VIDEO)
                    }
            if (capableComponent == null) {
                Log.e(EASYIMAGE_LOG_TAG, "No app capable of handling camera intent")
                cleanup()
            }
        }
    }

    fun openChooser(activity: Activity) = startChooser(activity)
    fun openChooser(fragment: Fragment) = startChooser(fragment)
    fun openChooser(fragment: android.app.Fragment) = startChooser(fragment)
    fun openDocuments(activity: Activity) = startDocuments(activity)
    fun openDocuments(fragment: Fragment) = startDocuments(fragment)
    fun openDocuments(fragment: android.app.Fragment) = startDocuments(fragment)
    fun openGallery(activity: Activity) = startGallery(activity)
    fun openGallery(fragment: Fragment) = startGallery(fragment)
    fun openGallery(fragment: android.app.Fragment) = startGallery(fragment)
    fun openCameraForImage(activity: Activity) = startCameraForImage(activity)
    fun openCameraForImage(fragment: Fragment) = startCameraForImage(fragment)
    fun openCameraForImage(fragment: android.app.Fragment) = startCameraForImage(fragment)
    fun openCameraForVideo(activity: Activity) = startCameraForVideo(activity)
    fun openCameraForVideo(fragment: Fragment) = startCameraForVideo(fragment)
    fun openCameraForVideo(fragment: android.app.Fragment) = startCameraForVideo(fragment)

    fun handleActivityResult(requestCode: Int, resultCode: Int, resultIntent: Intent?, activity: Activity, callbacks: Callbacks) {
        // EasyImage request codes are set to be between 374961 and 374965.
        if (requestCode !in 34961..34965) return

        val mediaSource = when (requestCode) {
            RequestCodes.PICK_PICTURE_FROM_DOCUMENTS -> MediaSource.DOCUMENTS
            RequestCodes.PICK_PICTURE_FROM_GALLERY -> MediaSource.GALLERY
            RequestCodes.TAKE_PICTURE -> MediaSource.CAMERA_IMAGE
            RequestCodes.CAPTURE_VIDEO -> MediaSource.CAMERA_VIDEO
            else -> MediaSource.CHOOSER
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RequestCodes.PICK_PICTURE_FROM_DOCUMENTS && resultIntent != null) {
                onPickedExistingPicturesFromLocalStorage(resultIntent, activity, callbacks)
            } else if (requestCode == RequestCodes.PICK_PICTURE_FROM_GALLERY && resultIntent != null) {
                onPickedExistingPictures(resultIntent, activity, callbacks)
            } else if (requestCode == RequestCodes.PICK_PICTURE_FROM_CHOOSER) {
                onFileReturnedFromChooser(resultIntent, activity, callbacks)
            } else if (requestCode == RequestCodes.TAKE_PICTURE) {
                onPictureReturnedFromCamera(activity, callbacks)
            } else if (requestCode == RequestCodes.CAPTURE_VIDEO) {
                onVideoReturnedFromCamera(activity, callbacks)
            }
        } else {
            removeCameraFileAndCleanup()
            callbacks.onCanceled(mediaSource)
        }
    }

    private fun onPickedExistingPicturesFromLocalStorage(resultIntent: Intent, activity: Activity, callbacks: Callbacks) {
        Log.d(EASYIMAGE_LOG_TAG, "Existing picture returned from local storage")
        try {
            val uri = resultIntent.data!!
            val photoFile = Files.pickedExistingPicture(activity, uri)
            val mediaFile = MediaFile(uri, photoFile)
            callbacks.onMediaFilesPicked(arrayOf(mediaFile), MediaSource.DOCUMENTS)
        } catch (error: Throwable) {
            error.printStackTrace()
            callbacks.onImagePickerError(error, MediaSource.DOCUMENTS)
        }
        cleanup()
    }

    private fun onPickedExistingPictures(resultIntent: Intent, activity: Activity, callbacks: Callbacks) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                val clipData = resultIntent.clipData
                if (clipData != null) {
                    Log.d(EASYIMAGE_LOG_TAG, "Existing picture returned")
                    val files = mutableListOf<MediaFile>()
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val file = Files.pickedExistingPicture(activity, uri)
                        files.add(MediaFile(uri, file))
                    }
                    if (files.isNotEmpty()) {
                        callbacks.onMediaFilesPicked(files.toTypedArray(), MediaSource.GALLERY)
                    } else {
                        callbacks.onImagePickerError(EasyImageException("No files were returned from gallery"), MediaSource.GALLERY)
                    }
                    cleanup()
                } else {
                    onPickedExistingPicturesFromLocalStorage(resultIntent, activity, callbacks)
                }
            } else {
                onPickedExistingPicturesFromLocalStorage(resultIntent, activity, callbacks)
            }
        } catch (error: Throwable) {
            cleanup()
            error.printStackTrace()
            callbacks.onImagePickerError(error, MediaSource.GALLERY)
        }

    }

    private fun onPictureReturnedFromCamera(activity: Activity, callbacks: Callbacks) {
        Log.d(EASYIMAGE_LOG_TAG, "Picture returned from camera")
        lastCameraFile?.let { cameraFile ->
            try {
                if (cameraFile.uri.toString().isEmpty()) Intents.revokeWritePermission(activity, cameraFile.uri)
                val files = mutableListOf(cameraFile)
                if (copyImagesToPublicGalleryFolder) Files.copyFilesInSeparateThread(activity, folderName, files.map { it.file })
                callbacks.onMediaFilesPicked(files.toTypedArray(), MediaSource.CAMERA_IMAGE)
            } catch (error: Throwable) {
                error.printStackTrace()
                callbacks.onImagePickerError(EasyImageException("Unable to get the picture returned from camera.", error), MediaSource.CAMERA_IMAGE)
            }
        }
        cleanup()
    }

    private fun onVideoReturnedFromCamera(activity: Activity, callbacks: Callbacks) {
        Log.d(EASYIMAGE_LOG_TAG, "Video returned from camera")
        lastCameraFile?.let { cameraFile ->
            try {
                if (cameraFile.uri.toString().isEmpty()) Intents.revokeWritePermission(activity, cameraFile.uri)
                val files = mutableListOf(cameraFile)
                if (copyImagesToPublicGalleryFolder) Files.copyFilesInSeparateThread(activity, folderName, files.map { it.file })
                callbacks.onMediaFilesPicked(files.toTypedArray(), MediaSource.CAMERA_VIDEO)
            } catch (error: Throwable) {
                error.printStackTrace()
                callbacks.onImagePickerError(EasyImageException("Unable to get the picture returned from camera.", error), MediaSource.CAMERA_IMAGE)
            }
        }
        cleanup()
    }

    private fun onFileReturnedFromChooser(resultIntent: Intent?, activity: Activity, callbacks: Callbacks) {
        Log.d(EASYIMAGE_LOG_TAG, "File returned from chooser")
        if (resultIntent != null) {
            onPickedExistingPictures(resultIntent, activity, callbacks)
            removeCameraFileAndCleanup()
        } else if (lastCameraFile != null) {
            onPictureReturnedFromCamera(activity, callbacks)
        }
    }

    fun canDeviceHandleGallery(): Boolean {
        return Intents.plainGalleryPickerIntent().resolveActivity(context.packageManager) != null
    }

    private fun removeCameraFileAndCleanup() {
        lastCameraFile?.file?.let { file ->
            Log.d(EASYIMAGE_LOG_TAG, "Removing camera file of size: ${file.length()}")
            file.delete()
            Log.d(EASYIMAGE_LOG_TAG, "Clearing reference to camera file")
            lastCameraFile = null
        }
    }

    private fun cleanup() {
        lastCameraFile?.let { cameraFile ->
            Log.d(EASYIMAGE_LOG_TAG, "Clearing reference to camera file of size: ${cameraFile.file.length()}")
            lastCameraFile = null
        }
    }

    class Builder(private val context: Context) {
        companion object {
            private fun getAppName(context: Context): String = try {
                context.applicationInfo.loadLabel(context.packageManager).toString()
            } catch (error: Throwable) {
                Log.e(EASYIMAGE_LOG_TAG, "App name couldn't be found. Probably no label was specified in the AndroidManifest.xml. Using EasyImage as a folder name for files.")
                error.printStackTrace()
                "EasyImage"
            }
        }

        private var chooserTitle: String = ""
        private var folderName: String = getAppName(context)
        private var allowMultiple = false
        private var chooserType: ChooserType = ChooserType.CAMERA_AND_DOCUMENTS
        private var copyImagesToPublicGalleryFolder: Boolean = false

        fun setChooserTitle(chooserTitle: String): Builder {
            this.chooserTitle = chooserTitle
            return this
        }

        fun setFolderName(folderName: String): Builder {
            this.folderName = folderName
            return this
        }

        fun setChooserType(chooserType: ChooserType): Builder {
            this.chooserType = chooserType
            return this
        }

        fun allowMultiple(allowMultiple: Boolean): Builder {
            this.allowMultiple = allowMultiple
            return this
        }

        fun setCopyImagesToPublicGalleryFolder(copyImagesToPublicGalleryFolder: Boolean): Builder {
            this.copyImagesToPublicGalleryFolder = copyImagesToPublicGalleryFolder
            return this
        }

        fun build(): EasyImage {
            return EasyImage(
                    context = context,
                    chooserTitle = chooserTitle,
                    folderName = folderName,
                    chooserType = chooserType,
                    allowMultiple = allowMultiple,
                    copyImagesToPublicGalleryFolder = copyImagesToPublicGalleryFolder
            )
        }
    }
}