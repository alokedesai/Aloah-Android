package hu.ait.android.aloke.aloah.task;

import android.os.AsyncTask;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.MainActivity;
import hu.ait.android.aloke.aloah.event.LoadBlobsEvent;
import hu.ait.android.aloke.aloah.model.ImageItem;

/**
 * Created by Aloke on 5/15/15.
 */
public class LoadBlobs extends AsyncTask<String, Void, ArrayList<ImageItem>> {
    private CloudStorageAccount storageAccount;

    public LoadBlobs(CloudStorageAccount storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<ImageItem> doInBackground(String... params) {
        ArrayList<ImageItem> images = new ArrayList<ImageItem>();
        try {
            storageAccount = CloudStorageAccount.parse(MainActivity.STORAGE_CONNECTION_STRING);
        } catch (URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
        }

        // get all the images from a container
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        try {
            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference(MainActivity.TEST_CONTAINER);

            for (ListBlobItem b : container.listBlobs()) {
                images.add(new ImageItem(b));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return images;
    }

    @Override
    protected void onPostExecute(ArrayList<ImageItem> blobs) {
        EventBus.getDefault().post(new LoadBlobsEvent(blobs));
    }
}

