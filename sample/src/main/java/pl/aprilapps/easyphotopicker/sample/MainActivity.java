package pl.aprilapps.easyphotopicker.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class MainActivity extends AppCompatActivity {

    private static final String PHOTOS_KEY = "easy_image_photos_list";

    @Bind(R.id.recycler_view)
    protected RecyclerView recyclerView;

    @Bind(R.id.gallery_button)
    protected View galleryButton;

    private ImagesAdapter imagesAdapter;

    private ArrayList<File> photos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Nammu.init(this);

        if (savedInstanceState != null) {
            photos = (ArrayList<File>) savedInstanceState.getSerializable(PHOTOS_KEY);
        }

        imagesAdapter = new ImagesAdapter(this, photos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(imagesAdapter);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    //Nothing, this sample saves to Public gallery so it needs permission
                }

                @Override
                public void permissionRefused() {
                    finish();
                }
            });
        }

        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true);

        checkGalleryAppAvailability();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PHOTOS_KEY, photos);
    }

    private void checkGalleryAppAvailability() {
        if (!EasyImage.canDeviceHandleGallery(this)) {
            //Device has no app that handles gallery intent
            galleryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick(R.id.camera_button)
    protected void onTakePhotoClicked() {
        EasyImage.openCamera(this, 0);
    }

    @OnClick(R.id.documents_button)
    protected void onPickFromDocumentsClicked() {
        /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            EasyImage.openDocuments(this, 0);
        } else {
            Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    EasyImage.openDocuments(MainActivity.this, 0);
                }

                @Override
                public void permissionRefused() {

                }
            });
        }
    }

    @OnClick(R.id.gallery_button)
    protected void onPickFromGaleryClicked() {
        /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */
        EasyImage.openGallery(this, 0);
    }

    @OnClick(R.id.chooser_button)
    protected void onChooserClicked() {
        EasyImage.openChooserWithDocuments(this, "Pick source", 0);
    }

    @OnClick(R.id.chooser_button2)
    protected void onChooserWithGalleryClicked() {
        EasyImage.openChooserWithGallery(this, "Pick source", 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(MainActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    private void onPhotosReturned(List<File> returnedPhotos) {
        photos.addAll(returnedPhotos);
        imagesAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(photos.size() - 1);
    }

    @Override
    protected void onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(this);
        super.onDestroy();
    }
}
