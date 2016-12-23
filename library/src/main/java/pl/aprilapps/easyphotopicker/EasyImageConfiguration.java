package pl.aprilapps.easyphotopicker;

import android.content.Context;
import android.preference.PreferenceManager;

import java.io.File;

/**
 * Created by Jacek Kwiecień on 22.12.2016.
 */

public class EasyImageConfiguration implements Constants {

    private Context context;

    EasyImageConfiguration(Context context) {
        this.context = context;
    }

    public EasyImageConfiguration setImagesFolderName(String folderName) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(BundleKeys.FOLDER_NAME, folderName)
                .commit();
        return this;
    }

    public EasyImageConfiguration setImagesStorageDirctory(ImagesStorageDirectory storage) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(BundleKeys.STORAGE_DIRECTORY, storage.toString())
                .commit();
        return this;
    }

    public EasyImageConfiguration setAllowMultiplePickInGallery(boolean allowMultiple) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(BundleKeys.ALLOW_MULTIPLE, allowMultiple)
                .commit();
        return this;
    }

    public String getFolderName() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(BundleKeys.FOLDER_NAME, DEFAULT_FOLDER_NAME);
    }

    public boolean allowsMultiplePickingInGallery() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BundleKeys.ALLOW_MULTIPLE, false);
    }

    public File getImagesStorageDirectory() {
        String savedPref = PreferenceManager.getDefaultSharedPreferences(context).getString(BundleKeys.STORAGE_DIRECTORY, ImagesStorageDirectory.CACHE.toString());
        ImagesStorageDirectory root = ImagesStorageDirectory.valueOf(savedPref);
        File rootDir = root.getRootDir(context);
        File appDir = new File(rootDir, getFolderName());
        if (!appDir.exists()) rootDir.mkdirs();
        return appDir;
    }

}
