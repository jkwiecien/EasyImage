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
    }

    public MainActivity() {
    }

    @OnClick(R.id.camera_button)
    protected void onTakePhotoClicked() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            EasyImage.openCamera(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
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

    @OnClick(R.id.gallery_button)
    protected void onPickFromGaleryClicked() {
        EasyImage.openGalleryPicker(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                //Handle the image
                onPhotoReturned(imageFile);
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
}
