package hu.ait.android.aloke.aloah;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.adapter.UnapprovedUsersListAdapter;
import hu.ait.android.aloke.aloah.event.UploadEncryptedKeyEvent;


public class AdminActivity extends ActionBarActivity {
    List<ParseObject> usersToApprove;
    UnapprovedUsersListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        final ListView listView = (ListView) findViewById(R.id.listView);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        query.whereEqualTo("approved", false);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    usersToApprove = scoreList;
                } else {
                    usersToApprove = new ArrayList<>();
                }
                adapter = new UnapprovedUsersListAdapter(AdminActivity.this, usersToApprove);
                // set the list adapter
                listView.setAdapter(adapter);
            }
        });
    }


    //TODO: think of a cleaner way of doing this
    public CloudStorageAccount getStorageAccount() {
        CloudStorageAccount storageAccount = null;
        try {
            storageAccount = CloudStorageAccount.parse(MainActivity.STORAGE_CONNECTION_STRING);
        } catch(URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return storageAccount;
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

    public void onEvent(UploadEncryptedKeyEvent event) {
        if (event.success) {
            Toast.makeText(this, getString(R.string.key_uploaded_succesful_toast_text),
                    Toast.LENGTH_LONG).show();
            adapter.removeItem(event.listIndex);
        }
    }
}
