package pl.aprilapps.easyphotopicker.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource

class CustomRequestCodeActivity : AppCompatActivity() {
    private lateinit var easyImage : EasyImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_request_code)
        initEasyImage()
        pickImage1()
        pickImage2()
    }

    private fun initEasyImage(){

    }

    private fun pickImage1(){
        val btnPick1 = findViewById<Button>(R.id.button_pickImage1)
        btnPick1.setOnClickListener {
            easyImage = EasyImage.Builder(this).apply {
                title = "Choose image"
                allowMultiple(false)
            }.build()
            easyImage.openGallery(this)
        }
    }

    private fun pickImage2(){
        val pickImage2 = findViewById<Button>(R.id.button_pickImage2)
        pickImage2.setOnClickListener {
            easyImage = EasyImage.Builder(this).apply {
                title = "Choose image"
                allowMultiple(false)
                setRequestCode(100)
            }.build()
            easyImage.openGallery(this)
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun onPhotosCustomRequestCodeReturned(images : Array<MediaFile>){
        val imageView2 = findViewById<ImageView>(R.id.imageView2)
        Picasso.get().load(images[0].file).fit().centerCrop().into(imageView2)
    }

    private fun onPhotosReturned(images : Array<MediaFile>){
        val imageView1 = findViewById<ImageView>(R.id.imageView1)
        Picasso.get().load(images[0].file).fit().centerCrop().into(imageView1)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        toast(requestCode.toString())
        easyImage.handleActivityResult(requestCode, resultCode, data, this, object: DefaultCallback(){
            override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                if (requestCode == 100){
                    onPhotosCustomRequestCodeReturned(imageFiles)
                }else{
                    onPhotosReturned(imageFiles)
                }
            }

            override fun onImagePickerError(error: Throwable, source: MediaSource) {
                super.onImagePickerError(error, source)
                Toast.makeText(this@CustomRequestCodeActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })

    }
}