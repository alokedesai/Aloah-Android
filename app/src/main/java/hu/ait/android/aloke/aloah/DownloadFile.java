package hu.ait.android.aloke.aloah;

import android.app.ProgressDialog;
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
public class DownloadFile extends AsyncTask<CloudBlockBlob, Void, Boolean> {

    private static final String FILTER_DOWNLOAD_FILE = "FILTER_DOWNLOAD_FILE";
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

            String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/";

            File tempFile = File.createTempFile("tempfile", ".tmp", context.getCacheDir());
            blob.downloadToFile(tempFile.getAbsolutePath());

            outputFile = new File(downloadPath, blob.getName().replace(".encrypted", ""));
            System.out.println("the proper path should be: " + outputFile.getAbsolutePath());
            // try to decrypt temp file and put it in outputfile
            try {
                success = CryptoUtils.decrypt(tempFile, outputFile);
            } catch (MediaCodec.CryptoException e1) {
                e1.printStackTrace();
                return false;
            } finally {
                tempFile.delete();
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
            ((MainActivity) context).makeToast("File successfully downloaded!");
            ((MainActivity) context).setIsDownloaded(index);
            ((MainActivity) context).setFile(index, outputFile);


        } else {
            ((MainActivity) context).makeToast("There was an error while downloading!");
        }

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }
}
