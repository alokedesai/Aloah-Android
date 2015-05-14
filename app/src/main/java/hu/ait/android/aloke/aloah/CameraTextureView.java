package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.TextureView;

import java.io.IOException;


/**
 * Created by Noah on 5/12/2015.
 */
public class CameraTextureView extends TextureView implements
        TextureView.SurfaceTextureListener {

    private Camera camera;

    public CameraTextureView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
        camera = Camera.open();

        camera.setDisplayOrientation(90);

        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(90);
        //parameters.setColorEffect(Camera.Parameters.EFFECT_BLACKBOARD);

        camera.setParameters(parameters);

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
