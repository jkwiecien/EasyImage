package pl.aprilapps.easyphotopicker;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Jacek Kwiecień on 22.12.2016.
 */

public class Configuration {
    private Context context;

    Configuration(Context context) {
        this.context = context;
    }

    public Configuration setImagesFolderName(String folderName) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(BundleKeys.FOLDER_NAME, folderName)
                .commit();
        return this;
    }

    public Configuration saveTakenPhotosInPublicPicturesDirectory() {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(BundleKeys.FOLDER_LOCATION, EasyImageFiles.getPublicPicturesDirectory().toString())
                .commit();
        return this;
    }

    public Configuration saveInAppExternalFilesDir() {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(BundleKeys.FOLDER_LOCATION, EasyImageFiles.publicAppExternalDir(context).toString())
                .commit();
        return this;
    }

    public Configuration setAllowMultiplePickInGallery(boolean allowMultiple) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(BundleKeys.ALLOW_MULTIPLE, allowMultiple)
                .commit();
        return this;
    }

    public boolean allowsMultiplePickingInGallery() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BundleKeys.ALLOW_MULTIPLE, false);
    }

    /**
     * Use this method if you want your picked gallery or documents pictures to be duplicated into public, other apps accessible, directory.
     * You'll have to take care of removing that file on your own after you're done with it. Use EasyImage.clearPublicTemp() method for that.
     * If you don't delete them they could show up in user galleries.
     *
     * @return modified Configuration object
     */
    public Configuration setCopyExistingPicturesToPublicLocation(boolean copyToPublicLocation) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(BundleKeys.PUBLIC_TEMP, copyToPublicLocation)
                .commit();
        return this;
    }
}
