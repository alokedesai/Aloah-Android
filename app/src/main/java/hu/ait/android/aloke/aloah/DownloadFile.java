package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import hu.ait.android.aloke.aloah.Crypto.CryptoUtils;

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
            String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath();

            File tempFile = File.createTempFile("tempfile", ".tmp", context.getCacheDir());
            blob.downloadToFile(tempFile.getAbsolutePath());

            File outputFile = new File(downloadPath, blob.getName().replace(".encrypted", ""));

            // try decrypt temp file and put it in outputfile
            try {
                CryptoUtils.decrypt(MainActivity.KEY, tempFile, outputFile);
            } catch (MediaCodec.CryptoException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


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