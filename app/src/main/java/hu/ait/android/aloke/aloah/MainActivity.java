package hu.ait.android.aloke.aloah;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

import hu.ait.android.aloke.aloah.adapter.BlobListAdapter;


public class MainActivity extends ActionBarActivity implements KeyEntryDialog.KeyEntryDialogListener{
    public static final String STORAGE_CONNECTION_STRING =
            "DefaultEndpointsProtocol=https;" +
                    "AccountName=aloah;" +
                    "AccountKey=t4gFHiiTQhPVYLqS3DI0EJ5loeEeU3vUqmIQFp57+UEfdL+FtRrhPAuB4i0Ad1S/pvxvO0DaI87FccGXw4Qstg==";

    public static final String KEY = "password";
    public static final int FILE_CODE = 101;

    private CloudStorageAccount storageAccount;
    private ArrayList<ListBlobItem> blobs = new ArrayList<>();
    private ListView listView;
    private boolean canClickBtnRefresh = false;

    private BlobListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.tvEmpty));

        Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canClickBtnRefresh) {
                    canClickBtnRefresh = false;
                    loadBlobs();
                }
            }
        });

        loadBlobs();
    }

    private void loadBlobs() {
        AsyncTask<String, Void, Void> asyncTask = new loadBlobs();
        asyncTask.execute();
    }

    private void setBlobAdapter(ArrayList<ListBlobItem> blobs) {

        adapter = new BlobListAdapter(blobs, this);
        listView.setAdapter(adapter);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String key) {
        Toast.makeText(this, "User input key: "+key, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();

    }

    class loadBlobs extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            // reset blobs
            blobs.clear();
        }

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

                for (ListBlobItem b : container.listBlobs()) {
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

            canClickBtnRefresh = true;
            setBlobAdapter(blobs);
        }
    }

    public void downloadFile(int position) {
        CloudBlockBlob blob = (CloudBlockBlob) adapter.getItem(position);
        AsyncTask<CloudBlockBlob, Void, Boolean> asyncTask = new DownloadFile(this);
        asyncTask.execute(blob);
    }

    public void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            // start the file picker to choose the file you want to be encrypted on the server
            Intent intent = new Intent(this, FilePickerActivity.class);
            startActivityForResult(intent, FILE_CODE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            uploadFile(uri);
        }
    }

    private void uploadFile(Uri uri) {
        AsyncTask<Uri, Void, Boolean> asyncTask = new UploadFile(this);
        asyncTask.execute(uri);
    }

    public CloudStorageAccount getStorageAccount() {
        return storageAccount;
    }

    public void showKeyEntryDialog() {
        KeyEntryDialog keyEntryDialog = new KeyEntryDialog();
        keyEntryDialog.show(getSupportFragmentManager(), KeyEntryDialog.TAG);
    }
}
