package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Aloke on 4/16/15.
 */
public class DownloadFile extends AsyncTask<CloudBlockBlob, Void, Boolean> {

    private static final String FILTER_DOWNLOAD_FILE = "FILTER_DOWNLOAD_FILE";
    private Context context;

    public DownloadFile(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(CloudBlockBlob... params) {
        CloudBlockBlob blob = params[0];

        try {
            blob.downloadToFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + blob.getName());


        } catch (StorageException | URISyntaxException | IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            ((MainActivity) context).makeToast("File successfully downloaded!");
        } else {
            ((MainActivity) context).makeToast("There was an error while downloading!");
        }

    }
}
