package pl.aprilapps.easypicker

/**
 * Created by Jacek Kwiecień on 03.11.2015.
 */

/**
 * EasyImage request codes are set to be between 34961 and 34965.
 * Unlikely to be a duplicate of clients request code but not ideal.
 */

internal const val EASYPICKER_LOG_TAG = "EasyPicker"

internal object RequestCodes {
    const val PICK_FILE_FROM_DOCUMENTS = 34961
    const val PICK_PICTURE_FROM_GALLERY = 34962
    const val PICK_FILE_FROM_CHOOSER = 34963
    const val TAKE_PICTURE = 34964
    const val CAPTURE_VIDEO = 34965
}