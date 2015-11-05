package pl.aprilapps.easyphotopicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;


/**
 * Created by Jacek Kwiecień on 16.10.2015.
 */
public class EasyImage implements EasyImageConfig {

    public enum ImageSource {
        GALLERY, CAMERA
    }

    public interface Callbacks {
        void onImagePickerError(Exception e, ImageSource source);

        void onImagePicked(File imageFile, ImageSource source);

        void onCanceled(ImageSource source);
    }

    private static final String KEY_PHOTO_URI = "pl.aprilapps.easyphotopicker.photo_uri";

    private static File tempImageDirectory(Context context) {
        File dir = new File(context.getApplicationContext().getCacheDir(), "Images");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static File publicImageDirectory() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Images");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static void openGalleryPicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openCamera(Activity activity) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }

        try {
            File image = File.createTempFile(UUID.randomUUID().toString(), ".jpg", publicImageDirectory());
            Uri capturedImageUri = Uri.fromFile(image);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
            activity.startActivityForResult(intent, REQ_TAKE_PICTURE);
            PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(KEY_PHOTO_URI, capturedImageUri.toString()).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File pickedGalleryPicture(Context context, Uri photoPath) throws IOException {
        InputStream pictureInputStream = context.getContentResolver().openInputStream(photoPath);
        File directory = EasyImage.tempImageDirectory(context);
        File photoFile = new File(directory, UUID.randomUUID().toString());
        photoFile.createNewFile();
        EasyImage.writeToFile(pictureInputStream, photoFile);
        return photoFile;

    }

    private static File takenCameraPicture(Context context) throws IOException, URISyntaxException {
        URI imageUri = new URI(PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PHOTO_URI, null));
        notifyGallery(context, imageUri);
        return new File(imageUri);
    }

    private static void writeToFile(InputStream in, File file) {
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

    private static void notifyGallery(Context context, URI pictureUri) throws URISyntaxException {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pictureUri);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static void handleActivityResult(int requestCode, int resultCode, Intent data, Activity activity, Callbacks callbacks) {
        if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri photoPath = data.getData();
                try {
                    File photoFile = EasyImage.pickedGalleryPicture(activity, photoPath);
                    callbacks.onImagePicked(photoFile, ImageSource.GALLERY);
                } catch (Exception e) {
                    e.printStackTrace();
                    callbacks.onImagePickerError(e, ImageSource.GALLERY);
                }
            } else {
                callbacks.onCanceled(ImageSource.GALLERY);
            }
        } else if (requestCode == EasyImageConfig.REQ_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    File photoFile = EasyImage.takenCameraPicture(activity);
                    callbacks.onImagePicked(photoFile, ImageSource.CAMERA);
                } catch (Exception e) {
                    callbacks.onImagePickerError(e, ImageSource.CAMERA);
                }
            } else {
                callbacks.onCanceled(ImageSource.CAMERA);
            }
        }
    }

}
