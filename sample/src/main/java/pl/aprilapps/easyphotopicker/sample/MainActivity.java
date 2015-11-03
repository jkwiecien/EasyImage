package pl.aprilapps.easyphotopicker.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.EasyImageConfig;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.image_view)
    protected ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.camera_button)
    protected void onTakePhotoClicked() {
        EasyImage.openCamera(this);
    }

    @OnClick(R.id.gallery_button)
    protected void onPickFromGaleryClicked() {
        EasyImage.openGalleryPicker(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY && data != null) {
            Uri originalPhotoPath = data.getData();
            try {
                onPhotoReturned(EasyImage.pickedGalleryPicture(this, originalPhotoPath));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == EasyImageConfig.REQ_TAKE_PICTURE) {
            try {
                File photoFile = EasyImage.takenCameraPicture(this);
                onPhotoReturned(photoFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onPhotoReturned(File photoFile) {
        Picasso.with(this)
                .load(photoFile)
                .fit()
                .centerCrop()
                .into(imageView);
    }
}
