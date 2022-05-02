package pl.aprilapps.easyphotopicker

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import pl.aprilapps.easyphotopicker.MimeType.AUDIO
import pl.aprilapps.easyphotopicker.MimeType.DOC
import pl.aprilapps.easyphotopicker.MimeType.DOCX
import pl.aprilapps.easyphotopicker.MimeType.IMAGE
import pl.aprilapps.easyphotopicker.MimeType.IMAGE_JPEG
import pl.aprilapps.easyphotopicker.MimeType.IMAGE_JPG
import pl.aprilapps.easyphotopicker.MimeType.IMAGE_PNG
import pl.aprilapps.easyphotopicker.MimeType.PDF
import pl.aprilapps.easyphotopicker.MimeType.TEXT
import pl.aprilapps.easyphotopicker.MimeType.XLS
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

    internal fun createDocumentsIntent(allowMultiple: Boolean, vararg types: String): Intent {
        val intent =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) Intent(Intent.ACTION_GET_CONTENT) else Intent(
                Intent.ACTION_OPEN_DOCUMENT
            )
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        if (types.isNotEmpty()) {
            intent.type = "*/*"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, types)
            }
        } else {
            intent.type = "image/*"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        }
        return intent
    }

    internal fun createGalleryIntent(allowMultiple: Boolean): Intent {
        val intent = plainGalleryPickerIntent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
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

    @SuppressLint("QueryPermissionsNeeded")
    @Throws(IOException::class)
    internal fun createChooserIntent(
        context: Context,
        chooserTitle: String,
        chooserType: ChooserType,
        cameraFileUri: Uri,
        allowMultiple: Boolean,
        supportedFileFormats: Array<String>
    ): Intent {
        val targetIntents = ArrayList<Intent>()
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = context.packageManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val camList = packageManager.queryIntentActivities(captureIntent, 0)
            for (resolveInfo in camList) {
                val packageName = resolveInfo.activityInfo.packageName
                val intent = Intent(captureIntent)
                intent.component = ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                )
                intent.setPackage(packageName)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri)

                //We have to explicitly grant the write permission since Intent.setFlag works only on API Level >=20
                grantWritePermission(context, intent, cameraFileUri)

                targetIntents.add(intent)
            }
        } else {
            targetIntents.add(captureIntent)
        }

        var chooserIntent: Intent? = null
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val storageIntent = when (chooserType) {
                ChooserType.CAMERA_AND_GALLERY -> createGalleryIntent(allowMultiple)
                ChooserType.CAMERA_AND_DOCUMENTS -> createDocumentsIntent(allowMultiple, *supportedFileFormats)
                ChooserType.CAMERA_AND_GALLERY_AND_DOCUMENTS -> {
                    targetIntents.add(createGalleryIntent(allowMultiple))
                    createDocumentsIntent(allowMultiple, *supportedFileFormats)
                }
            }

            chooserIntent =  Intent.createChooser(storageIntent, chooserTitle)
        }else{
            when(chooserType){
                ChooserType.CAMERA_AND_GALLERY -> targetIntents.add(createGalleryIntent(allowMultiple))
                ChooserType.CAMERA_AND_DOCUMENTS -> targetIntents.add(createDocumentsIntent(allowMultiple, *supportedFileFormats))
                ChooserType.CAMERA_AND_GALLERY_AND_DOCUMENTS -> {
                    targetIntents.add(createGalleryIntent(allowMultiple))
                    targetIntents.add(createDocumentsIntent(allowMultiple, *supportedFileFormats))
                }
            }
            chooserIntent = Intent.createChooser(Intent(),chooserTitle)
        }

        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            targetIntents.toTypedArray<Parcelable>()
        )
        return chooserIntent
    }

    internal fun isTherePhotoTakenWithCameraInsideIntent(dataIntent: Intent): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dataIntent.data == null && dataIntent.clipData == null
        } else {
            dataIntent.data == null
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun getSupportedFileFormats(types: Array<String>): Array<String> {
        val supportedFormats = ArrayList<String>()
        types.forEach { format ->
            when (format.lowercase()) {
                "pdf" -> supportedFormats.add(PDF)
                "jpeg" -> supportedFormats.add(IMAGE_JPEG)
                "jpg" -> supportedFormats.add(IMAGE_JPG)
                "png" -> supportedFormats.add(IMAGE_PNG)
                "image" -> supportedFormats.add(IMAGE)
                "doc" -> supportedFormats.add(DOC)
                "docx" -> supportedFormats.add(DOCX)
                "audio" -> supportedFormats.add(AUDIO)
                "text" -> supportedFormats.add(TEXT)
                "XLS" -> supportedFormats.add(XLS)
            }
        }
        return supportedFormats.toTypedArray()
    }
}