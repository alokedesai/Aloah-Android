package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.os.Environment;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import hu.ait.android.aloke.aloah.crypto.CryptoUtils;

/**
 * Created by Aloke on 4/16/15.
 */
public class DownloadFile extends AsyncTask<CloudBlockBlob, Void, File> {

    private static final String FILTER_DOWNLOAD_FILE = "FILTER_DOWNLOAD_FILE";
    private Context context;
    private int index;

    public DownloadFile(Context context, int index) {
        this.context = context;
        this.index = index;
    }

    @Override
    protected File doInBackground(CloudBlockBlob... params) {
        CloudBlockBlob blob = params[0];
        File outputFile = null;
        try {
            String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath();

            File tempFile = File.createTempFile("tempfile", ".tmp", context.getCacheDir());
            blob.downloadToFile(tempFile.getAbsolutePath());

            outputFile = new File(downloadPath, blob.getName().replace(".encrypted", ""));

            // try to decrypt temp file and put it in outputfile
            try {
                CryptoUtils.decrypt(tempFile, outputFile);
            } catch (MediaCodec.CryptoException e1) {
                e1.printStackTrace();
                outputFile = null;
            } finally {
                tempFile.delete();
            }



        } catch (StorageException | URISyntaxException | IOException e) {
            e.printStackTrace();
            outputFile = null;
        }

        return outputFile;
    }

    @Override
    protected void onPostExecute(File result) {
        if (result != null) {
            ((MainActivity) context).makeToast("File successfully downloaded!");
            ((MainActivity) context).setIsDownloaded(index);
            ((MainActivity) context).setFile(index, result);


        } else {
            ((MainActivity) context).makeToast("There was an error while downloading!");
        }

    }
}
