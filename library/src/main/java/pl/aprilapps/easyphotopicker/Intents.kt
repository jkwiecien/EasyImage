package pl.aprilapps.easyphotopicker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import java.io.IOException
import java.util.*

internal object Intents {

    internal fun revokeWritePermission(context: Context, uri: Uri) {
        context.revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun grantWritePermission(context: Context, intent: Intent, uri: Uri) {
        val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    internal fun plainGalleryPickerIntent(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    internal fun createDocumentsIntent(allowMultiple: Boolean): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        intent.type = "image/*"
        return intent
    }

    internal fun createGalleryIntent(allowMultiple: Boolean): Intent {
        val intent = plainGalleryPickerIntent()
        if (Build.VERSION.SDK_INT >= 18) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        return intent
    }

    internal fun createCameraForImageIntent(context: Context, fileUri: Uri): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            //We have to explicitly grant the write permission since Intent.setFlag works only on API Level >=20
            grantWritePermission(context, intent, fileUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return intent
    }

    internal fun createCameraForVideoIntent(context: Context, fileUri: Uri): Intent {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        try {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            //We have to explicitly grant the write permission since Intent.setFlag works only on API Level >=20
            grantWritePermission(context, intent, fileUri)
        } catch (error: Throwable) {
            error.printStackTrace()
        }
        return intent
    }

    @Throws(IOException::class)
    internal fun createChooserIntent(context: Context, chooserTitle: String, chooserType: ChooserType, cameraFileUri: Uri, allowMultiple: Boolean): Intent {
        val cameraIntents = ArrayList<Intent>()
        val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = context.packageManager
        val camList = packageManager.queryIntentActivities(captureIntent, 0)
        for (resolveInfo in camList) {
            val packageName = resolveInfo.activityInfo.packageName
            val intent = Intent(captureIntent)
            intent.component = ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
            intent.setPackage(packageName)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri)

            //We have to explicitly grant the write permission since Intent.setFlag works only on API Level >=20
            grantWritePermission(context, intent, cameraFileUri)

            cameraIntents.add(intent)
        }

        val storageIntent = when (chooserType) {
            ChooserType.CAMERA_AND_GALLERY -> createGalleryIntent(allowMultiple)
            else -> createDocumentsIntent(allowMultiple)
        }

        val chooserIntent = Intent.createChooser(storageIntent, chooserTitle)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray<Parcelable>())

        return chooserIntent
    }

    internal fun isTherePhotoTakenWithCameraInsideIntent(dataIntent: Intent): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dataIntent.data == null && dataIntent.clipData == null
        } else {
            dataIntent.data == null
        }
    }
}