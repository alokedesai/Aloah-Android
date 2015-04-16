package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

/**
 * Created by Aloke on 4/16/15.
 */
public class GetBlobList extends AsyncTask<Void, Void, ArrayList<ListBlobItem>> {

    public static final String FILTER_RESULT = "FILTER_RESULT";
    public static final String KEY_RESULT = "KEY_RESULT";

    private Context context;

    public GetBlobList(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<ListBlobItem> doInBackground(Void... params) {
        ArrayList<ListBlobItem> blobs = new ArrayList<>();

        CloudStorageAccount storageAccount = null;
        try {
             storageAccount = CloudStorageAccount.parse(MainActivity.STORAGE_CONNECTION_STRING);
        } catch (URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }

        // get all the blobs from a container
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        try {
            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference("testcontainer");

            for (ListBlobItem b :container.listBlobs()) {
                blobs.add(b);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return blobs;
    }

    @Override
    protected void onPostExecute(ArrayList<ListBlobItem> listBlobItems) {
        //update the UI thread with the list of blobs
        // get the result from the doInBackground
        Intent intentResult = new Intent(FILTER_RESULT);
        intentResult.putExtra(KEY_RESULT, listBlobItems);

        // how to send a local broadcast
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentResult);

    }
}
