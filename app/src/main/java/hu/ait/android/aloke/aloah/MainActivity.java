package hu.ait.android.aloke.aloah;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.BufferUnderflowException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

import hu.ait.android.aloke.aloah.adapter.BlobListAdapter;
import hu.ait.android.aloke.aloah.crypto.CryptoUtils;


public class MainActivity extends ActionBarActivity implements KeyEntryDialog.KeyEntryDialogListener{
    public static final String STORAGE_CONNECTION_STRING =
            "DefaultEndpointsProtocol=https;" +
                    "AccountName=aloah;" +
                    "AccountKey=t4gFHiiTQhPVYLqS3DI0EJ5loeEeU3vUqmIQFp57+UEfdL+FtRrhPAuB4i0Ad1S/pvxvO0DaI87FccGXw4Qstg==";

    public static final String KEY_CONTAINER = "keycontainer";
    public static final String TEST_CONTAINER = "testcontainer";

    public static final int DOWNLOAD_STATE = 0;
    public static final int UPLOAD_STATE = 1;

    public String inputKey;
    private CloudBlockBlob blobToDownload = null;
    private Uri uriForUpload = null;
    private int currentState;

    public static final String KEY = "passwordpassword";
    public static final int FILE_CODE = 101;

    private CloudStorageAccount storageAccount;
    private ArrayList<ListBlobItem> blobs = new ArrayList<>();
    private ListView listView;
    private boolean canClickBtnRefresh = false;

    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CryptoUtils.setContext(this);

        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.tvEmpty));
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBlobs();
            }
        });

        loadBlobs();

        SharedPreferences sp = getSharedPreferences("KEY", Context.MODE_PRIVATE);
        String key = sp.getString("USER_ID", "-1");

        if ("-1".equals(key)) {
            key = "1";
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("USER_ID", key);
            editor.commit();  //Use commit() because we want user_id stored immediately

            System.out.println("the value of the user id is: ");
            // create public and private keys
            CryptoUtils.setContext(this);
            byte[] encryptedKeyBytes = CryptoUtils.printKeys("passwordpassword");
            String encryptedKeyString = Base64.encodeToString(encryptedKeyBytes, Base64.DEFAULT);
            System.out.println("the encrypted key is: \n" + encryptedKeyString );
            System.out.println("\nthe encrypted key without newlines: \n:" + encryptedKeyString.replaceAll("\n", ""));

            encryptedKeyString = encryptedKeyString.replaceAll("\n", "");
            uploadEncryptedKey(encryptedKeyString);
        }



        // set up the fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);

        // add listener to create new shopping items using fav
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGalleryIntent();
            }
        });

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
            startGalleryIntent();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("image/*");
        startActivityForResult(intent, FILE_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                uploadDoneReceiver, new IntentFilter(UploadFile.FILTER_RESULT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uploadDoneReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {

            currentState = UPLOAD_STATE;

            uriForUpload = data.getData();
            uploadFile(uriForUpload);
        }
    }

    public void setState(int state) {
        currentState = state;
    }

    public int getState() {
        return currentState;
    }

    private void loadBlobs() {
        AsyncTask<String, Void, Void> asyncTask = new LoadBlobs();
        asyncTask.execute();
    }

    private void setBlobAdapter(ArrayList<ListBlobItem> blobs) {

        BlobListAdapter adapter = new BlobListAdapter(blobs, this);
        listView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void showKeyEntryDialog() {
        KeyEntryDialog keyEntryDialog = new KeyEntryDialog();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        keyEntryDialog.show(fragmentTransaction, KeyEntryDialog.TAG);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String key) {
        makeToast("User input key "+ key);
        inputKey = key;

        if (currentState == DOWNLOAD_STATE) {
            downloadFile(blobToDownload);
            blobToDownload = null;
        } else if (currentState == UPLOAD_STATE) {
            uploadFile(uriForUpload);
            uriForUpload = null;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        makeToast("Cancelled");

        dialog.dismiss();
        blobToDownload = null;

    }

    public void downloadFile(CloudBlockBlob blob) {
        if (blob == null) {
            makeToast("Null blob, download failed.");
            return;
        }
        AsyncTask<CloudBlockBlob, Void, Boolean> asyncTask = new DownloadFile(this);
        asyncTask.execute(blob);
    }

    private void uploadFile(Uri uri) {
        if (uri == null) {
            makeToast("Uri null, upload failed");
            return;
        }
        AsyncTask<Uri, Void, Boolean> asyncTask = new UploadFile(this);
        asyncTask.execute(uri);
    }
    
    private void uploadEncryptedKey(String encryptedKey) {
        AsyncTask<String, Void, Boolean> asyncTask = new UploadEncryptedKey(this);
        asyncTask.execute(encryptedKey);
    }


    public void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public CloudStorageAccount getStorageAccount() {
        return storageAccount;
    }

    public void setDownloadBlob(CloudBlockBlob blob) {
        blobToDownload = blob;
    }

    private BroadcastReceiver uploadDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadBlobs();
        }
    };


    class LoadBlobs extends AsyncTask<String, Void, Void> {

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

                saveEncryptedKeyToSharedPreferences(blobClient);

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

        private void saveEncryptedKeyToSharedPreferences(CloudBlobClient blobClient) throws StorageException, IOException, URISyntaxException {
            File tempFile = downloadTempKeyFile(blobClient);

            BufferedReader brTest = new BufferedReader(new FileReader(tempFile));
            String firstLine = brTest.readLine();
            System.out.println("First line from load blobs: " + firstLine);

            saveToSharedPreferences(CryptoUtils.ENCRYPTED_KEY, firstLine);

            tempFile.delete();
        }

        private File downloadTempKeyFile(CloudBlobClient blobClient) throws URISyntaxException, StorageException, IOException {
            CloudBlobContainer keyContainer = blobClient.getContainerReference("keycontainer");
            CloudBlockBlob blobEncryptedKey = keyContainer.getBlockBlobReference(getString(R.string.user_id) + ".txt");
            File tempKeyFile = File.createTempFile("tempkeyfile_download", ".tmp", getCacheDir());
            System.out.println("the path of the file is: " + tempKeyFile.getAbsolutePath());
            blobEncryptedKey.downloadToFile(tempKeyFile.getAbsolutePath());
            return tempKeyFile;
        }
    }

    public void saveToSharedPreferences(String key, String value) {
        SharedPreferences sp = getSharedPreferences("KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }


}
