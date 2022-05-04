package pl.aprilapps.easypicker

/**
 * Stas Parshin
 * 05 November 2015
 */
abstract class DefaultCallback : EasyPicker.Callbacks {

    override fun onPickerError(error: Throwable, source: MediaSource) {}

    override fun onCanceled(source: MediaSource) {}
}
