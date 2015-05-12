package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.os.AsyncTask;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * Created by Aloke on 5/11/15.
 */
public class GetNumKeyBlobs extends AsyncTask<Void, Void, Integer> {
    private Context context;

    public GetNumKeyBlobs(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int numKeyBlobs = 0;

        CloudStorageAccount storageAccount = null;
        try {
            storageAccount = CloudStorageAccount.parse(MainActivity.STORAGE_CONNECTION_STRING);
        } catch (URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
        }


        // get all the images from a container
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        try {
            System.out.println("inside runnable");
            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference(MainActivity.KEY_CONTAINER);


            for (ListBlobItem b : container.listBlobs()) {
                numKeyBlobs++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numKeyBlobs;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        ((MainActivity) context).onThreadFinish(integer);
    }
}
