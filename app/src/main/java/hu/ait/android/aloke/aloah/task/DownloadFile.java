package hu.ait.android.aloke.aloah.task;

import android.app.ProgressDialog;
import android.app.usage.UsageEvents;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.crypto.CryptoUtils;
import hu.ait.android.aloke.aloah.event.DownloadFileEvent;

/**
 * Created by Aloke on 4/16/15.
 */
public class DownloadFile extends AsyncTask<CloudBlockBlob, Void, Boolean> {

    private Context context;
    private int index;
    private ProgressDialog progressDialog;
    private boolean success;
    private File outputFile;


    public DownloadFile(Context context, int index) {
        this.context = context;
        this.index = index;

        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Downloading File");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected Boolean doInBackground(CloudBlockBlob... params) {
        CloudBlockBlob blob = params[0];

        try {

            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Decrypted");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    System.out.println("failed to create directory");
                    return false;
                }
            }

            File tempFile = File.createTempFile("tempfile", ".tmp", context.getCacheDir());
            blob.downloadToFile(tempFile.getAbsolutePath());

            outputFile = new File(mediaStorageDir.getPath() +
                    File.separator + blob.getName());

            //outputFile = new File(downloadPath, blob.getName().replace(".encrypted", ""));
            System.out.println("the proper path should be: " + outputFile.getAbsolutePath());

            // try to decrypt temp file and put it in outputfile
            try {
                success = CryptoUtils.decrypt(tempFile, outputFile);
            } catch (MediaCodec.CryptoException e1) {
                e1.printStackTrace();
                return false;
            } finally {
                tempFile.delete();
                attachMetaData(outputFile);
            }



        } catch (StorageException | URISyntaxException | IOException e) {
            e.printStackTrace();
            return false;
        }

        return success;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            EventBus.getDefault().post(new DownloadFileEvent(index, success, outputFile));
        } else {
            EventBus.getDefault().post(new DownloadFileEvent(success));
        }

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }

    private void attachMetaData(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "desc");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        ContentResolver cr = context.getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    }
}
