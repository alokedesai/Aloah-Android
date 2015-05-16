package hu.ait.android.aloke.aloah;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.adapter.BlobListAdapter;
import hu.ait.android.aloke.aloah.crypto.CryptoUtils;
import hu.ait.android.aloke.aloah.event.CreateRSAKeysEvent;
import hu.ait.android.aloke.aloah.event.DeleteFileEvent;
import hu.ait.android.aloke.aloah.event.DownloadEncryptedKeyEvent;
import hu.ait.android.aloke.aloah.event.DownloadFileEvent;
import hu.ait.android.aloke.aloah.event.LoadBlobsEvent;
import hu.ait.android.aloke.aloah.event.UploadEncryptedKeyEvent;
import hu.ait.android.aloke.aloah.event.UploadFileEvent;
import hu.ait.android.aloke.aloah.fragment.WelcomeDialogFragment;
import hu.ait.android.aloke.aloah.model.DataItem;
import hu.ait.android.aloke.aloah.task.CreateRSAKeys;
import hu.ait.android.aloke.aloah.task.DeleteFile;
import hu.ait.android.aloke.aloah.task.DownloadEncryptedKey;
import hu.ait.android.aloke.aloah.task.DownloadFile;
import hu.ait.android.aloke.aloah.task.LoadBlobs;
import hu.ait.android.aloke.aloah.task.UploadFile;


public class MainActivity extends ActionBarActivity {
    public static final String STORAGE_CONNECTION_STRING =
            "DefaultEndpointsProtocol=https;" +
                    "AccountName=aloah;" +
                    "AccountKey=t4gFHiiTQhPVYLqS3DI0EJ5loeEeU3vUqmIQFp57+UEfdL+FtRrhPAuB4i0Ad1S/pvxvO0DaI87FccGXw4Qstg==";

    public static final String KEY_CONTAINER = "keycontainer";
    public static final String TEST_CONTAINER = "testcontainer";

    public String inputKey;
    private Uri uriForUpload = null;
    private boolean canRefresh = false;

    public static final int FILE_CODE = 101;
    public static final int PHOTO_CODE = 102;
    public static final int NEW_USER_CODE = 103;
    public static final int VIDEO_CODE = 104;

    private CloudStorageAccount storageAccount;
    private ArrayList<DataItem> images = new ArrayList<>();
    private ListView listView;
    private BlobListAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ParseObject currentUser;

    private MenuItem adminItem;
    private TextView tvEmpty;
    private TextView tvUnapproved;
    private Button btnRefresh;


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

        // saveKeysToSharedPreferences();


        //CryptoUtils.symmetricKeyHandshake();

