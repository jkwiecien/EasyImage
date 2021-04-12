package pl.aprilapps.easyphotopicker.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

public class MainActivity extends AppCompatActivity implements EasyImage.EasyImageStateHandler {

    private static final String PHOTOS_KEY = "easy_image_photos_list";
    private static final String STATE_KEY = "easy_image_state";
    private static final int CHOOSER_PERMISSIONS_REQUEST_CODE = 7459;
    private static final int GALLERY_REQUEST_CODE = 7502;
    private static final int DOCUMENTS_REQUEST_CODE = 7503;
    private static final int LEGACY_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 456;

    protected RecyclerView recyclerView;

    protected View galleryButton;

    private ImagesAdapter imagesAdapter;

    private ArrayList<MediaFile> photos = new ArrayList<>();

    private EasyImage easyImage;

    private static final String[] LEGACY_WRITE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        galleryButton = findViewById(R.id.gallery_button);

        if (savedInstanceState != null) {
            photos = savedInstanceState.getParcelableArrayList(PHOTOS_KEY);
            easyImageState = savedInstanceState.getParcelable(STATE_KEY);
        }

        imagesAdapter = new ImagesAdapter(this, photos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(imagesAdapter);

        easyImage = new EasyImage.Builder(this)
                .setChooserTitle("Pick media")
                .setCopyImagesToPublicGalleryFolder(true) // THIS requires granting WRITE_EXTERNAL_STORAGE permission for devices running Android 9 or lower
//                .setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setFolderName("EasyImage sample")
                .allowMultiple(true)
                .setStateHandler(this)
                .build();

        checkGalleryAppAvailability();


        findViewById(R.id.gallery_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLegacyExternalStoragePermissionRequired()) {
                    requestLegacyWriteExternalStoragePermission();
                } else {
                    easyImage.openGallery(MainActivity.this);
                }
            }
        });


        findViewById(R.id.camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLegacyExternalStoragePermissionRequired()) {
                    requestLegacyWriteExternalStoragePermission();
                } else {
                    easyImage.openCameraForImage(MainActivity.this);
                }
            }
        });

        findViewById(R.id.camera_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLegacyExternalStoragePermissionRequired()) {
                    requestLegacyWriteExternalStoragePermission();
                } else {
                    easyImage.openCameraForVideo(MainActivity.this);
                }
            }
        });

        findViewById(R.id.documents_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLegacyExternalStoragePermissionRequired()) {
                    requestLegacyWriteExternalStoragePermission();
                } else {
                    easyImage.openDocuments(MainActivity.this);
                }
            }
        });

        findViewById(R.id.chooser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLegacyExternalStoragePermissionRequired()) {
                    requestLegacyWriteExternalStoragePermission();
                } else {
                    easyImage.openChooser(MainActivity.this);
                }
            }
        });

    }

    private Bundle easyImageState = new Bundle();

    @Override
    @NonNull
    public Bundle restoreEasyImageState() {
        return easyImageState;
    }

    @Override
    public void saveEasyImageState(Bundle state) {
        easyImageState = state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PHOTOS_KEY, photos);
        outState.putParcelable(STATE_KEY, easyImageState);
    }

    private void checkGalleryAppAvailability() {
        if (!easyImage.canDeviceHandleGallery()) {
            //Device has no app that handles gallery intent
            galleryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CHOOSER_PERMISSIONS_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openChooser(MainActivity.this);
        } else if (requestCode == GALLERY_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openGallery(MainActivity.this);
        } else if (requestCode == DOCUMENTS_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openDocuments(MainActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                for (MediaFile imageFile : imageFiles) {
                    Log.d("EasyImage", "Image file returned: " + imageFile.getFile().toString());
                }
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });
    }

    private void onPhotosReturned(@NonNull MediaFile[] returnedPhotos) {
        photos.addAll(Arrays.asList(returnedPhotos));
        imagesAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(photos.size() - 1);
    }

    private boolean isLegacyExternalStoragePermissionRequired() {
        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return Build.VERSION.SDK_INT < 29 && !permissionGranted;
    }

    private void requestLegacyWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this, LEGACY_WRITE_PERMISSIONS, LEGACY_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }
}
