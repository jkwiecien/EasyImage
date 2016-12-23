package pl.aprilapps.easyphotopicker;

/**
 * Created by Jacek Kwiecień on 03.11.2015.
 */
public interface Constants {

    String DEFAULT_FOLDER_NAME = "EasyImage";
    String TEMP_FOLDER_NAME = "Temp";

    interface RequestCodes {
        int EASYIMAGE_IDENTIFICATOR = 0b1101101100; //876
        int SOURCE_CHOOSER = 1 << 14;

        int PICK_PICTURE_FROM_DOCUMENTS = EASYIMAGE_IDENTIFICATOR + (1 << 11);
        int PICK_PICTURE_FROM_GALLERY = EASYIMAGE_IDENTIFICATOR + (1 << 12);
        int TAKE_PICTURE = EASYIMAGE_IDENTIFICATOR + (1 << 13);
    }

    interface BundleKeys {
        String FOLDER_NAME = "pl.aprilapps.folder_name";
        String FOLDER_LOCATION = "pl.aprilapps.folder_location";
        String ALLOW_MULTIPLE = "pl.aprilapps.allow_multiple";
        String STORAGE_DIRECTORY = "pl.aprilapps.directory_type";
    }
}
