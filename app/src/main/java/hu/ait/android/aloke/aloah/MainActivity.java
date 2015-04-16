package hu.ait.android.aloke.aloah;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.widget.Toast;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;


public class MainActivity extends ListActivity{
    public static final String STORAGE_CONNECTION_STRING =
            "DefaultEndpointsProtocol=https;" +
                    "AccountName=aloah;" +
                    "AccountKey=t4gFHiiTQhPVYLqS3DI0EJ5loeEeU3vUqmIQFp57+UEfdL+FtRrhPAuB4i0Ad1S/pvxvO0DaI87FccGXw4Qstg==";

    private CloudStorageAccount storageAccount;
    private ArrayList<ListBlobItem> blobs = new ArrayList<>();

    private Iterable<ListBlobItem> blobIterable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AsyncTask<String, Void, Void> asyncTask = new loadBlobs();
        asyncTask.execute();
    }

    private void setBlobAdapter(ArrayList<ListBlobItem> blobs) {

        BlobListAdapter adapter = new BlobListAdapter(blobs, this);
        setListAdapter(adapter);
    }

    class loadBlobs extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                storageAccount = CloudStorageAccount.parse(STORAGE_CONNECTION_STRING);
            } catch (URISyntaxException | InvalidKeyException e) {
                e.printStackTrace();
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

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            setBlobAdapter(blobs);
        }
    }

    public void downloadFile(final CloudBlockBlob blob) {
        AsyncTask<CloudBlockBlob, Void, Boolean> asyncTask = new DownloadFile(this);
        asyncTask.execute(blob);
    }

    public void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
