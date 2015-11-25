package pl.aprilapps.easyphotopicker.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    @Bind(R.id.image_view)
    protected ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        /**
         * If saving in public app folder inside Pictures by using saveInAppExternalFilesDir,
         * write permission after SDK 18 is NOT required as can be seen in manifest.
         *
         * If saving in the root of sdcard inside Pictures by using saveInRootPicturesDirectory,
         * permission is required.
         *
         * By default, if no configuration is set Images Folder Name will be EasyImage, and save
         * location will be in the ExternalFilesDir of the app.
         * */
        EasyImage.configuration(this)
                .setImagesFolderName("Sample app images")
                .saveInAppExternalFilesDir();

//        EasyImage.configuration(this)
//                .setImagesFolderName("Sample app images")
//                .saveInRootPicturesDirectory();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                EasyImage.openCamera(this);
            } else {
                // permission denied, boo!
            }
        }
    }

    @OnClick(R.id.camera_button)
    protected void onTakePhotoClicked() {

        /**Permission check only required if saving pictures to root of sdcard*/
//        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//            EasyImage.openCamera(this);
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
//        }

        EasyImage.openCamera(this);
    }

    @OnClick(R.id.documents_button)
    protected void onPickFromDocumentsClicked() {
        /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */
        EasyImage.openDocumentsPicker(this);
    }

    @OnClick(R.id.gallery_button)
    protected void onPickFromGaleryClicked() {
        /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */
        EasyImage.openGalleryPicker(this);
    }

    @OnClick(R.id.chooser_button)
    protected void onChoserClicked() {
        EasyImage.openChooser(this, "Pick source");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                //Handle the image
                onPhotoReturned(imageFile);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(MainActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    private void onPhotoReturned(File photoFile) {
        Picasso.with(this)
                .load(photoFile)
                .fit()
                .centerCrop()
                .into(imageView);
    }

    @Override
    protected void onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(this);
        super.onDestroy();
    }
}
