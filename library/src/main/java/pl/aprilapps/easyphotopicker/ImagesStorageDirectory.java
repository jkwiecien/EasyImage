package pl.aprilapps.easyphotopicker;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Jacek Kwiecień on 23.12.2016.
 */

public enum ImagesStorageDirectory {
    CACHE, PUBLIC_GALLERY;

    public File getRootDir(Context context) {
        switch (this) {
            case PUBLIC_GALLERY:
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            case CACHE:
                return context.getCacheDir();
            default:
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }
    }
}
