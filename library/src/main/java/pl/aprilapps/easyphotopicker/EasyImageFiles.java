package pl.aprilapps.easyphotopicker;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Jacek Kwiecień on 14.12.15.
 */
class EasyImageFiles {

    public static String DEFAULT_FOLDER_NAME = "EasyImage";
    public static String TEMP_FOLDER_NAME = "Temp";


    public static String getFolderName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(BundleKeys.FOLDER_NAME, DEFAULT_FOLDER_NAME);
    }

    public static File tempImageDirectory(Context context) {
        boolean publicTemp = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BundleKeys.PUBLIC_TEMP, false);
        File dir = publicTemp ? publicTemplDir(context) : privateTemplDir(context);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File publicRootDir(Context context) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }

//    public static File publicRootPicturesDir(Context context) {
//        File dir = new File(publicRootDir(context), getFolderName(context));
//        if (!dir.exists()) dir.mkdirs();
//        return dir;
//    }

    public static File publicAppExternalDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public static File publicTemplDir(Context context) {
        File cameraPicturesDir = new File(EasyImageFiles.getFolderLocation(context), EasyImageFiles.getFolderName(context));
        File publicTempDir = new File(cameraPicturesDir, TEMP_FOLDER_NAME);
        if (!publicTempDir.exists()) publicTempDir.mkdirs();
        return publicTempDir;
    }

    private static File privateTemplDir(Context context) {
        File privateTempDir = new File(context.getApplicationContext().getCacheDir(), getFolderName(context));
        if (!privateTempDir.exists()) privateTempDir.mkdirs();
        return privateTempDir;
    }

//    public static File publicAppExternalFilesDir(Context context) {
//        File dir = new File(publicAppExternalDir(context), getFolderName(context));
//        if (!dir.exists()) dir.mkdirs();
//        return dir;
//    }

    public static void writeToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File pickedExistingPicture(Context context, Uri photoUri) throws IOException {
        InputStream pictureInputStream = context.getContentResolver().openInputStream(photoUri);
        File directory = tempImageDirectory(context);
        File photoFile = new File(directory, UUID.randomUUID().toString());
        photoFile.createNewFile();
        writeToFile(pictureInputStream, photoFile);
        return photoFile;
    }

    /**
     * Default folder location will be inside app public directory. That way write permissions after SDK 18 aren't required and contents are deleted if app is uninstalled.
     *
     * @param context context
     */
    public static String getFolderLocation(Context context) {
        String defaultFolderLocation = publicAppExternalDir(context).getPath();
        return PreferenceManager.getDefaultSharedPreferences(context).getString(BundleKeys.FOLDER_LOCATION, defaultFolderLocation);
    }

    public static File getCameraPicturesLocation(Context context) throws IOException {
        File dir = new File(EasyImageFiles.getFolderLocation(context), EasyImageFiles.getFolderName(context));
        if (!dir.exists()) dir.mkdirs();
        File imageFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg", dir);
        return imageFile;
    }


}
