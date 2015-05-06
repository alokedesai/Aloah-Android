package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.os.Environment;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
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

    public DownloadFile(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(CloudBlockBlob... params) {
        CloudBlockBlob blob = params[0];
        boolean success = false;

        try {
            String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath();

            // create client to connect to the azure sever
            CloudBlobClient blobClient = ((MainActivity) context).getStorageAccount().createCloudBlobClient();
            File tempKeyFile = downloadTempKeyFile(blobClient);
            File tempFile = File.createTempFile("tempfile", ".tmp", context.getCacheDir());

            blob.downloadToFile(tempKeyFile.getAbsolutePath());

            File outputFile = new File(downloadPath, blob.getName().replace(".encrypted", ""));

            // try to decrypt temp file and put it in outputfile

            String key = ((MainActivity) context).inputKey;
            success = CryptoUtils.decrypt(tempKeyFile, tempFile, outputFile);



        } catch (StorageException | URISyntaxException | IOException | MediaCodec.CryptoException e) {
            e.printStackTrace();
            return false;
        }

        return success;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            ((MainActivity) context).makeToast("File successfully downloaded!");
        } else {
            ((MainActivity) context).makeToast("There was an error while downloading!");
        }
    }

    private File downloadTempKeyFile(CloudBlobClient blobClient) throws URISyntaxException, StorageException, IOException {
        CloudBlobContainer keyContainer = blobClient.getContainerReference("keycontainer");
        CloudBlockBlob blobEncryptedKey = keyContainer.getBlockBlobReference(context.getString(R.string.user_id) + ".txt");
        File tempKeyFile = File.createTempFile("tempkeyfile_download", ".tmp", context.getCacheDir());
        System.out.println("the path of the file is: " + tempKeyFile.getAbsolutePath());
        blobEncryptedKey.downloadToFile(tempKeyFile.getAbsolutePath());
        return tempKeyFile;
    }
}
