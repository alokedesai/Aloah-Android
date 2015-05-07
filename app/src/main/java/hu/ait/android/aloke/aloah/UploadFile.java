package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;

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
public class UploadFile extends AsyncTask<Uri, Void, Boolean> {
    private Context context;

    public static final String FILTER_RESULT = "FILTER_RESULT";

    public UploadFile(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Uri... params) {
        Uri uri = params[0];
        boolean success = false;

        try {
            String path = getRealPathFromURI(context, uri);
            File tempFile = File.createTempFile("tempfile", ".tmp", context.getCacheDir());

            File inputFile = new File(path);

            // create client to connect to the azure sever
            CloudBlobClient blobClient = ((MainActivity) context).getStorageAccount().createCloudBlobClient();

            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference("testcontainer");
            CloudBlockBlob blob = container.getBlockBlobReference(inputFile.getName() + ".encrypted");


            File tempKeyFile = downloadTempKeyFile(blobClient);

            // encrypt the file
            String key = ((MainActivity) context).inputKey;
            success = CryptoUtils.encrypt(inputFile, tempFile);
            blob.upload(new java.io.FileInputStream(tempFile), tempFile.length());

        } catch (IOException | StorageException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        return success;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            ((MainActivity) context).makeToast("File uploaded successfully!");
            Intent intent = new Intent(FILTER_RESULT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        } else {
            ((MainActivity) context).makeToast("There was an error uploading!");
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private File downloadTempKeyFile(CloudBlobClient blobClient) throws URISyntaxException, StorageException, IOException {
        CloudBlobContainer keyContainer = blobClient.getContainerReference("keycontainer");
        CloudBlockBlob blobEncryptedKey = keyContainer.getBlockBlobReference(context.getString(R.string.user_id) + ".txt");
        File tempKeyFile = File.createTempFile("tempkeyfile", ".tmp", context.getCacheDir());
        System.out.println("the path of the file is: " + tempKeyFile.getAbsolutePath());
        blobEncryptedKey.downloadToFile(tempKeyFile.getAbsolutePath());
        return tempKeyFile;
    }
}
