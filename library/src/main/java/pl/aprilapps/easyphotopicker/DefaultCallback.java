package pl.aprilapps.easyphotopicker;

/**
 * Stas Parshin
 * 05 November 2015
 */
public abstract class DefaultCallback implements EasyImage.Callbacks {

    @Override
    public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
    }

    @Override
    public void onCanceled(EasyImage.ImageSource source, int type) {
    }
}
