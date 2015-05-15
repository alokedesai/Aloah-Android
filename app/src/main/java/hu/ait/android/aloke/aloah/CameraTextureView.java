package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Noah on 5/12/2015.
 */
public class CameraTextureView extends TextureView implements
        TextureView.SurfaceTextureListener {

    private Camera camera;
    private MediaRecorder mediaRecorder;
    private SurfaceTexture surface;

    private Context context;

    public CameraTextureView(Context context) {
        super(context);
        this.context = context;
        setSurfaceTextureListener(this);
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture s, int width,
                                          int height) {
        camera = Camera.open();

        camera.setDisplayOrientation(90);

        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(90);
        //parameters.setColorEffect(Camera.Parameters.EFFECT_BLACKBOARD);

        camera.setParameters(parameters);

        surface = s;

        try {
            camera.setPreviewTexture(surface);
        } catch (IOException t) {
        }

        camera.setPreviewCallback(previewCallback);
        camera.startPreview();

    }


    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            //Log.d("PREVIEW SIZE", data.length+"");
        }
    };

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
        // Ignored, the Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Called whenever a new frame is available and displayed in the
        // TextureView
    }

    public void takePhoto(Camera.PictureCallback pictureCallback) {
        new TakePictureTask(pictureCallback).execute();
    }

    public void endVideoCapture() {
        // stop recording and release camera
        mediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        camera.lock();         // take camera access back from MediaRecorder

    }

    public void startVideoCapture() {
        if (prepareVideoRecorder()) {
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            mediaRecorder.start();

            // inform the user that recording has started
            //setCaptureButtonText("Stop");

        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();
            // inform user
        }
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    private boolean prepareVideoRecorder(){

        //camera = getCameraInstance();
        mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        camera.unlock();
        mediaRecorder.setCamera(camera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        //mediaRecorder.setPreviewDisplay();

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("Tag_", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("Tag_", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private class TakePictureTask extends AsyncTask<Void, Void, Void> {
        private Camera.PictureCallback pictureCallback;
        public TakePictureTask(Camera.PictureCallback pictureCallback) {
            this.pictureCallback = pictureCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            camera.takePicture(null, null, pictureCallback);

            // Sleep for however long, you could store this in a variable and
            // have it updated by a menu item which the user selects.
            try {
                Thread.sleep(1000); // 1 second preview
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // This returns the preview back to the live camera feed
//            camera.startPreview();
        }
    }
}
