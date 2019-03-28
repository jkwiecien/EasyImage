package pl.aprilapps.easyphotopicker.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class MainActivity extends AppCompatActivity {

    private static final String PHOTOS_KEY = "easy_image_photos_list";

    protected RecyclerView recyclerView;

    protected View galleryButton;

    private ImagesAdapter imagesAdapter;

    private ArrayList<MediaFile> photos = new ArrayList<>();

    private EasyImage easyImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Nammu.init(this);

        recyclerView = findViewById(R.id.recycler_view);
        galleryButton = findViewById(R.id.gallery_button);

        if (savedInstanceState != null) {
            photos = savedInstanceState.getParcelableArrayList(PHOTOS_KEY);
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

        easyImage = new EasyImage.Builder(this)
                .setChooserTitle("Pick media")
                .setCopyImagesToPublicGalleryFolder(false)
//                .setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setFolderName("EasyImage sample")
                .allowMultiple(true)
                .build();

        checkGalleryAppAvailability();


        findViewById(R.id.gallery_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */
                easyImage.openGallery(MainActivity.this);
            }
        });


        findViewById(R.id.camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easyImage.openCameraForImage(MainActivity.this);
            }
        });

        findViewById(R.id.camera_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easyImage.openCameraForVideo(MainActivity.this);
            }
        });

        findViewById(R.id.documents_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */

                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    easyImage.openDocuments(MainActivity.this);
                } else {
                    Nammu.askForPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
                        @Override
                        public void permissionGranted() {
                            easyImage.openDocuments(MainActivity.this);
                        }

                        @Override
                        public void permissionRefused() {

                        }
                    });
                }
            }
        });

        findViewById(R.id.chooser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easyImage.openChooser(MainActivity.this);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PHOTOS_KEY, photos);
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
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
}
