package hu.ait.android.aloke.aloah.task;

import android.app.ProgressDialog;
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

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.MainActivity;
import hu.ait.android.aloke.aloah.R;
import hu.ait.android.aloke.aloah.crypto.CryptoUtils;
import hu.ait.android.aloke.aloah.event.UploadFileEvent;

/**
 * Created by Aloke on 4/16/15.
 */
public class UploadFile extends AsyncTask<String, Void, Boolean> {
    private Context context;
    private ProgressDialog progressDialog;

    public UploadFile(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Uploading File");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String path = params[0];
        boolean success = false;

        try {
            //String path = getRealPathFromURI(context, uri);
            File tempFile = File.createTempFile("tempfile", ".tmp", context.getCacheDir());

            File inputFile = new File(path);

            // create client to connect to the azure sever
            CloudBlobClient blobClient = ((MainActivity) context).getStorageAccount().createCloudBlobClient();

            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference(MainActivity.TEST_CONTAINER);
            CloudBlockBlob blob = container.getBlockBlobReference(inputFile.getName());

            // encrypt the file
            String key = ((MainActivity) context).inputKey;
            success = CryptoUtils.encrypt(inputFile, tempFile);
            blob.upload(new java.io.FileInputStream(tempFile), tempFile.length());

            tempFile.delete();
        } catch (IOException | StorageException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        return success;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        EventBus.getDefault().post(new UploadFileEvent(result));
    }

//    public String getRealPathFromURI(Context context, Uri contentUri) {
//
//        Cursor cursor = null;
//        try {
//            String[] proj = { MediaStore.Images.Media.DATA };
//            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            System.out.println("URI real path: "+cursor.getString(column_index));
//            return cursor.getString(column_index);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//    }

    private File downloadTempKeyFile(CloudBlobClient blobClient) throws URISyntaxException, StorageException, IOException {
        CloudBlobContainer keyContainer = blobClient.getContainerReference("keycontainer");
        CloudBlockBlob blobEncryptedKey = keyContainer.getBlockBlobReference(context.getString(R.string.user_id) + ".txt");
        File tempKeyFile = File.createTempFile("tempkeyfile", ".tmp", context.getCacheDir());
        System.out.println("the path of the file is: " + tempKeyFile.getAbsolutePath());
        blobEncryptedKey.downloadToFile(tempKeyFile.getAbsolutePath());
        return tempKeyFile;
    }
}
