package pl.aprilapps.easyphotopicker;

/**
 * Created by Jacek Kwiecień on 03.11.2015.
 */
public interface EasyImageConfig {

    int EASYIMAGE_IDENTIFICATOR = 0b1101101100; //876
    int REQ_SOURCE_CHOOSER = 1 << 14;
    int REQ_PICK_PICTURE_FROM_DOCUMENTS = EASYIMAGE_IDENTIFICATOR + (1 << 11);
    int REQ_PICK_PICTURE_FROM_GALLERY = EASYIMAGE_IDENTIFICATOR + (1 << 12);
    int REQ_TAKE_PICTURE = EASYIMAGE_IDENTIFICATOR + (1 << 13);
}
