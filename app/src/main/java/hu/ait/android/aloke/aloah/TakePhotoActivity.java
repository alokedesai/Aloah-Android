package hu.ait.android.aloke.aloah;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import at.markushi.ui.CircleButton;


public class TakePhotoActivity extends ActionBarActivity {

    public static final String PHOTO_PATH = "PHOTO_PATH";
    public static final int REQUEST_VIDEO_CAPTURE = 1;
    private CameraTextureView camTextureView;
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        //camTextureView = (CameraTextureView) findViewById(R.id.camTextureView);

        CircleButton btnPhoto = (CircleButton) findViewById(R.id.btnPhoto);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // camTextureView.takePhoto(pictureCallback);
            }
        });

        CircleButton btnVideo = (CircleButton) findViewById(R.id.btnVideo);
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dispatchTakeVideoIntent();
//                if (!recording) {
//                    camTextureView.startVideoCapture();
//
//                    recording = true;
//                    Toast.makeText(TakePhotoActivity.this, "Now recording", Toast.LENGTH_SHORT).show();
//                } else {
//                    camTextureView.endVideoCapture();
//
//                    recording = false;
//                    Toast.makeText(TakePhotoActivity.this, "Stopped recording", Toast.LENGTH_SHORT).show();
//                }
                //camTextureView.takePhoto(pictureCallback);




            }
        });
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            Toast.makeText(this, "Finished recording",Toast.LENGTH_LONG);
            //mVideoView.setVideoURI(videoUri);
        }
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            ImageView image = new ImageView(MainActivity.this);
            //v.setImageBitmap(bitmap);
            System.out.println("picturex");

            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Aloah");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    System.out.println("failed to create directory");
                    return;
                }
            }

            File pictureFile = new File(mediaStorageDir.getPath() +
                    File.separator + "photo" + System.currentTimeMillis() + ".png");

            if (pictureFile == null) {
                System.out.println("picture file is null");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            attachMetaData(pictureFile);

            System.out.println("Path taken photo: " + pictureFile.getAbsolutePath());
            String path = pictureFile.getAbsolutePath();


            Intent intentResult = new Intent();
            intentResult.putExtra(PHOTO_PATH, path);
            setResult(RESULT_OK, intentResult);
            finish();

//            try {
//                FileOutputStream stream = new FileOutputStream(file);
//                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
//                stream.close();
//
//                System.out.println("picturex added to downloads");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
    };

    private void attachMetaData(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "desc");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        ContentResolver cr = getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    }
}
