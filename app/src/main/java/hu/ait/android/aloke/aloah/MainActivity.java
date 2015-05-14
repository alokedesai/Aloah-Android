package hu.ait.android.aloke.aloah;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import hu.ait.android.aloke.aloah.adapter.BlobListAdapter;
import hu.ait.android.aloke.aloah.crypto.CryptoUtils;
import hu.ait.android.aloke.aloah.fragment.WelcomeDialogFragment;
import hu.ait.android.aloke.aloah.model.ImageItem;


public class MainActivity extends ActionBarActivity {
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
    private boolean canRefresh = false;

    public static final int FILE_CODE = 101;
    public static final int PHOTO_CODE = 102;
    public static final int NEW_USER_CODE = 103;

    private CloudStorageAccount storageAccount;
    private ArrayList<ImageItem> images = new ArrayList<>();
    private ListView listView;
    private BlobListAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ParseObject currentUser;

    private MenuItem adminItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            storageAccount = CloudStorageAccount.parse(STORAGE_CONNECTION_STRING);
        } catch (URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
        }

        CryptoUtils.setContext(this);


        intializeParse();

        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.tvEmpty));
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBlobs();
            }
        });


//        // we only need to decrypt all the symmetric keys
//        if (currentUser != null && currentUser.getBoolean("owner")) {
//            adminItem.setVisible(true);
////            AsyncTask<Void, Void, Integer> getKeyBlobsAsync = new GetNumKeyBlobs(this);
////            getKeyBlobsAsync.execute();
//        }


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

    private void intializeParse() {
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "MdcgPHF16T9TSgEWIKhkozwm0ZGv0ZSQL85FQZR5", "Q8TTs5ClUELbRrbw9kjNRjiov4WHLUF3AaAvpELg");

        final String deviceId = getDeviceId();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        query.whereEqualTo("deviceId", deviceId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    currentUser = scoreList.get(0);
                    canRefresh = true;

                    if (currentUser.getBoolean("owner")) {
                        adminItem.setVisible(true);
                    }

                    loadBlobs();
                } else {
                    startNewUserActivity();
                }
            }
        });
    }

    private void startNewUserActivity() {
        Intent intent = new Intent(this, NewUserActivity.class);
        startActivityForResult(intent, NEW_USER_CODE);
    }

    private String getDeviceId() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void launchWelcomeDialog() {
        WelcomeDialogFragment dialog = new WelcomeDialogFragment();
        dialog.show(getSupportFragmentManager(), WelcomeDialogFragment.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        adminItem = menu.findItem(R.id.action_admin);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_take_photo) {
            startTakePhotoActivity();
        } else if (item.getItemId() == R.id.action_admin) {
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void startTakePhotoActivity() {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        startActivityForResult(intent, PHOTO_CODE);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(
                deleteDoneReceiver, new IntentFilter(DeleteFile.FILTER_RESULT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uploadDoneReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deleteDoneReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {

            currentState = UPLOAD_STATE;

            uriForUpload = data.getData();
            String path = "" + getRealPathFromURI(uriForUpload);
            System.out.println("URI passed to activity result: " + uriForUpload);
            uploadFile(path);
        } else if (requestCode == PHOTO_CODE && resultCode == Activity.RESULT_OK) {

            currentState = UPLOAD_STATE;

            String filePath = data.getExtras().getString(TakePhotoActivity.PHOTO_PATH);
            System.out.println("PATH passed to activity result: " + filePath);
            uploadFile(filePath);
        } else if (requestCode == NEW_USER_CODE && resultCode == Activity.RESULT_OK) {
            String username = data.getExtras().getString(NewUserActivity.NAME);
            createParseUser(username);
        }
    }

    public void setState(int state) {
        currentState = state;
    }

    public int getState() {
        return currentState;
    }

    private void loadBlobs() {
        if (canRefresh) {
            AsyncTask<String, Void, Void> asyncTask = new LoadBlobs();
            asyncTask.execute();
        }
    }

    private void setBlobAdapter(ArrayList<ImageItem> blobs) {

        adapter = new BlobListAdapter(blobs, this);
        listView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }


    public void downloadFile(CloudBlockBlob blob, int index) {
        if (blob == null) {
            makeToast("Null blob, download failed.");
            return;
        }
        AsyncTask<CloudBlockBlob, Void, Boolean> asyncTask = new DownloadFile(this, index);
        asyncTask.execute(blob);
    }

    public void setIsDownloaded(int index) {
        adapter.setIsDownloaded(index);
    }

    public void setFile(int index, File file) {
        adapter.setFile(index, file);
    }

    private void uploadFile(String path) {
        if ("".equals(path)) {
            makeToast("Uri null, upload failed");
            return;
        }
        AsyncTask<String, Void, Boolean> asyncTask = new UploadFile(this);
        asyncTask.execute(path);
    }

    public String getRealPathFromURI(Uri contentUri) {

        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            System.out.println("URI real path: " + cursor.getString(column_index));
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public CloudStorageAccount getStorageAccount() {
        return storageAccount;
    }

    private BroadcastReceiver uploadDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadBlobs();
        }
    };

    private BroadcastReceiver deleteDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadBlobs();
        }
    };


    //TODO: move this to an eventbus
    class LoadBlobs extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            // reset images
            images.clear();
            canRefresh = false;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                storageAccount = CloudStorageAccount.parse(STORAGE_CONNECTION_STRING);
            } catch (URISyntaxException | InvalidKeyException e) {
                e.printStackTrace();
            }

            // get all the images from a container
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            try {
                // Retrieve reference to a previously created container.
                CloudBlobContainer container = blobClient.getContainerReference("testcontainer");

                for (ListBlobItem b : container.listBlobs()) {
                    images.add(new ImageItem(b));
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

            canRefresh = true;
            setBlobAdapter(images);
        }

        private void saveEncryptedKeyToSharedPreferences(CloudBlobClient blobClient) throws StorageException, IOException, URISyntaxException {
            File tempFile = downloadTempKeyFile(blobClient);

            BufferedReader brTest = new BufferedReader(new FileReader(tempFile));
            String firstLine = brTest.readLine();
            System.out.println("First line from load images: " + firstLine);

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

    public void openImageFromImageItem(ImageItem imageItem) {
        File file = imageItem.getFile();
        Uri path = Uri.fromFile(file);
        Intent imageOpenIntent = new Intent(Intent.ACTION_VIEW);
        imageOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        imageOpenIntent.setDataAndType(path, "image/*");
        startActivity(imageOpenIntent);
    }

    public void deleteFile(CloudBlockBlob blob) {
        AsyncTask<CloudBlockBlob, Void, Boolean> asyncTask = new DeleteFile(this);
        asyncTask.execute(blob);
    }

    public void onThreadFinish(int numKeyBlobs) {

        canRefresh = true;
        if (numKeyBlobs == 0) {
            CryptoUtils.symmetricKeyHandshake();
        }
        loadBlobs();
    }

    public void createParseUser(String username) {
        ParseObject user = new ParseObject("User");
        user.put("username", username);
        user.put("owner", false);
        user.put("deviceId", getDeviceId());

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                canRefresh = true;
                loadBlobs();

            }
        });
    }
}
