package pl.aprilapps.easyphotopicker

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.*


object Files {

    private fun tempImageDirectory(context: Context): File {
        val privateTempDir = File(context.cacheDir, "EasyImage")
        if (!privateTempDir.exists()) privateTempDir.mkdirs()
        return privateTempDir
    }

    private fun generateFileName(): String {
        return "ei_${System.currentTimeMillis()}"
    }

    private fun writeToFile(inputStream: InputStream, file: File) {
        try {
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int = inputStream.read(buffer)
            while (length > 0) {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flipImage(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getFixedRotationBitmap(bitmapFile: File): Bitmap {
        val exifInterface = ExifInterface(bitmapFile.path)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return with(BitmapFactory.decodeFile(bitmapFile.path)) {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(this, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(this, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(this, 270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipImage(this, horizontal = true, vertical = false)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipImage(this, horizontal = false, vertical = true)
                else -> this
            }
        }
    }

    @RequiresApi(29)
    private fun copyImageToPublicGallery(context: Context, fileToCopy: File, folderName: String): String {
        val bitmapToCopy = getFixedRotationBitmap(fileToCopy)
        val contentResolver = context.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileToCopy.name)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$folderName")
        val copyUri: Uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
        val outputStream: OutputStream = contentResolver.openOutputStream(copyUri)!!
        bitmapToCopy.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        Log.d(EASYIMAGE_LOG_TAG, "Copied image to public gallery: ${copyUri.path}")
        return copyUri.path!!
    }

    private fun legacyCopyImageToPublicGallery(fileToCopy: File, folderName: String): String {
        val bitmapToCopy = getFixedRotationBitmap(fileToCopy)
        val legacyExternalStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName)
        if (!legacyExternalStorageDir.exists()) legacyExternalStorageDir.mkdirs()
        val copyFile = File(legacyExternalStorageDir, fileToCopy.name)
        copyFile.createNewFile()
        val outputStream = FileOutputStream(copyFile)
        bitmapToCopy.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        Log.d(EASYIMAGE_LOG_TAG, "Copied image to public gallery: ${copyFile.path}")
        return copyFile.path
    }


    internal fun copyImagesToPublicGallery(context: Context, folderName: String, filesToCopy: List<File>) {
        Thread {
            val copiedFilesPaths: List<String?> = filesToCopy.map { fileToCopy ->
                try {
                    if (Build.VERSION.SDK_INT >= 29) {
                        copyImageToPublicGallery(context, fileToCopy, folderName)
                    } else {
                        legacyCopyImageToPublicGallery(fileToCopy, folderName)
                    }
                } catch (error: Throwable) {
                    error.printStackTrace()
                    Log.e(EASYIMAGE_LOG_TAG, "File couldn't be copied to public gallery: ${fileToCopy.name}")
                    null
                }
            }
            runMediaScanner(context, copiedFilesPaths.filterNotNull())
        }.run()
    }

    private fun runMediaScanner(context: Context, paths: List<String>) {
        MediaScannerConnection.scanFile(context, paths.toTypedArray(), null) { path, uri ->
            Log.d(EASYIMAGE_LOG_TAG, "Scanned media with path: $path | uri: $uri")
        }
    }

    @Throws(IOException::class)
    internal fun pickedExistingPicture(context: Context, photoUri: Uri): File {
        val pictureInputStream = context.contentResolver.openInputStream(photoUri)
                ?: throw IOException("Could not open input stream for a file: $photoUri")

        val directory = tempImageDirectory(context)
        val photoFile = File(directory, generateFileName() + "." + getMimeType(context, photoUri))
        photoFile.createNewFile()
        writeToFile(pictureInputStream, photoFile)
        return photoFile
    }

    /**
     * To find out the extension of required object in given uri
     * Solution by http://stackoverflow.com/a/36514823/1171484
     */
    private fun getMimeType(context: Context, uri: Uri): String? {
        val extension: String?

        //Check uri format to avoid null
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            extension = mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }

        return extension
    }

    private fun getUriToFile(context: Context, file: File): Uri {
        val packageName = context.applicationContext.packageName
        val authority = "$packageName.easyphotopicker.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    @Throws(IOException::class)
    internal fun createCameraPictureFile(context: Context): MediaFile {
        val dir = tempImageDirectory(context)
        val file = File.createTempFile(generateFileName(), ".jpg", dir)
        val uri = getUriToFile(context, file)
        return MediaFile(uri, file)
    }

    @Throws(IOException::class)
    internal fun createCameraVideoFile(context: Context): MediaFile {
        val dir = tempImageDirectory(context)
        val file = File.createTempFile(generateFileName(), ".mp4", dir)
        val uri = getUriToFile(context, file)
        return MediaFile(uri, file)
    }
}