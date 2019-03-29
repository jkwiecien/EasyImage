package pl.aprilapps.easyphotopicker

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.File

data class MediaFile(internal val uri: Uri, val file: File) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Uri::class.java.classLoader)!!,
            parcel.readSerializable() as File)


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeSerializable(file)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaFile> {
        override fun createFromParcel(parcel: Parcel): MediaFile {
            return MediaFile(parcel)
        }

        override fun newArray(size: Int): Array<MediaFile?> {
            return arrayOfNulls(size)
        }
    }
}