        initializeParse();

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        listView.setEmptyView(tvEmpty);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBlobs();
            }
        });

        btnRefresh = (Button) findViewById(R.id.btnRefreshApproved);
        tvUnapproved = (TextView) findViewById(R.id.tvUnapproved);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // refresh the current user. If the current user is now approved, load blobs. Otherwise
                // flash a toast saying still not approved
                refreshCurrentUser();
                if (currentUser.getBoolean("approved")) {
                    hideUnapprovedText();
                    canRefresh = true;

                    downloadEncryptedKey();
                } else {
                    makeToast(getString(R.string.not_approved_toast_text));
                }
            }
        });

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

    private void initializeParse() {
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "MdcgPHF16T9TSgEWIKhkozwm0ZGv0ZSQL85FQZR5", "Q8TTs5ClUELbRrbw9kjNRjiov4WHLUF3AaAvpELg");

        final String deviceId = getDeviceId();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        query.whereEqualTo("deviceId", deviceId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null && scoreList.size() > 0) {
                    currentUser = scoreList.get(0);

                    String privateKey = CryptoUtils.getKeyFromSharedPreferences(CryptoUtils.PRIVATE_KEY);
                    if (privateKey == null) {
                        currentUser.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                startNewUserActivity();
                            }
                        });

                    } else {
                        if (currentUser.getBoolean("owner")) {
                            adminItem.setVisible(true);
                        }

                        if (currentUser.getBoolean("approved")) {
                            canRefresh = true;

                            // download the encrypted key and save it to shared preferences, on result
                            // load blobs
                            downloadEncryptedKey();
                        } else {
                            showUnapprovedText();
                        }
                    }
                } else {
                    startNewUserActivity();
                }
            }
        });
    }

    private void showUnapprovedText() {
        tvEmpty.setVisibility(View.GONE);
        tvUnapproved.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.VISIBLE);
    }

    private void hideUnapprovedText() {
        tvEmpty.setVisibility(View.VISIBLE);
        tvUnapproved.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
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
        } else if (item.getItemId() == R.id.action_take_video) {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, VIDEO_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void startTakePhotoActivity() {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        startActivityForResult(intent, PHOTO_CODE);
    }

    private void startGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("image/*,video/mp4");
        startActivityForResult(intent, FILE_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            uriForUpload = data.getData();
            String path = "" + getRealPathFromURI(uriForUpload);
            System.out.println("URI passed to activity result: " + uriForUpload);
            uploadFile(path);

        } else if (requestCode == PHOTO_CODE && resultCode == Activity.RESULT_OK) {
            String filePath = data.getExtras().getString(TakePhotoActivity.PHOTO_PATH);
            System.out.println("PATH passed to activity result: " + filePath);
            uploadFile(filePath);

        } else if (requestCode == NEW_USER_CODE && resultCode == Activity.RESULT_OK) {
            String username = data.getExtras().getString(NewUserActivity.NAME);
            createParseUser(username);

        } else if (requestCode == VIDEO_CODE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            uploadFile(getRealPathFromURI(videoUri));
        }
    }

    // onEvent methods for async tasks
    //------------------------------------------------
    public void onEvent(UploadFileEvent event) {
        if (event.result) {
            makeToast(getString(R.string.file_uploaded_succesfully_toast_text));
            loadBlobs();
        } else {
            makeToast(getString(R.string.file_upload_unsuccessful_toast_text));
        }
    }

    public void onEvent(DownloadFileEvent event) {
        if (event.success) {
            makeToast(getString(R.string.downloading_successful_toast_text));

            setIsDownloaded(event.index);
            setFile(event.index, event.outputFile);
        } else {
            makeToast(getString(R.string.downloading_error_toast_text));
        }
    }

    public void onEvent(CreateRSAKeysEvent event) {
        makeToast(getString(R.string.rsa_key_successful_toast_text));
        CryptoUtils.saveRSAKeysToSharedPreferences(event.pair.getPublic(), event.pair.getPrivate());
    }

    public void onEvent(DeleteFileEvent event) {
        if (event.success) {
            makeToast(getString(R.string.file_deleted_successful_toast_text));
            loadBlobs();
        } else {
            makeToast(getString(R.string.file_deleted_unsuccessful_toast_text));
        }
    }

    public void onEvent(UploadEncryptedKeyEvent event) {
        if (event.success) {
            makeToast(getString(R.string.key_uploaded_succesful_toast_text));
        }
    }

    public void onEvent(LoadBlobsEvent event) {
        canRefresh = true;
        images = event.images;
        setBlobAdapter(images);
    }

    public void onEvent(DownloadEncryptedKeyEvent event) {
        String encryptedKey = event.encryptedKey;
        saveToSharedPreferences(CryptoUtils.ENCRYPTED_KEY, encryptedKey);
        loadBlobs();
    }
    // End onEvent methods
    //------------------------------------------------

    private void loadBlobs() {
        if (currentUser != null) {
            refreshCurrentUser();
        }

        if (canRefresh) {
            canRefresh = false;
            images.clear();

            AsyncTask<String, Void, ArrayList<DataItem>> asyncTask = new LoadBlobs(storageAccount);
            asyncTask.execute();
        }
    }

    private void downloadEncryptedKey() {
        AsyncTask<String, Void, String> asyncTask = new DownloadEncryptedKey(storageAccount, getCacheDir());
        asyncTask.execute(currentUser.getObjectId());
    }

    private void refreshCurrentUser() {
        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    currentUser = parseObject;
//                    if (currentUser.getBoolean("approved")) {
//                        canRefresh = true;
//                    }
                }

            }
        });
    }

    private void setBlobAdapter(ArrayList<DataItem> blobs) {

        adapter = new BlobListAdapter(blobs, this);
        listView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void downloadFile(CloudBlockBlob blob, int index) {
        if (blob == null) {
            makeToast(getString(R.string.download_file_null_blob_toast_text));
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
            makeToast(getString(R.string.null_uri_upload_failed_toast_text));
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

    public void saveToSharedPreferences(String key, String value) {
        SharedPreferences sp = getSharedPreferences("KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public void openImageFromImageItem(DataItem dataItem) {
        File file = dataItem.getFile();
        Uri path = Uri.fromFile(file);
        Intent imageOpenIntent = new Intent(Intent.ACTION_VIEW);
        imageOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (file.getName().endsWith(".mp4")) {
            imageOpenIntent.setDataAndType(path, "video/*");
        } else {
            imageOpenIntent.setDataAndType(path, "image/*");
        }

        startActivity(imageOpenIntent);
    }

    public void deleteFile(CloudBlockBlob blob) {
        AsyncTask<CloudBlockBlob, Void, Boolean> asyncTask = new DeleteFile(this);
        asyncTask.execute(blob);
    }

    public void createParseUser(String username) {
        final ParseObject user = new ParseObject("User");
        user.put("username", username);
        user.put("owner", false);
        user.put("deviceId", getDeviceId());
        user.put("approved", false);

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                currentUser = user;
                showUnapprovedText();
                // create RSA keys
                AsyncTask<Void, Void, KeyPair> asyncTask = new CreateRSAKeys(MainActivity.this, currentUser);
                asyncTask.execute();
            }
        });
    }
}
