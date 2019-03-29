package pl.aprilapps.easyphotopicker

import android.content.ContentResolver
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


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

    @Throws(IOException::class)
    private fun copyFile(src: File, dst: File) {
        val inputStream = FileInputStream(src)
        writeToFile(inputStream, dst)
    }

    internal fun copyFilesInSeparateThread(context: Context, folderName: String, filesToCopy: List<File>) {
        Thread(Runnable {
            val copiedFiles = ArrayList<File>()
            var i = 1
            for (fileToCopy in filesToCopy) {
                val dstDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName)
                if (!dstDir.exists()) dstDir.mkdirs()

                val filenameSplit = fileToCopy.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val extension = "." + filenameSplit[filenameSplit.size - 1]
                val datePart = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().time)
                val filename = "IMG_${datePart}_$i.$extension%d.%s"
                val dstFile = File(dstDir, filename)
                try {
                    dstFile.createNewFile()
                    copyFile(fileToCopy, dstFile)
                    copiedFiles.add(dstFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                i++
            }
            scanCopiedImages(context, copiedFiles)
        }).run()
    }

    private fun scanCopiedImages(context: Context, copiedImages: List<File>) {
        val paths = arrayOfNulls<String>(copiedImages.size)
        for (i in copiedImages.indices) {
            paths[i] = copiedImages[i].toString()
        }

        MediaScannerConnection.scanFile(context,
                paths, null,
                object : MediaScannerConnection.OnScanCompletedListener {
                    override fun onScanCompleted(path: String, uri: Uri) {
                        Log.d(javaClass.simpleName, "Scanned $path:")
                        Log.d(javaClass.simpleName, "-> uri=$uri")
                    }
                })
    }

    @Throws(IOException::class)
    internal fun pickedExistingPicture(context: Context, photoUri: Uri): File {
        val pictureInputStream = context.contentResolver.openInputStream(photoUri)
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