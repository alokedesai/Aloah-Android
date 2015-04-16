package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import hu.ait.android.aloke.aloah.crypto.CryptoUtils;

/**
 * Created by Aloke on 4/16/15.
 */
public class UploadFile extends AsyncTask<Uri, Void, Boolean> {
    private Context context;

    public UploadFile(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Uri... params) {
        Uri uri = params[0];

        try {
            URI newURI = new URI(uri.toString());
            File tempFile = File.createTempFile("tempfile", ".tmp", context.getCacheDir());
            File inputFile = new File(newURI);

            // create client to connect to the azure sever
            CloudBlobClient blobClient = ((MainActivity) context).getStorageAccount().createCloudBlobClient();

            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference("testcontainer");
            CloudBlockBlob blob = container.getBlockBlobReference(inputFile.getName() + ".encrypted");

            // encrypt the file
            CryptoUtils.encrypt(MainActivity.KEY, inputFile, tempFile);
            blob.upload(new java.io.FileInputStream(tempFile), tempFile.length());

        } catch (IOException | StorageException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            ((MainActivity) context).makeToast("File uploaded successfully!");
        } else {
            ((MainActivity) context).makeToast("There was an error uploading!");
        }
    }
}
