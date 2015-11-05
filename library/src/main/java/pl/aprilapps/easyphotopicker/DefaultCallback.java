package pl.aprilapps.easyphotopicker;

import java.io.File;

/**
 * Stas Parshin
 * 05 November 2015
 */
public class DefaultCallback implements EasyImage.Callbacks {

    @Override
    public void onImagePickerError(Exception e, EasyImage.ImageSource source) {}

    @Override
    public void onImagePicked(File imageFile, EasyImage.ImageSource source) {}

    @Override
    public void onCanceled(EasyImage.ImageSource source) {}
}
